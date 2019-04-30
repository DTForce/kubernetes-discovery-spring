package com.dtforce.spring.kubernetes.instance;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;

import com.dtforce.spring.kubernetes.discovery.KubernetesDiscoveryClient;
import com.dtforce.spring.kubernetes.api.IServiceInstanceExtractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class DefaultInstanceExtractor implements IServiceInstanceExtractor
{

	private static Logger log = LoggerFactory.getLogger(KubernetesDiscoveryClient.class.getName());

	private final String defaultPortName;

	public DefaultInstanceExtractor(final String defaultPortName)
	{
		this.defaultPortName = defaultPortName;
	}

	public List<ServiceInstance> getInstancesFromServiceList(List<Service> services)
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

}
