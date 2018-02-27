package com.dtforce.spring.kubernetes;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KubernetesDiscoveryAutoConfiguration
{

	@Bean
	@ConditionalOnMissingBean
	public KubernetesClient kubernetesClient()
	{
		return new DefaultKubernetesClient();
	}

	@Bean
	@ConditionalOnProperty(name = "spring.kubernetes.discovery.enabled", matchIfMissing = true)
	public KubernetesDiscoveryClient kubernetesDiscoveryClient()
	{
		return new KubernetesDiscoveryClient(kubernetesClient());
	}

}
