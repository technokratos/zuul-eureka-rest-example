package com.ognio.rest.service.conditional;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.stream.Stream;

/**
 * @author Denis B. Kulikov<br/>
 * date: 25.08.2019:4:52<br/>
 */
public class ClusterCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return Stream.of(context.getEnvironment().getActiveProfiles()).anyMatch(s -> s.contains("node"));
    }
}
