package com.dtforce.spring.kubernetes.ribbon;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ServerList;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.dtforce.spring.kubernetes.api.IServiceInstanceExtractor;
import com.dtforce.spring.kubernetes.discovery.KubernetesServerList;

import javax.validation.constraints.NotNull;

@Configuration
public class KubernetesRibbonConfiguration
{

	@Bean
	@ConditionalOnBean({KubernetesClient.class, IClientConfig.class, IServiceInstanceExtractor.class})
	public ServerList<?> kubernetesServerList(
		@NotNull final KubernetesClient kubernetesClient,
		@NotNull final IClientConfig clientConfig,
		@NotNull final IServiceInstanceExtractor serviceInstanceExtractor
	)
	{
		KubernetesServerList serverList = new KubernetesServerList(kubernetesClient, serviceInstanceExtractor);
		serverList.initWithNiwsConfig(clientConfig);
		return serverList;
	}

}
