package com.trainerworkloadservice.integration;

import com.trainerworkloadservice.integration.config.IntegrationTestConfig;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/features/integration",glue = {
        "com.trainerworkloadservice.integration.steps", "com.trainerworkloadservice.integration.config"},plugin = {
                "pretty", "html:target/cucumber-reports/integration-tests"})
@ContextConfiguration(classes = IntegrationTestConfig.class)
@ActiveProfiles("test")
public class CucumberIntegrationTest {
}
