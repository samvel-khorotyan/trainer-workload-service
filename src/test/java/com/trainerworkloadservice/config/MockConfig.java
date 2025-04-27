package com.trainerworkloadservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
@ComponentScan(basePackages = "com.trainerworkloadservice",excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX,pattern = "com\\.trainerworkloadservice\\.TrainerWorkload\\.adapter\\..*")})
public class MockConfig {
}
