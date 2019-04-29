package com.dtforce.spring.kubernetes.ribbon;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty("spring.cloud.kubernetes.enabled")
@AutoConfigureAfter(RibbonAutoConfiguration.class)
@RibbonClients(defaultConfiguration = KubernetesRibbonConfiguration.class)
public class KubernetesRibbonAutoConfiguration
{
}
