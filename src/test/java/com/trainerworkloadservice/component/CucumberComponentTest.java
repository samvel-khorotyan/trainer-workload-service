package com.trainerworkloadservice.component;

import com.trainerworkloadservice.component.config.ComponentTestConfig;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/features/component",glue = {
        "com.trainerworkloadservice.component.steps", "com.trainerworkloadservice.component.config"},plugin = {"pretty",
                "html:target/cucumber-reports/component-tests"})
@ContextConfiguration(classes = ComponentTestConfig.class)
@ActiveProfiles("test")
public class CucumberComponentTest {
}
