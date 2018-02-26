package com.dtforce.spring.kubernetes;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.kubernetes.discovery.enabled", matchIfMissing = true)
public class KubernetesDiscoveryConfiguration
{

	@Bean
	@ConditionalOnMissingBean
	public KubernetesDiscoveryClient kubernetesDiscoveryClient()
	{
		return new KubernetesDiscoveryClient();
	}

}
