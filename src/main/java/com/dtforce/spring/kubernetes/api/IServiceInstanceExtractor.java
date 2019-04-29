package com.dtforce.spring.kubernetes.api;

import io.fabric8.kubernetes.api.model.Service;
import org.springframework.cloud.client.ServiceInstance;

import java.util.List;

public interface IServiceInstanceExtractor
{
	List<ServiceInstance> getInstancesFromServiceList(List<Service> services);
}
