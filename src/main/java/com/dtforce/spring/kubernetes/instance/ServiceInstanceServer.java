package com.dtforce.spring.kubernetes.instance;

import com.netflix.loadbalancer.Server;
import org.springframework.cloud.client.ServiceInstance;

public class ServiceInstanceServer extends Server
{
	private final String serviceId;

	public ServiceInstanceServer(ServiceInstance instance)
	{
		super(
			instance.getHost(),
			instance.getPort()
		);
		this.serviceId = instance.getServiceId();
	}

	@Override
	public String toString()
	{
		return String.format("%s -> %s:%d", this.serviceId, this.getHost(), this.getPort());
	}

}
