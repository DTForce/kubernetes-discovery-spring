package com.dtforce.spring.kubernetes;

import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ClientConfigConfiguration
{
	@Bean
	@Primary
	IClientConfig clientConfig()
	{
		return new DefaultClientConfigImpl();
	}

}
