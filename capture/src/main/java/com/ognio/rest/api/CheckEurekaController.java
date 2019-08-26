
package com.ognio.rest.api;

import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.ognio.rest.config.InitConfig;
import com.ognio.rest.service.conditional.ClusterCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Component
@RestController
@Conditional(ClusterCondition.class)
public class CheckEurekaController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Lazy
    @Autowired
    EurekaClient eurekaClient;

    @Value("${spring.application.name}")
    private String appName;


    @RequestMapping("/eureka")
    public String check() {
        logger.debug("Request eureka");
        Application application = eurekaClient== null? null :eurekaClient.getApplication(appName);
        String status = eurekaClient== null? "is not found" : eurekaClient.getInstanceRemoteStatus().toString();
        String applicationName = application == null ? appName : application.getName();
        return String.format("Eureka %s, application '%s'!", status, applicationName);
    }
}