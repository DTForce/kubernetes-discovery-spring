package com.dtforce.spring.kubernetes;

import com.netflix.loadbalancer.Server;
import io.fabric8.kubernetes.api.model.Service;

public class KubernetesEnabledServer extends Server
{
	private Service kubeService;

	private MetaInfo serviceInfo;

	public KubernetesEnabledServer(Service service)
	{
		super(
			service.getSpec().getClusterIP(),
			service.getSpec().getPorts().get(0).getPort()
		);
		this.kubeService = service;
		this.serviceInfo = new MetaInfo()
		{
			@Override
			public String getAppName()
			{
				return null;
			}

			@Override
			public String getServerGroup()
			{
				return service.getMetadata().getNamespace();
			}

			@Override
			public String getServiceIdForDiscovery()
			{
				return service.getMetadata().getName();
			}

			@Override
			public String getInstanceId()
			{
				return null;
			}
		};
	}

	public Service getKubeService()
	{
		return kubeService;
	}

	@Override
	public MetaInfo getMetaInfo()
	{
		return serviceInfo;
	}

	@Override
	public String toString()
	{
		return String.format("%s -> %s:%d", this.getMetaInfo().getServiceIdForDiscovery(),
			this.getHost(), this.getPort());
	}
}
