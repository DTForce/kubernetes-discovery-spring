package com.dtforce.spring.kubernetes;

import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;
import org.springframework.context.annotation.Import;

import com.dtforce.spring.kubernetes.discovery.KubernetesDiscoveryAutoConfiguration;
import com.dtforce.spring.kubernetes.ribbon.KubernetesRibbonAutoConfiguration;

@Import({RibbonAutoConfiguration.class, KubernetesRibbonAutoConfiguration.class, KubernetesDiscoveryAutoConfiguration.class})
public class TestConfiguration
{

}
