package com.dtforce.spring.kubernetes.discovery;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import com.dtforce.spring.kubernetes.instance.DefaultInstanceExtractor;
import com.dtforce.spring.kubernetes.discovery.KubernetesDiscoveryClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DiscoveryClientTests
{

	private final String serviceId = "dummy-service";

	private final String multiPortServiceId = "multiport-service";

	@Rule
	public KubernetesServer server = new KubernetesServer(true, true);

	private DiscoveryClient discoveryClient;

	@Before
	public void setUp()
	{
		final KubernetesClient kube = server.getClient();
		this.discoveryClient = new KubernetesDiscoveryClient(
			kube,
			new DefaultInstanceExtractor("http")
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
