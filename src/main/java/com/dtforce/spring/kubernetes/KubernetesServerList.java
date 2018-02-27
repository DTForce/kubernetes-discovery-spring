package com.dtforce.spring.kubernetes;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import com.netflix.loadbalancer.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KubernetesServerList extends AbstractServerList<Server>
{
	private static Logger log = LoggerFactory.getLogger(KubernetesServerList.class.getName());

	private String serviceId;

	private KubernetesDiscoveryClient discoveryClient;

	public KubernetesServerList(KubernetesDiscoveryClient client)
	{
		discoveryClient = client;
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
		List<ServiceInstance> serviceInstances = discoveryClient.getInstances(serviceId);
		for (ServiceInstance serviceInstance : serviceInstances) {
			servers.add(new ServiceInstanceServer(serviceInstance));
		}
		log.debug("getUpdatedListOfServers: updated servers list = {}", servers.toString());
		return servers;
	}

	@Override
	public void initWithNiwsConfig(IClientConfig clientConfig)
	{
		serviceId = clientConfig.getClientName();
	}
}
