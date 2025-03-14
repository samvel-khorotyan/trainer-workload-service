package com.trainerworkloadservice.unit.TrainerWorkload.application.factory;

import static org.junit.jupiter.api.Assertions.*;

import com.trainerworkloadservice.TrainerWorkload.application.factory.TrainerWorkloadFactory;
import com.trainerworkloadservice.TrainerWorkload.application.port.input.ProcessTrainerWorkloadCommand;
import com.trainerworkloadservice.TrainerWorkload.domain.ActionType;
import com.trainerworkloadservice.TrainerWorkload.domain.TrainerWorkload;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TrainerWorkloadFactoryTest {
	private TrainerWorkloadFactory factory;
	private String username;
	private String firstName;
	private String lastName;
	private boolean isActive;
	private LocalDate trainingDate;
	private int trainingDuration;
	private String transactionId;

	@BeforeEach
	void setUp() {
		factory = new TrainerWorkloadFactory();
		username = "trainer.username";
		firstName = "John";
		lastName = "Doe";
		isActive = true;
		trainingDate = LocalDate.of(2023, 5, 15);
		trainingDuration = 60;
		transactionId = "transaction-123";
	}

	@Test
	void createFrom_ShouldCreateTrainerWorkload_WithCorrectValues() {
		ProcessTrainerWorkloadCommand command = ProcessTrainerWorkloadCommand.builder().username(username)
		        .firstName(firstName).lastName(lastName).isActive(isActive).trainingDate(trainingDate)
		        .trainingDuration(trainingDuration).actionType(ActionType.ADD).transactionId(transactionId).build();

		TrainerWorkload result = factory.createFrom(command);

		assertNotNull(result);
		assertEquals(username, result.getUsername());
		assertEquals(firstName, result.getFirstName());
		assertEquals(lastName, result.getLastName());
		assertEquals(isActive, result.getIsActive());
	}

	@Test
	void createFrom_ShouldHandleNullValues_InCommand() {
		ProcessTrainerWorkloadCommand command = ProcessTrainerWorkloadCommand.builder().username(null).firstName(null)
		        .lastName(null).isActive(null).trainingDate(null).trainingDuration(null).actionType(null)
		        .transactionId(null).build();

		TrainerWorkload result = factory.createFrom(command);

		assertNotNull(result);
		assertNull(result.getUsername());
		assertNull(result.getFirstName());
		assertNull(result.getLastName());
		assertNull(result.getIsActive());
	}

	@Test
	void createFrom_ShouldIgnoreTrainingFields_NotUsedInTrainerWorkload() {
		ProcessTrainerWorkloadCommand command1 = ProcessTrainerWorkloadCommand.builder().username(username)
		        .firstName(firstName).lastName(lastName).isActive(isActive).trainingDate(trainingDate)
		        .trainingDuration(trainingDuration).actionType(ActionType.ADD).transactionId(transactionId).build();

		ProcessTrainerWorkloadCommand command2 = ProcessTrainerWorkloadCommand.builder().username(username)
		        .firstName(firstName).lastName(lastName).isActive(isActive).trainingDate(LocalDate.of(2024, 1, 1))
		        .trainingDuration(120).actionType(ActionType.DELETE).transactionId("different-transaction").build();

		TrainerWorkload result1 = factory.createFrom(command1);
		TrainerWorkload result2 = factory.createFrom(command2);

		assertEquals(result1.getUsername(), result2.getUsername());
		assertEquals(result1.getFirstName(), result2.getFirstName());
		assertEquals(result1.getLastName(), result2.getLastName());
		assertEquals(result1.getIsActive(), result2.getIsActive());
	}

	@Test
	void createFrom_ShouldHandleEmptyStrings() {
		ProcessTrainerWorkloadCommand command = ProcessTrainerWorkloadCommand.builder().username("").firstName("")
		        .lastName("").isActive(isActive).trainingDate(trainingDate).trainingDuration(trainingDuration)
		        .actionType(ActionType.ADD).transactionId(transactionId).build();

		TrainerWorkload result = factory.createFrom(command);

		assertNotNull(result);
		assertEquals("", result.getUsername());
		assertEquals("", result.getFirstName());
		assertEquals("", result.getLastName());
		assertEquals(isActive, result.getIsActive());
	}
}
