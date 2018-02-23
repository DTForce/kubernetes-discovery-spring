package com.dtforce.spring.kubernetes;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@ConditionalOnProperty(name = "spring.kubernetes.discovery.enabled", havingValue="true")
public class KubernetesDiscoveryClient implements DiscoveryClient
{
	// TODO: namespace detection

	private KubernetesClient kubeClient;

	public KubernetesDiscoveryClient()
	{
		kubeClient = new DefaultKubernetesClient();
	}

	@Override
	public String description()
	{
		return "Kubernetes Service Discovery for Spring Cloud";
	}

	@Override
	public List<ServiceInstance> getInstances(String serviceId)
	{
		Service service = kubeClient.services().withName(serviceId).get();

		if (service != null) {
			// TODO: support multiple ports
			ServicePort svcPort = service.getSpec().getPorts().get(0);
			if (svcPort == null) {
				return Collections.emptyList();
			}

			String svcName = service.getMetadata().getName();
			String svcHost = service.getSpec().getClusterIP();
			int svcPortNumber = svcPort.getPort();

			List<ServiceInstance> serviceInstances = new ArrayList<>();
			serviceInstances.add(new DefaultServiceInstance(
				svcName, svcHost, svcPortNumber, false));
			return new ArrayList<ServiceInstance>();
		}

		return Collections.emptyList();
	}

	@Override
	public List<String> getServices()
	{
		ServiceList serviceList = kubeClient.services().list();

		if (serviceList != null) {
			List<String> serviceNames = new ArrayList<>();

			List<Service> items = serviceList.getItems();
			for (Service svc : items) {
				serviceNames.add(svc.getMetadata().getName());
			}

			return serviceNames;
		}

		return Collections.emptyList();
	}

}
