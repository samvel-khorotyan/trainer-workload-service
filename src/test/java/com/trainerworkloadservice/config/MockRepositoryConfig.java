package com.trainerworkloadservice.config;

import static org.mockito.Mockito.mock;

import com.trainerworkloadservice.TrainerWorkload.adapter.output.persistence.TrainerWorkloadPersistenceRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class MockRepositoryConfig {
	@Bean
	@Primary
	public TrainerWorkloadPersistenceRepository trainerWorkloadPersistenceRepository() {
		return mock(TrainerWorkloadPersistenceRepository.class);
	}
}
