package com.dtforce.spring.kubernetes;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "spring.kubernetes.discovery.enabled", matchIfMissing = true)
public class KubernetesDiscoveryConfiguration
{

	@Bean
	public KubernetesDiscoveryClient kubernetesDiscoveryClient()
	{
		return new KubernetesDiscoveryClient();
	}

}
