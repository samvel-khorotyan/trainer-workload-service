package com.trainerworkloadservice.component.config;

import com.trainerworkloadservice.TrainerWorkload.adapter.output.persistence.TrainerWorkloadRepository;
import com.trainerworkloadservice.TrainerWorkload.application.TrainerWorkloadService;
import com.trainerworkloadservice.TrainerWorkload.application.factory.TrainerWorkloadFactory;
import com.trainerworkloadservice.TrainerWorkload.application.port.output.LoadTrainerWorkloadPort;
import com.trainerworkloadservice.TrainerWorkload.application.port.output.UpdateTrainerWorkloadPort;
import com.trainerworkloadservice.config.TestApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(classes = TestApplication.class,properties = {"spring.main.allow-bean-definition-overriding=true",
        "eureka.client.enabled=false", "spring.cloud.discovery.enabled=false",
        "spring.data.mongodb.auto-index-creation=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration,org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration"})
@ActiveProfiles("test")
public class ComponentTestConfig {
	@MockBean
	private TrainerWorkloadRepository repository;

	@Configuration
	static class TestConfig {
		@Bean
		@Primary
		public LoadTrainerWorkloadPort loadTrainerWorkloadPort() {
			return org.mockito.Mockito.mock(LoadTrainerWorkloadPort.class);
		}

		@Bean
		@Primary
		public UpdateTrainerWorkloadPort updateTrainerWorkloadPort() {
			return org.mockito.Mockito.mock(UpdateTrainerWorkloadPort.class);
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
}
