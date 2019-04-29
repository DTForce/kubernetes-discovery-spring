package com.dtforce.spring.kubernetes;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class KubernetesDiscoveryClient implements DiscoveryClient, SelectorEnabledDiscoveryClient
{

	private static final String defaultPortName = "http";

	private static Logger log = LoggerFactory.getLogger(KubernetesDiscoveryClient.class.getName());

	private KubernetesClient kubeClient;

	private LoadingCache<String, List<ServiceInstance>> serviceCache;

	public KubernetesDiscoveryClient(
		final KubernetesClient client,
		final Duration cacheExpireAfterWrite,
		final Duration cacheRefreshAfterWrite,
		int maximumCacheSize
	)
	{
		kubeClient = client;

		serviceCache = Caffeine.newBuilder()
			.maximumSize(maximumCacheSize)
			.expireAfterWrite(cacheExpireAfterWrite)
			.refreshAfterWrite(cacheRefreshAfterWrite)
			.build(this::loadServiceInstances);
	}

	@Override
	public String description()
	{
		return "Kubernetes Service Discovery for Spring Cloud";
	}

	@Override
	public List<ServiceInstance> getInstances(String serviceId)
	{
		return serviceCache.get(serviceId);
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

		// Populate cache.
		// getServices mostly called manually and not by Ribbon, and
		// if getInstances is called right after it then we should
		// take advantage of the cache instead of calling
		// the API twice (1st call to list services, 2nd call to get info for one service)
		// as we already get everything we need in the list() call
		services.forEach((Service svc) -> {
			serviceCache.put(svc.getMetadata().getName(), getInstancesFromService(svc));
		});

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

		return getInstancesFromServiceList(serviceList.getItems());
	}

	private List<ServiceInstance> getInstancesFromService(Service service)
	{
		if (service == null) {
			return Collections.emptyList();
		}
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
				service.getMetadata().getName(),
				service.getSpec().getClusterIP(),
				svcPort.getPort(),
				false,
				service.getMetadata().getLabels()
			));
		}
		return serviceInstances;
	}

	private List<ServiceInstance> loadServiceInstances(String key)
	{
		Service service = fetchService(key);
		if (service != null) {
			log.info("Service cache loaded for {} - {}", service.getMetadata().getName(), service);
		}
		if (log.isDebugEnabled()) {
			printCacheStats();
		}
		return getInstancesFromService(service);
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

	private void printCacheStats()
	{
		CacheStats stats = serviceCache.stats();
		final String statsMsg = "\n=== Kubernetes Discovery - Cache Stats ===\n" +
			String.format(
				"=> [Cache Requests] total: %d | hits: %d (%.2f %%) | misses: %d (%.2f %%)\n",
				stats.requestCount(),
				stats.hitCount(),
				stats.hitRate(),
				stats.missCount(),
				stats.missRate()
			) +
			String.format("=> [Load Calls] total : %d | successes: %d | failures: %d | average load time : %.2f ms\n",
				stats.loadCount(),
				stats.loadSuccessCount(),
				stats.loadFailureCount(),
				(stats.averageLoadPenalty() / 1000000.0)
			) +
			String.format("=> [Other] eviction count: %d\n=== End of Cache Stats ===", stats.evictionCount());
		log.debug(statsMsg);
	}

}
