package com.dtforce.spring.kubernetes;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KubernetesDiscoveryClient implements DiscoveryClient
{
	// TODO: namespace detection

	private KubernetesClient kubeClient;

	public KubernetesDiscoveryClient()
	{
		this(new DefaultKubernetesClient());
	}

	public KubernetesDiscoveryClient(KubernetesClient client)
	{
		kubeClient = client;
	}

	@Override
	public String description()
	{
		return "Kubernetes Service Discovery for Spring Cloud";
	}

	@Override
	public List<ServiceInstance> getInstances(String serviceId)
	{
		Service service;
		try {
			service = kubeClient.services().withName(serviceId).get();
		} catch(KubernetesClientException e) {
			return Collections.emptyList();
		}

		// TODO: support multiple ports
		ServicePort svcPort = service.getSpec().getPorts().get(0);
		if (svcPort == null) {
			return Collections.emptyList();
		}

		List<ServiceInstance> serviceInstances = new ArrayList<>();
		serviceInstances.add(new DefaultServiceInstance(
			service.getMetadata().getName(),
			service.getSpec().getClusterIP(),
			svcPort.getPort(),
			false,
			service.getMetadata().getAnnotations()
		));
		return serviceInstances;
	}

	@Override
	public List<String> getServices()
	{
		ServiceList serviceList;
		try {
			serviceList = kubeClient.services().list();
		} catch (KubernetesClientException e) {
			return Collections.emptyList();
		}

		List<String> serviceNames = new ArrayList<>();
		List<Service> items = serviceList.getItems();
		for (Service svc : items) {
			serviceNames.add(svc.getMetadata().getName());
		}
		return serviceNames;
	}

}
