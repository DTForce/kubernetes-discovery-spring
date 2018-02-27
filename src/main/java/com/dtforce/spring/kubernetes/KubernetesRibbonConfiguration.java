package com.dtforce.spring.kubernetes;

import com.netflix.client.config.IClientConfig;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KubernetesRibbonConfiguration
{

	@Bean
	public KubernetesServerList kubernetesServerList(KubernetesClient kubernetesClient, IClientConfig clientConfig)
	{
		KubernetesServerList serverList = new KubernetesServerList(kubernetesClient);
		serverList.initWithNiwsConfig(clientConfig);
		return serverList;
	}

}
