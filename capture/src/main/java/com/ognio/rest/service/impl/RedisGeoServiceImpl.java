package com.ognio.rest.service.impl;

import cn.yueshutong.redislock.RedisLock;
import com.ognio.rest.api.data.Feature;
import com.ognio.rest.api.data.FeatureCollection;
import com.ognio.rest.api.data.Point;
import com.ognio.rest.service.GeoService;
import com.ognio.rest.service.conditional.ClusterCondition;
import org.apache.lucene.util.SloppyMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.ognio.rest.api.data.Point.ID;

/**
 * @author Denis B. Kulikov<br/>
 * date: 24.08.2019:20:29<br/>
 */
@Service
@Conditional(ClusterCondition.class)
public class RedisGeoServiceImpl implements GeoService {
    private static Logger logger = LoggerFactory.getLogger(RedisGeoServiceImpl.class);

    public static final String CONQUER_LOCK = "conquer_lock";
    @Autowired
    private RedisLock lock;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Value("${conquer.radius:15}")
    private int conquerRadius;

    @Override
    public FeatureCollection getPoints(Point from, Point to) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        GeoOperations<String, Object> geoOperations = redisTemplate.opsForGeo();
        double x1 = to.getCoordinates()[0];
        double y1 = to.getCoordinates()[1];
        double x0 = from.getCoordinates()[0];
        double y0 = from.getCoordinates()[1];
        double xm = (x0+x1)/2;
        double ym = (y0+y1)/2;
        double r =  SloppyMath.haversinMeters(x0, y0, x1, y1)/2;//todo it isnn't best way to find point in rectangle
        GeoResults<RedisGeoCommands.GeoLocation<Object>> features = geoOperations.radius("features", new Circle(new org.springframework.data.geo.Point(xm, ym), new Distance(r, RedisGeoCommands.DistanceUnit.METERS)));
        Map<Long, Feature> featureMap = features.getContent().stream()
                .map(GeoResult::getContent)
                .filter(objectGeoLocation -> valueOperations.get(objectGeoLocation.getName()) == null)
                .map(loc -> {
                    HashMap<String, String> properties = new HashMap<>();
                    properties.put(ID, loc.getName().toString());
                    properties.put(Point.MARKED, "false");
                    List<org.springframework.data.geo.Point> points = geoOperations.position("features", loc.getName());
                    org.springframework.data.geo.Point point = points.get(0);
                    return new Feature(new Point(new double[]{point.getX(), point.getY()}), properties);
                })
                .filter(f-> {
                    double[] coordinates = f.getGeometry().getCoordinates();
                    return coordinates[0] >= from.getCoordinates()[0] &&
                            coordinates[1] >= from.getCoordinates()[1] &&
                            coordinates[0] <= to.getCoordinates()[0] &&
                            coordinates[1] <= to.getCoordinates()[1];
                })
                .collect(Collectors.toMap(f -> Long.parseLong(f.getProperties().get(ID)), f -> f));
        ;
        return new FeatureCollection(featureMap);
    }

    @Override
    public Long score(Long clientId) {
        GeoOperations<String, Object> geoOperations = redisTemplate.opsForGeo();
        GeoResults<RedisGeoCommands.GeoLocation<Object>> features = geoOperations.radius("features", new Circle(new org.springframework.data.geo.Point(0d, 0d), new Distance(10000, RedisGeoCommands.DistanceUnit.KILOMETERS)));
//RedisGeoCommands.GeoLocation(name=305075246, point=null) RedisGeoCommands.GeoLocation(name=26610590, point=null)
        AtomicLong count = new AtomicLong();
        features.getContent().stream().parallel().forEach(result -> {
            Object value = redisTemplate.opsForValue().get(result.getContent().getName().toString());
            if (value != null) {
                count.getAndIncrement();
            }
        });
        return count.get();
    }
    /*
     -0.154481,
               51.654469

              -0.1443341,
              51.5405479
     */

    @Override
    public Boolean conquer(Long clientId, Point point) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        GeoOperations<String, Object> geoOperations = redisTemplate.opsForGeo();
        Circle circle = new Circle(new org.springframework.data.geo.Point(point.getCoordinates()[0], point.getCoordinates()[1]),
                new Distance(conquerRadius, RedisGeoCommands.DistanceUnit.METERS));
        GeoResults<RedisGeoCommands.GeoLocation<Object>> geoResult = geoOperations.radius("features", circle);


        String strClientId = clientId.toString();
        AtomicBoolean result = new AtomicBoolean(false);
        if (geoResult == null || geoResult.getContent() == null ) {
            logger.error("Error in try to conquer {}, {}, redis result is incorrect", clientId, point.toString());
            return false;
        }
        geoResult.getContent().stream()
                .map(g -> g.getContent().getName().toString())
                .forEach(pointId -> {
                    lock.lock(CONQUER_LOCK, pointId);
                    try {
                        String ownerId = (String) valueOperations.get(pointId);
                        if (ownerId == null) {
                            valueOperations.set(pointId, strClientId);
                            result.set(true);
                        }
                    } finally {
                        lock.lock(CONQUER_LOCK, pointId);
                    }
                });

        return result.get();
    }


}
