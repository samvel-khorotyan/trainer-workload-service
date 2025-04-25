package com.trainerworkloadservice.config;

import static org.mockito.Mockito.mock;

import com.trainerworkloadservice.TrainerWorkload.application.TrainerWorkloadService;
import com.trainerworkloadservice.TrainerWorkload.application.factory.TrainerWorkloadFactory;
import com.trainerworkloadservice.TrainerWorkload.application.port.output.LoadTrainerWorkloadPort;
import com.trainerworkloadservice.TrainerWorkload.application.port.output.UpdateTrainerWorkloadPort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class ServiceTestConfig {
	@Bean
	@Primary
	public LoadTrainerWorkloadPort loadTrainerWorkloadPort() {
		return mock(LoadTrainerWorkloadPort.class);
	}

	@Bean
	@Primary
	public UpdateTrainerWorkloadPort updateTrainerWorkloadPort() {
		return mock(UpdateTrainerWorkloadPort.class);
	}

	@Bean
	@Primary
	public TrainerWorkloadFactory trainerWorkloadFactory() {
		return new TrainerWorkloadFactory();
	}

	@Bean
	@Primary
	public TrainerWorkloadService trainerWorkloadService() {
		return new TrainerWorkloadService(loadTrainerWorkloadPort(), updateTrainerWorkloadPort(),
		        trainerWorkloadFactory());
	}
}
