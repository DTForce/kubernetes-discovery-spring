package com.dtforce.spring.kubernetes.discovery;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;

import com.dtforce.spring.kubernetes.instance.DefaultInstanceExtractor;
import com.dtforce.spring.kubernetes.discovery.KubernetesDiscoveryClient;
import com.dtforce.spring.kubernetes.api.SelectorEnabledDiscoveryClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class SelectorEnabledDiscoveryClientTests
{

	@Rule
	public KubernetesServer server = new KubernetesServer(true, false);

	private SelectorEnabledDiscoveryClient discoveryClient;

	@Before
	public void setUp()
	{
		final KubernetesClient kube = server.getClient();
		this.discoveryClient = new KubernetesDiscoveryClient(
			kube,
			new DefaultInstanceExtractor("http")
		);

		String namespace = kube.getNamespace();

		Service prodFrontend = new ServiceBuilder()
			.withNewMetadata()
				.withName("service1")
				.withNamespace(namespace)
				.addToLabels("environment", "production")
				.addToLabels("tier", "frontend")
			.and()
				.withNewSpec()
					.withType("ClusterIP")
					.withClusterIP("192.168.1.121")
			.addNewPort()
				.withPort(80).withProtocol("TCP")
				.endPort()
			.and()
			.build();

		Service prodBackend = new ServiceBuilder()
			.withNewMetadata()
				.withName("service2")
				.withNamespace(namespace)
				.addToLabels("environment", "production")
				.addToLabels("tier", "backend")
			.and()
				.withNewSpec()
					.withType("ClusterIP")
					.withClusterIP("192.168.1.122")
			.addNewPort()
				.withPort(80).withProtocol("TCP")
				.endPort()
			.and()
			.build();

		Service prodDatabase = new ServiceBuilder()
			.withNewMetadata()
				.withName("service3")
				.withNamespace(namespace)
				.addToLabels("environment", "production")
				.addToLabels("tier", "database")
			.and()
				.withNewSpec()
					.withType("ClusterIP")
					.withClusterIP("192.168.1.123")
			.addNewPort()
				.withPort(80).withProtocol("TCP")
				.endPort()
			.and()
			.build();

		Service stagingFrontend = new ServiceBuilder()
			.withNewMetadata()
				.withName("service4")
				.withNamespace(namespace)
				.addToLabels("environment", "staging")
				.addToLabels("tier", "frontend")
			.and()
				.withNewSpec()
					.withType("ClusterIP")
					.withClusterIP("192.168.1.124")
			.addNewPort()
				.withPort(80).withProtocol("TCP")
				.endPort()
			.and()
			.build();

		ServiceList prodServices = new ServiceList();
		prodServices.getItems().add(prodFrontend);
		prodServices.getItems().add(prodBackend);
		prodServices.getItems().add(prodDatabase);

		ServiceList prodServicesWithoutDatabase = new ServiceList();
		prodServicesWithoutDatabase.getItems().add(prodFrontend);
		prodServicesWithoutDatabase.getItems().add(prodBackend);

		ServiceList singleStagingFrontend = new ServiceList();
		singleStagingFrontend.getItems().add(stagingFrontend);

		server.expect()
			.withPath("/api/v1/namespaces/test/services?labelSelector=environment%3Dproduction")
			.andReturn(200, prodServices).always();

		server.expect()
			.withPath("/api/v1/namespaces/test/services?labelSelector=environment%3Dproduction,tier!%3Ddatabase")
			.andReturn(200, prodServicesWithoutDatabase).always();

		// swap multi-selectors
		server.expect()
			.withPath("/api/v1/namespaces/test/services?labelSelector=tier!%3Ddatabase,environment%3Dproduction")
			.andReturn(200, prodServicesWithoutDatabase).always();

		server.expect()
			.withPath("/api/v1/namespaces/test/services?labelSelector=environment%3Dstaging,tier%3Dfrontend")
			.andReturn(200, singleStagingFrontend).always();

		// swap multi-selectors
		server.expect()
			.withPath("/api/v1/namespaces/test/services?labelSelector=tier%3Dfrontend,environment%3Dstaging")
			.andReturn(200, singleStagingFrontend).always();
	}

	@Test
	public void getInstancesHavingMatchingLabels()
	{
		Map<String, String> matchingLabels = new HashMap<>();
		matchingLabels.put("environment", "production");

		List<ServiceInstance> instances = discoveryClient.selectInstances(matchingLabels);

		assertThat(instances).hasSize(3);
		for(ServiceInstance serviceInstance : instances) {
			assertThat(serviceInstance.getMetadata()).containsAllEntriesOf(matchingLabels);
		}
	}

	@Test
	public void getInstancesHavingBothMatchingAndNotMatchingLabels()
	{
		Map<String, String> matchingLabels = new HashMap<>();
		matchingLabels.put("environment", "production");

		Map<String, String> nonMatchingLabels = new HashMap<>();
		nonMatchingLabels.put("tier", "database");

		List<ServiceInstance> instances = discoveryClient.selectInstances(matchingLabels, nonMatchingLabels);

		assertThat(instances).hasSize(2);
		for(ServiceInstance serviceInstance : instances) {
			assertThat(serviceInstance.getMetadata()).containsAllEntriesOf(matchingLabels);
			for(Map.Entry<String, String> nonMatchingLabel : nonMatchingLabels.entrySet()) {
				assertThat(serviceInstance.getMetadata()).doesNotContain(nonMatchingLabel);
			}
		}
	}

	@Test
	public void getInstancesHavingExactlyMatchingLabels()
	{
		Map<String, String> matchingLabels = new HashMap<>();
		matchingLabels.put("environment", "staging");
		matchingLabels.put("tier", "frontend");

		List<ServiceInstance> instances = discoveryClient.selectInstances(matchingLabels);

		assertThat(instances).hasSize(1);
		for(ServiceInstance serviceInstance : instances) {
			assertThat(serviceInstance.getMetadata()).containsAllEntriesOf(matchingLabels);
		}
	}

}
