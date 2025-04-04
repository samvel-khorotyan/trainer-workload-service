package com.trainerworkloadservice.configuration.messaging;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;

@Configuration
public class ActiveMQConfig {
	@Value("${spring.activemq.broker-url}")
	private String brokerUrl;

	@Value("${spring.activemq.user}")
	private String username;

	@Value("${spring.activemq.password}")
	private String password;

	@Bean
	public ActiveMQConnectionFactory activeMQConnectionFactory() {
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
		factory.setBrokerURL(brokerUrl);
		factory.setUserName(username);
		factory.setPassword(password);

		RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
		redeliveryPolicy.setMaximumRedeliveries(3);
		redeliveryPolicy.setInitialRedeliveryDelay(1000);
		redeliveryPolicy.setRedeliveryDelay(2000);
		redeliveryPolicy.setBackOffMultiplier(2);
		redeliveryPolicy.setUseExponentialBackOff(true);
		factory.setRedeliveryPolicy(redeliveryPolicy);

		return factory;
	}

	@Bean
	public CachingConnectionFactory cachingConnectionFactory() {
		return new CachingConnectionFactory(activeMQConnectionFactory());
	}
}
