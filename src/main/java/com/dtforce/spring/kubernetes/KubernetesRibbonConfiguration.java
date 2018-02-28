package com.dtforce.spring.kubernetes;

import com.netflix.client.config.IClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KubernetesRibbonConfiguration
{

	@Bean
	public DiscoveryClientServerList kubernetesServerList(
		KubernetesDiscoveryClient kubernetesDiscoveryClient, IClientConfig clientConfig)
	{
		DiscoveryClientServerList serverList = new DiscoveryClientServerList(kubernetesDiscoveryClient);
		serverList.initWithNiwsConfig(clientConfig);
		return serverList;
	}

}
