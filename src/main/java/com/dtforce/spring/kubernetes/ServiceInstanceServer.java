package com.dtforce.spring.kubernetes;

import com.netflix.loadbalancer.Server;
import org.springframework.cloud.client.ServiceInstance;

public class ServiceInstanceServer extends Server
{
	private ServiceInstance serviceInstance;

	public ServiceInstanceServer(ServiceInstance instance)
	{
		super(
			instance.getHost(),
			instance.getPort()
		);
		this.serviceInstance = instance;
	}

	public ServiceInstance getServiceInstance()
	{
		return serviceInstance;
	}

	@Override
	public String toString()
	{
		return String.format("%s -> %s:%d", this.serviceInstance.getServiceId(), this.getHost(), this.getPort());
	}
}
