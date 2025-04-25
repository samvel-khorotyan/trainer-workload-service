package com.trainerworkloadservice.config;

import static org.mockito.Mockito.mock;

import com.trainerworkloadservice.TrainerWorkload.adapter.output.persistence.TrainerWorkloadPersistenceRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;

@TestConfiguration
@Profile("test")
public class MessageTestConfig {
	@Bean
	@Primary
	public JmsTemplate jmsTemplate() {
		return mock(JmsTemplate.class);
	}

	@Bean
	@Primary
	public TrainerWorkloadPersistenceRepository trainerWorkloadPersistenceRepository() {
		return mock(TrainerWorkloadPersistenceRepository.class);
	}
}
