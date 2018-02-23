package com.dtforce.spring.kubernetes.tests;

import com.dtforce.spring.kubernetes.KubernetesDiscoveryClient;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DiscoveryTests
{
	private final String serviceId = "dummy-service";

	@Rule
	public KubernetesServer server = new KubernetesServer(true, true);

	private KubernetesClient kube;

	private DiscoveryClient discoveryClient;

	@Before
	public void setUp()
	{
		this.kube = server.getClient();
		this.discoveryClient = new KubernetesDiscoveryClient(this.kube);

		assertThat(discoveryClient.getServices()).isEmpty();

		Map<String, String> svcAnnotations = new HashMap<>();
		svcAnnotations.put("beta.kubernetes.io/arch", "amd64");
		svcAnnotations.put("beta.kubernetes.io/os", "linux");

		Service svc = new ServiceBuilder()
			.withNewMetadata()
				.withName(serviceId)
				.withAnnotations(svcAnnotations)
			.and()
			.withNewSpec()
				.withType("ClusterIP")
				.withClusterIP("192.168.1.120")
				.addNewPort()
					.withPort(80).withProtocol("TCP")
					.endPort()
			.and()
			.build();
		kube.services().create(svc);
	}

	@Test
	public void listServices()
	{
		List<String> services = discoveryClient.getServices();
		assertThat(services).contains(serviceId);
	}

	@Test
	public void getInstancesForSpecifiedService()
	{
		List<ServiceInstance> services = discoveryClient.getInstances(serviceId);
		assertThat(services).hasAtLeastOneElementOfType(ServiceInstance.class);
		validateServiceInstance(services.get(0));
	}

	private void validateServiceInstance(ServiceInstance serviceInstance)
	{
		assertThat(serviceInstance.getHost()).isNotEmpty();
		assertThat(serviceInstance.getPort()).isNotZero();
		assertThat(serviceInstance.getUri()).isNotNull();
		assertThat(serviceInstance.getMetadata()).isNotEmpty();
	}
}
