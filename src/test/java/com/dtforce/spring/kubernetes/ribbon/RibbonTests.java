package com.dtforce.spring.kubernetes.ribbon;

import com.netflix.loadbalancer.Server;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.dtforce.spring.kubernetes.api.IServiceInstanceExtractor;
import com.dtforce.spring.kubernetes.discovery.KubernetesServerList;
import com.dtforce.spring.kubernetes.instance.DefaultInstanceExtractor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RibbonTests
{

	private final String serviceId = "dummy-service";

	private final String multiPortServiceId = "multiport-service";

	@Rule
	public KubernetesServer server = new KubernetesServer(true, true);

	private KubernetesClient kube;

	private IServiceInstanceExtractor serviceInstanceExtractor = new DefaultInstanceExtractor("http");


	@Before
	public void setUp()
	{
		kube = server.getClient();

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
	public void singlePortService()
	{
		KubernetesServerList serverList = new KubernetesServerList(
			kube,
			serviceInstanceExtractor,
			serviceId
		);
		List<Server> services = serverList.getInitialListOfServers();
		assertThat(services).hasSize(1);
		assertThat(services.get(0).getHostPort()).isEqualTo("192.168.1.120:80");
		assertThat(services.get(0).toString()).isEqualTo(serviceId + " -> 192.168.1.120:80");
	}

	@Test
	public void getInstancesForSpecifiedService()
	{
		KubernetesServerList serverList = new KubernetesServerList(
			kube,
			serviceInstanceExtractor,
			multiPortServiceId
		);
		List<Server> services = serverList.getInitialListOfServers();
		assertThat(services).hasSize(1);
		assertThat(services.get(0).getHostPort()).isEqualTo("192.168.1.121:8080");
	}

}
