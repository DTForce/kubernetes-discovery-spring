package com.dtforce.spring.kubernetes;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.List;
import java.util.Map;

public interface SelectorEnabledDiscoveryClient extends DiscoveryClient
{

	/**
	 * Get instances having the specified metadata labels (equality-based query)
	 * @param match pairs - key is the name of the label and value is the value
	 * @return List of service instances matching the query
	 */
	List<ServiceInstance> selectInstances(Map<String, String> match);


	/**
	 * Get instances having the metadata labels specified in {@code match} but not
	 * the ones specified in {@code doNotMatch}
	 * @param match pairs - key is the name of the label and value is the value
	 * @param doNotMatch pairs - key is the name of the label and value is the value
	 * @return List of service instances matching the query
	 */
	List<ServiceInstance> selectInstances(Map<String, String> match, Map<String, String> doNotMatch);

}
