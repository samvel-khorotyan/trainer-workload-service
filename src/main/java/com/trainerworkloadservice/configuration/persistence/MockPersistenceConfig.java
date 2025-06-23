package com.trainerworkloadservice.configuration.persistence;

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "app.persistence.mode",havingValue = "mock",matchIfMissing = true)
public class MockPersistenceConfig {

	@PostConstruct
	public void init() {
		log.warn("=".repeat(80));
		log.warn("MOCK PERSISTENCE MODE ENABLED");
		log.warn("Data will be stored in memory only!");
		log.warn("This is for development/testing purposes only.");
		log.warn("=".repeat(80));
	}
}
