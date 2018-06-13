package com.dtforce.spring.kubernetes.tests;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import static org.assertj.core.api.Assertions.assertThat;

import com.dtforce.spring.kubernetes.KubernetesDiscoveryClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class DiscoveryClientTests
{

	private final String serviceId = "dummy-service";

	private final String multiPortServiceId = "multiport-service";

	private final String cachedService = "cached-service";

	private final int cacheRefreshSeconds = 3;

	@Rule
	public KubernetesServer server = new KubernetesServer(true, true);

	private KubernetesClient kube;

	private DiscoveryClient discoveryClient;

	@Before
	public void setUp()
	{
		this.kube = server.getClient();
		this.discoveryClient = new KubernetesDiscoveryClient(
			this.kube,
			Duration.ofMinutes(1), Duration.ofSeconds(cacheRefreshSeconds),
			100
		);

		assertThat(discoveryClient.getServices()).isEmpty();

		Map<String, String> svcLabels = new HashMap<>();
		svcLabels.put("beta.kubernetes.io/arch", "amd64");
		svcLabels.put("beta.kubernetes.io/os", "linux");

		kube.services().createNew()
			.withNewMetadata()
				.withName(serviceId)
				.withNamespace(kube.getNamespace())
				.withLabels(svcLabels)
			.and()
			.withNewSpec()
				.withType("ClusterIP")
				.withClusterIP("192.168.1.120")
				.addNewPort()
					.withPort(80).withProtocol("TCP")
					.endPort()
			.and()
			.done();

		kube.services().createNew()
			.withNewMetadata()
				.withName(multiPortServiceId)
				.withNamespace(kube.getNamespace())
				.withLabels(svcLabels)
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

		kube.services().createNew()
			.withNewMetadata()
				.withName(cachedService)
				.withNamespace(kube.getNamespace())
				.withLabels(svcLabels)
			.and()
			.withNewSpec()
				.withType("ClusterIP")
				.withClusterIP("192.168.1.122")
				.addNewPort()
					.withPort(80).withProtocol("TCP")
					.endPort()
			.and()
			.done();
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
		List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
		assertThat(instances).hasAtLeastOneElementOfType(ServiceInstance.class);
		validateServiceInstance(instances.get(0));
	}

	@Test
	public void cacheReloadSuccess() throws InterruptedException
	{
		List<ServiceInstance> initialInstances = discoveryClient.getInstances(cachedService);
		assertThat(initialInstances).hasAtLeastOneElementOfType(ServiceInstance.class);

		String newIP = "10.11.12.13";

		Service svc = this.kube.services().withName(cachedService).get();
		svc.getSpec().setClusterIP(newIP);
		this.kube.services().delete(svc);
		this.kube.services().createOrReplace(svc);

		Service updatedSvc = this.kube.services().withName(cachedService).get();
		assertThat(updatedSvc.getSpec().getClusterIP()).isEqualTo(newIP);

		Thread.sleep((cacheRefreshSeconds * 1000) + 1000);

		List<ServiceInstance> updatedInstances = discoveryClient.getInstances(cachedService);
		assertThat(updatedInstances).hasAtLeastOneElementOfType(ServiceInstance.class);
		assertThat(updatedInstances.get(0).getHost()).isEqualTo(newIP);
	}

	@Test
	public void getInstancesForNonExistentService()
	{
		List<ServiceInstance> instances = discoveryClient.getInstances("noop");
		assertThat(instances).isEmpty();
	}

	@Test
	public void getHttpInstanceForMultiPortService() {
		List<ServiceInstance> instances = discoveryClient.getInstances(multiPortServiceId);
		assertThat(instances).hasSize(1);
		validateServiceInstance(instances.get(0));

		assertThat(instances.get(0).getPort()).isEqualTo(8080);
	}

	private void validateServiceInstance(ServiceInstance serviceInstance)
	{
		assertThat(serviceInstance.getHost()).isNotEmpty();
		assertThat(serviceInstance.getPort()).isNotZero();
		assertThat(serviceInstance.getUri()).isNotNull();
		assertThat(serviceInstance.getMetadata()).isNotEmpty();
	}

}
