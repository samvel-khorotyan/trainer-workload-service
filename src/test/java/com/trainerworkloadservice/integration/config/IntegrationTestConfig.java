package com.trainerworkloadservice.integration.config;

import static org.mockito.Mockito.mock;

import com.trainerworkloadservice.config.MockMessageSender;
import com.trainerworkloadservice.config.ServiceTestConfig;
import com.trainerworkloadservice.config.TestApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(classes = {TestApplication.class, ServiceTestConfig.class,
        IntegrationTestConfig.TestConfig.class},properties = {"spring.main.allow-bean-definition-overriding=true",
                "eureka.client.enabled=false", "spring.cloud.discovery.enabled=false",
                "spring.data.mongodb.auto-index-creation=false",
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration,org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration"})
@ActiveProfiles("test")
@Import(ServiceTestConfig.class)
public class IntegrationTestConfig {

	@Configuration
	static class TestConfig {
		@Bean
		public JmsTemplate jmsTemplate() {
			return mock(JmsTemplate.class);
		}

		@Bean
		public MockMessageSender mockMessageSender() {
			return new MockMessageSender();
		}
	}
}
