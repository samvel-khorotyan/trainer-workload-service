package com.trainerworkloadservice.configuration.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.persistence.mode",havingValue = "mock",matchIfMissing = true)
public class MockPersistenceHealthIndicator implements HealthIndicator {

	@Override
	public Health health() {
		return Health.up().withDetail("mode", "mock").withDetail("storage", "in-memory")
		        .withDetail("warning", "Data will be lost on restart").build();
	}
}
