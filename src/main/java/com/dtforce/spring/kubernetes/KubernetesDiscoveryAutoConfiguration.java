package com.dtforce.spring.kubernetes;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class KubernetesDiscoveryAutoConfiguration
{

	@Value("${spring.cloud.kubernetes.discovery.cache.expire-after:100s}")
	private Duration cacheExpirySeconds;

	@Value("${spring.cloud.kubernetes.discovery.cache.refresh-after:10s}")
	private Duration cacheRefreshSeconds;

	@Value("${spring.cloud.kubernetes.discovery.cache.maximum-size:1000}")
	private Integer maximumCacheSize;

	@Bean
	@ConditionalOnMissingBean
	public KubernetesClient kubernetesClient()
	{
		return new DefaultKubernetesClient();
	}

	@Bean
	@ConditionalOnProperty(name = "spring.cloud.kubernetes.discovery.enabled", matchIfMissing = true)
	public KubernetesDiscoveryClient kubernetesDiscoveryClient()
	{
		return new KubernetesDiscoveryClient(
			kubernetesClient(),
			cacheExpirySeconds,
			cacheRefreshSeconds,
			maximumCacheSize
		);
	}

}
