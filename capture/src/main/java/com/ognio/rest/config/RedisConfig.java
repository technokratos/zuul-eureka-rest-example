package com.ognio.rest.config;

import com.ognio.rest.api.data.FeatureCollection;
import com.ognio.rest.service.MappingService;
import com.ognio.rest.service.conditional.ClusterCondition;
import com.ognio.rest.service.impl.MemoryGeoServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;

import java.nio.charset.Charset;

/**
 * @author Denis B. Kulikov<br/>
 * date: 25.08.2019:4:58<br/>
 */
@Configuration
@Conditional(ClusterCondition.class)
public class RedisConfig {

    private static Logger logger = LoggerFactory.getLogger(MemoryGeoServiceImpl.class);

    @Value("${redis.host:192.168.99.100}")
    private String redisHost;

    @Value("${redis.port:32768}")
    private int redisPort;

    @Autowired
    private MappingService mappingService;

    @Autowired
    @Qualifier("firstCollection")
    private FeatureCollection featureCollection;


    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration(redisHost, redisPort);
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(standaloneConfig);

        return jedisConnectionFactory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setKeySerializer(new GenericToStringSerializer<String>(String.class, Charset.forName("UTF8")));
        template.setValueSerializer(new GenericToStringSerializer<>(String.class, Charset.forName("UTF8")));
        template.setConnectionFactory(jedisConnectionFactory());
        template.afterPropertiesSet();
        initFirstValue(template);
        return template;
    }

    private void initFirstValue(RedisTemplate<String, Object> template) {
        GeoOperations<String, Object> geoOperations = template.opsForGeo();


        featureCollection.getFeatures().values().stream()
                .map(f -> mappingService.toModel(f))
                .forEach(g->
                        geoOperations.add("features", new Point(g.getGeoPoint().getLongitude(), g.getGeoPoint().getLatitude()), Long.toString(g.getId())));

    }
}
