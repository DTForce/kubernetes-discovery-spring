package com.dtforce.spring.kubernetes;

import com.netflix.loadbalancer.ILoadBalancer;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.test.context.junit4.SpringRunner;

import com.dtforce.spring.kubernetes.api.IServiceInstanceExtractor;
import com.dtforce.spring.kubernetes.api.SelectorEnabledDiscoveryClient;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestConfiguration.class}, properties = {
	"spring.cloud.kubernetes.enabled=true",
	"spring.cloud.kubernetes.ribbon.enabled=true"
})
public class ApplicationContextTestsRibbon
{

	@Autowired(required = false)
	private KubernetesClient kubernetesClient;

	@Autowired(required = false)
	private IServiceInstanceExtractor serviceInstanceExtractor;

	@Autowired(required = false)
	private SelectorEnabledDiscoveryClient discoveryClient;

	@Autowired(required = false)
	private SpringClientFactory clientFactory;


	@Test
	public void contextLoads() {
		assert kubernetesClient != null;
		assert serviceInstanceExtractor != null;
		assert discoveryClient == null;
		assert clientFactory != null;

		ILoadBalancer loadBalancer = clientFactory.getLoadBalancer("test-service");
		assertThat(loadBalancer.getAllServers()).hasSize(1);
		assertThat(loadBalancer.getAllServers().get(0).toString()).isEqualTo("test-service -> 192.168.1.121:8080");
	}

}
