package com.ognio.rest.service.impl;

import com.ognio.rest.api.data.Feature;
import com.ognio.rest.api.data.FeatureCollection;
import com.ognio.rest.api.data.Point;
import com.ognio.rest.model.GeoFeature;
import com.ognio.rest.model.GeoPoint;
import com.ognio.rest.service.GeoService;
import com.ognio.rest.service.MappingService;
import com.ognio.rest.service.conditional.SingleCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.String.format;

/**
 * @author Denis B. Kulikov<br/>
 * date: 23.08.2019:19:43<br/>
 */
@Service
@Conditional(SingleCondition.class)
public class MemoryGeoServiceImpl implements GeoService {

    private static Logger logger = LoggerFactory.getLogger(MemoryGeoServiceImpl.class);
    @Autowired
    private MappingService mappingService;

    @Autowired
    @Qualifier("firstCollection")
    private FeatureCollection featureCollection;

    private final ConcurrentMap<Long, GeoFeature> map = new ConcurrentHashMap<>();

    @Value("${conquer.radius:15}")
    private int conquerRadius;


    @PostConstruct
    private void init() {
        featureCollection.getFeatures().values().stream()
                .map(f -> mappingService.toModel(f))
                .forEach(g -> map.put(g.getId(), g));

    }


    @Override
    public FeatureCollection getPoints(Point from, Point to) {
        GeoPoint fromPoint = mappingService.toModel(from);
        GeoPoint toPoint = mappingService.toModel(to);
        HashMap<Long, Feature> featuresMap = new HashMap<>();
        FeatureCollection featureCollection = new FeatureCollection(featuresMap);
        map.values().stream()
                .filter(f -> f.getClientId() == null)
                .filter(point -> point.getGeoPoint().between(fromPoint, toPoint))//todo it isn't best
                .forEach(p -> featuresMap.put(p.getId(), mappingService.toApi(p)));
        return featureCollection;
    }

    @Override
    public Long score(Long clientId) {
        return map.values().stream().filter(point -> clientId.equals(point.getClientId())).count();
    }

    @Override
    public Boolean conquer(Long clientId, Point point) {
        GeoPoint basePoint = mappingService.toModel(point);
        AtomicBoolean result = new AtomicBoolean(false);
        map.values().stream().parallel()
                .filter(geoFeature -> geoFeature.getClientId() == null)
                .filter(geoFeature -> geoFeature.getGeoPoint().near(basePoint, conquerRadius))
                .map(GeoFeature::getId)
                .forEach(pointId -> map.computeIfPresent(pointId, (id, p) -> {//Point(coordinates=[-0.1387765, 51.5283234])
                    if (p.getClientId() == null) {
                        p.setClientId(clientId);
                        result.set(true);
                    }
                    return p;
                }));
        return result.get();

    }
}
