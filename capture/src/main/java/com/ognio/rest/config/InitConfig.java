package com.ognio.rest.config;

/**
 * @author Denis B. Kulikov<br/>
 * date: 24.08.2019:19:24<br/>
 */

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ognio.rest.api.data.FeatureCollection;
import com.ognio.rest.service.MappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

@Configuration
public class InitConfig {
    private static Logger logger = LoggerFactory.getLogger(InitConfig.class);

    @Bean
    @Qualifier("firstCollection")
    public FeatureCollection featureCollection(){
        try {
            File file = ResourceUtils.getFile("classpath:data.json");
            //InitConfig.class.getClassLoader().getResourceAsStream("data.json");
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            return mapper.readValue(new FileInputStream(file), FeatureCollection.class);
        } catch (IOException e) {
            logger.error("Not load default values", e);
            return new FeatureCollection(Collections.emptyMap());
        }
    }
}
