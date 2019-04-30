package com.dtforce.spring.kubernetes.discovery;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import com.dtforce.spring.kubernetes.api.IServiceInstanceExtractor;
import com.dtforce.spring.kubernetes.api.SelectorEnabledDiscoveryClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;

public class KubernetesDiscoveryClient implements DiscoveryClient, SelectorEnabledDiscoveryClient
{

	private static Logger log = LoggerFactory.getLogger(KubernetesDiscoveryClient.class.getName());

	@NotNull
	private final KubernetesClient kubeClient;

	@NotNull
	private final IServiceInstanceExtractor serviceInstanceExtractor;

	public KubernetesDiscoveryClient(
		final KubernetesClient client,
		final IServiceInstanceExtractor serviceInstanceExtractor
	)
	{
		kubeClient = client;
		this.serviceInstanceExtractor = serviceInstanceExtractor;
	}

	@Override
	public String description()
	{
		return "Kubernetes Service Discovery for Spring Cloud";
	}

	@Override
	public List<ServiceInstance> getInstances(String serviceId)
	{
		return getInstancesFromService(fetchService(serviceId));
	}

	@Override
	public List<String> getServices()
	{
		ServiceList serviceList;
		try {
			serviceList = kubeClient.services().list();
		} catch (KubernetesClientException e) {
			log.error("getServices: failed to retrieve the list of services: API call failed. " +
				"Check your K8s client configuration and account permissions.");
			throw e;
		}

		List<Service> services = serviceList.getItems();

		if (log.isDebugEnabled()) {
			log.debug("Services retrieved - {}", services);
		}

		return services.stream()
			.map(s -> s.getMetadata().getName())
			.collect(Collectors.toList());
	}

	@Override
	public List<ServiceInstance> selectInstances(Map<String, String> match)
	{
		return selectInstances(match, Collections.emptyMap());
	}

	@Override
	public List<ServiceInstance> selectInstances(Map<String, String> match, Map<String, String> doNotMatch)
	{
		ServiceList serviceList;
		try {
			serviceList = kubeClient.services().withLabels(match).withoutLabels(doNotMatch).list();
		} catch(KubernetesClientException e) {
			log.error("selectInstances: failed to retrieve matching services: API call failed. " +
				"Check your K8s client configuration and account permissions.");
			throw e;
		}

		// serviceList is never supposed to be null, even if the query has no results
		if (serviceList == null) {
			log.error("selectInstances: service list is null");
			return Collections.emptyList();
		}

		return serviceInstanceExtractor.getInstancesFromServiceList(serviceList.getItems());
	}

	private List<ServiceInstance> getInstancesFromService(Service service)
	{
		if (service == null) {
			return Collections.emptyList();
		}
		return serviceInstanceExtractor.getInstancesFromServiceList(Collections.singletonList(service));
	}

	private Service fetchService(String name)
	{
		try {
			return kubeClient.services().withName(name).get();
		} catch(KubernetesClientException e) {
			log.error("getInstances: failed to retrieve service '{}': API call failed. " +
				"Check your K8s client configuration and account permissions.", name);
			throw e;
		}
	}


}
