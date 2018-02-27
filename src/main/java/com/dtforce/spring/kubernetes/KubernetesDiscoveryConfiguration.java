package com.dtforce.spring.kubernetes;

import com.netflix.loadbalancer.ConfigurationBasedServerList;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KubernetesDiscoveryConfiguration
{

	@Bean
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

	@Bean
	@ConditionalOnProperty(name = "spring.kubernetes.ribbon.enabled", matchIfMissing = true)
	public KubernetesServerList kubernetesServerList()
	{
		return new KubernetesServerList(kubernetesClient());
	}

}
