package com.dtforce.spring.kubernetes;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.helpers.BasicMarker;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KubernetesDiscoveryClient implements DiscoveryClient
{
	// TODO: namespace detection

	private static Logger log = LoggerFactory.getLogger(KubernetesDiscoveryClient.class.getName());

	private KubernetesClient kubeClient;

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
		log.debug("getInstances: requesting info for service with id '{}' ...", serviceId);

		Service service;
		try {
			service = kubeClient.services().withName(serviceId).get();
		} catch(KubernetesClientException e) {
			log.warn("getInstances: failed to retrieve service '{}': API call failed.", serviceId);
			return Collections.emptyList();
		}

		log.debug("getInstances: request success!");

		if (service == null) {
			log.warn("getInstances: specified service '{}' doesn't exist", serviceId);
			return Collections.emptyList();
		}

		log.debug("getInstances: service = {}", service.toString());

		// TODO: support multiple ports
		ServicePort svcPort = service.getSpec().getPorts().get(0);
		if (svcPort == null) {
			log.warn("getInstances: service '{}' has no ports", serviceId);
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
		log.debug("getServices: requesting list of services...");

		ServiceList serviceList;
		try {
			serviceList = kubeClient.services().list();
		} catch (KubernetesClientException e) {
			log.warn("getServices: failed to retrieve the list of services: API call failed.");
			return Collections.emptyList();
		}

		log.debug("getServices: request success! serviceList = {}", serviceList.toString());

		List<String> serviceNames = new ArrayList<>();
		List<Service> items = serviceList.getItems();
		for (Service svc : items) {
			serviceNames.add(svc.getMetadata().getName());
		}
		return serviceNames;
	}

}
