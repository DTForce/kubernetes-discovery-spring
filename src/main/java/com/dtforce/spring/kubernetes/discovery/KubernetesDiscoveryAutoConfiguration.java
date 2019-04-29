package com.dtforce.spring.kubernetes.discovery;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.dtforce.spring.kubernetes.api.IServiceInstanceExtractor;
import com.dtforce.spring.kubernetes.instance.DefaultInstanceExtractor;

@Configuration
@ConditionalOnProperty(name = "spring.cloud.kubernetes.enabled")
public class KubernetesDiscoveryAutoConfiguration
{

	@Bean
	@ConditionalOnMissingBean
	public IServiceInstanceExtractor serviceInstanceExtractor()
	{
		return new DefaultInstanceExtractor("http");
	}

	@Bean
	@ConditionalOnMissingBean
	public KubernetesClient kubernetesClient()
	{
		return new DefaultKubernetesClient();
	}

	@Bean
	@ConditionalOnProperty(name = "spring.cloud.kubernetes.discovery.enabled")
	public KubernetesDiscoveryClient kubernetesDiscoveryClient(
		KubernetesClient kubernetesClient,
		IServiceInstanceExtractor serviceInstanceExtractor
	)
	{
		return new KubernetesDiscoveryClient(
			kubernetesClient,
			serviceInstanceExtractor
		);
	}

}
