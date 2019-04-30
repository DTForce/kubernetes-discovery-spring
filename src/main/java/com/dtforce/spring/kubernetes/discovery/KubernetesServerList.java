package com.dtforce.spring.kubernetes.discovery;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import com.netflix.loadbalancer.Server;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;

import com.dtforce.spring.kubernetes.api.IServiceInstanceExtractor;
import com.dtforce.spring.kubernetes.instance.ServiceInstanceServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.validation.constraints.NotNull;

public class KubernetesServerList extends AbstractServerList<Server>
{
	private static Logger log = LoggerFactory.getLogger(KubernetesServerList.class.getName());

	private String serviceId;

	@NotNull
	private final KubernetesClient kubernetesClient;

	@NotNull
	private final IServiceInstanceExtractor serviceInstanceExtractor;

	public KubernetesServerList(
		@NotNull final KubernetesClient kubernetesClient,
		@NotNull final IServiceInstanceExtractor serviceInstanceExtractor
	)
	{
		this.kubernetesClient = kubernetesClient;
		this.serviceInstanceExtractor = serviceInstanceExtractor;
	}

	public KubernetesServerList(
		@NotNull final KubernetesClient kubernetesClient,
		@NotNull final IServiceInstanceExtractor serviceInstanceExtractor,
		@NotNull final String serviceId
	)
	{
		this.kubernetesClient = kubernetesClient;
		this.serviceInstanceExtractor = serviceInstanceExtractor;
		this.serviceId = serviceId;
	}

	@Override
	public List<Server> getInitialListOfServers()
	{
		return getUpdatedListOfServers();
	}

	@Override
	public List<Server> getUpdatedListOfServers()
	{
		if (serviceId == null) {
			log.error("getUpdatedListOfServers: serviceId is null.");
			return Collections.emptyList();
		}

		List<Server> servers = new ArrayList<>();
		List<ServiceInstance> serviceInstances = serviceInstanceExtractor.getInstancesFromServiceList(
			Collections.singletonList(fetchService(serviceId))
		);
		for (ServiceInstance serviceInstance : serviceInstances) {
			servers.add(new ServiceInstanceServer(serviceInstance));
		}
		if (log.isDebugEnabled()) {
			log.debug("getUpdatedListOfServers: updated servers list = {}", servers.toString());
		}
		return servers;
	}

	@Override
	public void initWithNiwsConfig(IClientConfig clientConfig)
	{
		serviceId = clientConfig.getClientName();
	}

	private Service fetchService(String name)
	{
		try {
			return kubernetesClient.services().withName(name).get();
		} catch(KubernetesClientException e) {
			log.error("getInstances: failed to retrieve service '{}': API call failed. " +
				"Check your K8s client configuration and account permissions.", name);
			throw e;
		}
	}

}
