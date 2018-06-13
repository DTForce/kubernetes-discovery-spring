package com.dtforce.spring.kubernetes;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class KubernetesDiscoveryClient implements DiscoveryClient, SelectorEnabledDiscoveryClient
{

	private class ServiceCacheLoader extends CacheLoader<String, Service>
	{
		private KubernetesClient k8s;

		public ServiceCacheLoader(KubernetesClient k8s)
		{
			this.k8s = k8s;
		}

		@Override
		public Service load(String key) throws Exception
		{
			try {
				return kubeClient.services().withName(key).get();
			} catch(KubernetesClientException e) {
				log.error("getInstances: failed to retrieve service '{}': API call failed. " +
					"Check your K8s client configuration and account permissions.", key);
				throw e;
			}
		}
	}

	private static final String defaultPortName = "http";

	private static Logger log = LoggerFactory.getLogger(KubernetesDiscoveryClient.class.getName());

	private KubernetesClient kubeClient;

	private LoadingCache<String, Service> serviceCache;

	public KubernetesDiscoveryClient(KubernetesClient client)
	{
		kubeClient = client;

		serviceCache = CacheBuilder.newBuilder()
			.maximumSize(10000)
			.expireAfterWrite(5, TimeUnit.MINUTES)
			.build(new ServiceCacheLoader(kubeClient));
	}

	@Override
	public String description()
	{
		return "Kubernetes Service Discovery for Spring Cloud";
	}

	@Override
	public List<ServiceInstance> getInstances(String serviceId)
	{
		Service service = serviceCache.getUnchecked(serviceId);

		if (service == null) {
			log.warn("getInstances: specified service '{}' doesn't exist", serviceId);
			return Collections.emptyList();
		}

		return getInstancesFromService(service);
	}

	@Override
	public List<String> getServices()
	{
		// TODO caching

		ServiceList serviceList;
		try {
			serviceList = kubeClient.services().list();
		} catch (KubernetesClientException e) {
			log.error("getServices: failed to retrieve the list of services: API call failed. " +
				"Check your K8s client configuration and account permissions.");
			throw e;
		}

		List<String> serviceNames = new ArrayList<>();
		List<Service> items = serviceList.getItems();
		for (Service svc : items) {
			serviceNames.add(svc.getMetadata().getName());
		}
		return serviceNames;
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

		return getInstancesFromServiceList(serviceList.getItems());
	}

	private List<ServiceInstance> getInstancesFromService(Service service)
	{
		return getInstancesFromServiceList(Collections.singletonList(service));
	}

	private List<ServiceInstance> getInstancesFromServiceList(List<Service> services)
	{
		if (log.isDebugEnabled()) {
			log.debug("getInstancesFromServiceList: services = {}", services.toString());
		}

		List<ServiceInstance> serviceInstances = new ArrayList<>();
		for (Service service : services) {
			if (service.getSpec() == null) {
				log.error("skipping service with no spec");
				continue;
			}

			String serviceName = service.getMetadata().getName();
			List<ServicePort> servicePorts = service.getSpec().getPorts();

			if (servicePorts.isEmpty()) {
				log.error("service '{}' has no ports", serviceName);
				continue;
			}

			ServicePort svcPort;
			if (servicePorts.size() > 1) {
				Optional<ServicePort> httpPort = servicePorts.stream()
					.filter(s -> s.getName() != null && s.getName().equals(defaultPortName))
					.findFirst();

				if (httpPort.isPresent()) {
					svcPort = httpPort.get();
				} else {
					log.warn("getInstancesFromServiceList: multiple ports detected in '{}' " +
						"and named default port '{}' not found. Falling back to first port available.",
						serviceName, defaultPortName);
					svcPort = servicePorts.get(0);
				}
			} else {
				svcPort = servicePorts.get(0);
			}

			assert svcPort != null;

			serviceInstances.add(new DefaultServiceInstance(
				service.getMetadata().getName(),
				service.getSpec().getClusterIP(),
				svcPort.getPort(),
				false,
				service.getMetadata().getLabels()
			));
		}
		return serviceInstances;
	}

}
