package com.dtforce.spring.kubernetes;

import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.test.context.junit4.SpringRunner;

import com.dtforce.spring.kubernetes.api.IServiceInstanceExtractor;
import com.dtforce.spring.kubernetes.api.SelectorEnabledDiscoveryClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestConfiguration.class}, properties = {
	"spring.cloud.kubernetes.enabled=true",
	"spring.cloud.kubernetes.discovery.enabled=true"
})
public class ApplicationContextTestsDiscovery
{

	@Autowired(required = false)
	private KubernetesClient kubernetesClient;

	@Autowired(required = false)
	private IServiceInstanceExtractor serviceInstanceExtractor;

	@Autowired(required = false)
	private SelectorEnabledDiscoveryClient discoveryClient;

	@Test
	public void contextLoads() {
		assert kubernetesClient != null;
		assert serviceInstanceExtractor != null;
		assert discoveryClient != null;

		final List<ServiceInstance> instances = discoveryClient.getInstances("test-service");
		assertThat(instances).hasSize(1);
		assertThat(instances.get(0).getPort()).isEqualTo(8080);
		assertThat(instances.get(0).getHost()).isEqualTo("192.168.1.121");
	}

}
