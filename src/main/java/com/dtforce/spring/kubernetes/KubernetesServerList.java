package com.dtforce.spring.kubernetes;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import com.netflix.loadbalancer.Server;
import io.fabric8.kubernetes.api.model.Service;
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
		if (serviceId == null) {
			log.error("getUpdatedListOfServers: serviceId is null.");
			return Collections.emptyList();
		}

		Service service;
		try {
			service = kubeClient.services().withName(serviceId).get();
		} catch(KubernetesClientException e) {
			log.warn("getUpdatedListOfServers: unable to get service '{}': API call failed.", serviceId);
			return Collections.emptyList();
		}

		List<Server> servers = new ArrayList<>();
		servers.add(new KubernetesEnabledServer(service));
		log.debug("getUpdatedListOfServers: updated servers list = {}", servers.toString());
		return servers;
	}

	@Override
	public void initWithNiwsConfig(IClientConfig clientConfig)
	{
		serviceId = clientConfig.getClientName();
	}
}
