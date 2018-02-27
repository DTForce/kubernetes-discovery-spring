package com.dtforce.spring.kubernetes;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.Server;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KubernetesServerList extends AbstractServerList<Server>
{
	private static Logger log = LoggerFactory.getLogger(KubernetesServerList.class.getName());

	private String serviceId;

	private KubernetesClient kubeClient;

	public KubernetesServerList(KubernetesClient client)
	{
		kubeClient = client;
	}

	@Override
	public List<Server> getInitialListOfServers()
	{
		return getUpdatedListOfServers();
	}

	@Override
	public List<Server> getUpdatedListOfServers()
	{
		log.debug("getUpdatedListOfServers: requesting services list...");

		ServiceList serviceList;
		try {
			serviceList = kubeClient.services().list();
		} catch(KubernetesClientException e) {
			log.warn("getUpdatedListOfServers: unable to get a list of services: API call failed.");
			return Collections.emptyList();
		}

		log.debug("getUpdatedListOfServers: request success! serviceList = {}", serviceList.toString());

		List<Server> servers = new ArrayList<>();
		List<Service> items = serviceList.getItems();
		for (Service service : items) {
			servers.add(new KubernetesEnabledServer(service));
		}

		log.debug("getUpdatedListOfServers: updated servers list = {}", servers.toString());

		return servers;
	}

	@Override
	public void initWithNiwsConfig(IClientConfig clientConfig)
	{
		// No-op
	}
}
