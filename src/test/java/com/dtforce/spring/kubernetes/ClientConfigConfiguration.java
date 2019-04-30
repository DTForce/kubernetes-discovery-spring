package com.dtforce.spring.kubernetes;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfigConfiguration
{
	@Bean
	public KubernetesServer kubernetesServer()
	{
		KubernetesServer kubernetesServer = new KubernetesServer(true, true);
		kubernetesServer.before();

		KubernetesClient kubernetesClient = kubernetesServer.getClient();

		kubernetesClient.services().createNew()
			.withNewMetadata()
			.withName("test-service")
			.withNamespace(kubernetesClient.getNamespace())
			.and()
			.withNewSpec()
			.withType("ClusterIP")
			.withClusterIP("192.168.1.121")
			.addNewPort()
			.withPort(80).withProtocol("TCP")
			.endPort()
			.addNewPort()
			.withPort(8080).withProtocol("TCP").withName("http")
			.endPort()
			.and()
			.done();

		return kubernetesServer;
	}

	@Bean
	public KubernetesClient kubernetesClient()
	{
		return kubernetesServer().getClient();
	}

}
