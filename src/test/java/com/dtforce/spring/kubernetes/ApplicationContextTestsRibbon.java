package com.dtforce.spring.kubernetes;

import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.dtforce.spring.kubernetes.api.IServiceInstanceExtractor;
import com.dtforce.spring.kubernetes.api.SelectorEnabledDiscoveryClient;
import com.dtforce.spring.kubernetes.discovery.KubernetesServerList;

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
	private KubernetesServerList kubernetesServerList;

	@Test
	public void contextLoads() {
		assert kubernetesClient != null;
		assert serviceInstanceExtractor != null;
		assert discoveryClient == null;
		assert kubernetesServerList != null;
	}

}
