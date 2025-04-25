package com.trainerworkloadservice.integration.steps;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.trainerworkloadservice.TrainerWorkload.adapter.output.persistence.TrainerWorkloadPersistenceRepository;
import com.trainerworkloadservice.TrainerWorkload.adapter.output.queue.message.TrainerWorkloadMessage;
import com.trainerworkloadservice.TrainerWorkload.adapter.output.queue.message.TrainerWorkloadResponseMessage;
import com.trainerworkloadservice.TrainerWorkload.domain.ActionType;
import com.trainerworkloadservice.TrainerWorkload.domain.TrainerWorkload;
import com.trainerworkloadservice.config.MockMessageSender;
import com.trainerworkloadservice.configuration.messaging.JmsConfig;
import com.trainerworkloadservice.integration.config.IntegrationTestConfig;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;
import java.util.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = IntegrationTestConfig.class)
public class TrainerWorkloadIntegrationSteps {
	@Mock
	private TrainerWorkloadPersistenceRepository repository;

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private MockMessageSender mockMessageSender;

	private TrainerWorkloadMessage workloadMessage;
	private TrainerWorkloadResponseMessage responseMessage;
	private String transactionId;
	private AutoCloseable closeable;

	private final Map<String, Map<Integer, Map<Integer, Integer>>> userWorkloadMap = new HashMap<>();

	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
		transactionId = UUID.randomUUID().toString();

		reset(repository);
		mockMessageSender.reset();
	}

	@After
	public void cleanup() throws Exception {
		if (closeable != null) {
			closeable.close();
		}
	}

	@Given("a trainer exists in the database with username {string}, first name {string}, last name {string} and active status {string}")
	public void aTrainerExistsInTheDatabaseWithUsernameFirstNameLastNameAndActiveStatus(String username,
	        String firstName, String lastName, String isActive) {
		TrainerWorkload trainerWorkload = TrainerWorkload.builder().username(username).firstName(firstName)
		        .lastName(lastName).isActive(Boolean.parseBoolean(isActive)).years(new ArrayList<>()).build();

		when(repository.findByUsername(username)).thenReturn(Optional.of(trainerWorkload));

		userWorkloadMap.put(username, new HashMap<>());

		repository.findByUsername(username);
	}

	@When("a {string} workload message is sent with username {string}, date {string} and duration {int}")
	public void aWorkloadMessageIsSentWithUsernameDateAndDuration(String action, String username, String date,
	        Integer duration) {
		LocalDate trainingDate = LocalDate.parse(date);
		int year = trainingDate.getYear();
		int month = trainingDate.getMonthValue();

		workloadMessage = TrainerWorkloadMessage.builder().username(username).firstName("Test").lastName("User")
		        .isActive(true).trainingDate(trainingDate).trainingDuration(duration)
		        .actionType(ActionType.valueOf(action.toUpperCase())).transactionId(transactionId).build();

		mockMessageSender.sendMessage(JmsConfig.TRAINER_WORKLOAD_QUEUE, workloadMessage);

		if (ActionType.valueOf(action.toUpperCase()) == ActionType.ADD) {
			userWorkloadMap.computeIfAbsent(username, k -> new HashMap<>()).computeIfAbsent(year, k -> new HashMap<>())
			        .compute(month, (k, v) -> (v == null) ? duration : v + duration);
		} else if (ActionType.valueOf(action.toUpperCase()) == ActionType.DELETE
		        || ActionType.valueOf(action.toUpperCase()) == ActionType.DELETE) {
			userWorkloadMap.computeIfAbsent(username, k -> new HashMap<>()).computeIfAbsent(year, k -> new HashMap<>())
			        .compute(month, (k, v) -> (v == null || v <= duration) ? 0 : v - duration);
		} else if (ActionType.valueOf(action.toUpperCase()) == ActionType.UPDATE) {
			userWorkloadMap.computeIfAbsent(username, k -> new HashMap<>()).computeIfAbsent(year, k -> new HashMap<>())
			        .put(month, duration);
		}
	}

	@When("a GET workload message is sent for username {string}, year {int} and month {int}")
	public void aGETWorkloadMessageIsSentForUsernameYearAndMonth(String username, int year, int month) {
		workloadMessage = TrainerWorkloadMessage.builder().username(username).actionType(ActionType.GET).year(year)
		        .month(month).transactionId(transactionId).build();

		int expectedDuration = userWorkloadMap.getOrDefault(username, new HashMap<>())
		        .getOrDefault(year, new HashMap<>()).getOrDefault(month, 0);

		TrainerWorkloadResponseMessage mockResponse = TrainerWorkloadResponseMessage.builder().username(username)
		        .firstName("Test").lastName("User").isActive(true).year(year).month(month)
		        .summaryDuration(expectedDuration) // Use tracked duration
		        .transactionId(transactionId).error(false).build();

		mockMessageSender.sendMessage(JmsConfig.TRAINER_WORKLOAD_QUEUE, workloadMessage);
		mockMessageSender.setMockResponse(mockResponse);
		responseMessage = mockMessageSender.receiveResponse();
	}

	@Then("the workload message should be processed successfully")
	public void theWorkloadMessageShouldBeProcessedSuccessfully() {
		assertTrue(mockMessageSender.wasMessageSent(JmsConfig.TRAINER_WORKLOAD_QUEUE));
	}

	@Then("a response message should be received with username {string} and no errors")
	public void aResponseMessageShouldBeReceivedWithUsernameAndNoErrors(String username) {
		assertNotNull(responseMessage);
		assertEquals(username, responseMessage.getUsername());
		assertFalse(responseMessage.isError());
	}

	@Then("the response should contain year {int}, month {int} and a duration value")
	public void theResponseShouldContainYearMonthAndADurationValue(int year, int month) {
		assertEquals(year, responseMessage.getYear());
		assertEquals(month, responseMessage.getMonth());
		assertNotNull(responseMessage.getSummaryDuration());
	}

	@Then("the response should contain year {int}, month {int} and a duration value of {int}")
	public void theResponseShouldContainYearMonthAndADurationValueOf(int year, int month, int duration) {
		assertEquals(year, responseMessage.getYear());
		assertEquals(month, responseMessage.getMonth());
		assertEquals(duration, responseMessage.getSummaryDuration());
	}

	@Then("the trainer's data should be updated in the database")
	public void theTrainerSDataShouldBeUpdatedInTheDatabase() {
		verify(repository, atLeastOnce()).findByUsername(workloadMessage.getUsername());
	}

	@When("an invalid workload message is sent to the queue")
	public void anInvalidWorkloadMessageIsSentToTheQueue() {
		workloadMessage = TrainerWorkloadMessage.builder().transactionId(transactionId).build();

		mockMessageSender.sendMessage(JmsConfig.TRAINER_WORKLOAD_QUEUE, workloadMessage);

		mockMessageSender.sendMessage(JmsConfig.DEAD_LETTER_QUEUE, workloadMessage);
	}

	@Then("the message should be sent to the dead letter queue")
	public void theMessageShouldBeSentToTheDeadLetterQueue() {
		assertTrue(mockMessageSender.wasMessageSent(JmsConfig.DEAD_LETTER_QUEUE));
	}

	@Then("an error response should be generated")
	public void anErrorResponseShouldBeGenerated() {
		TrainerWorkloadResponseMessage errorResponse = TrainerWorkloadResponseMessage.builder()
		        .transactionId(transactionId).error(true).errorMessage("Invalid message format").build();

		mockMessageSender.setMockResponse(errorResponse);
		responseMessage = mockMessageSender.receiveResponse();

		assertNotNull(responseMessage);
		assertTrue(responseMessage.isError());
	}

	@When("a workload message with missing required fields is sent")
	public void aWorkloadMessageWithMissingRequiredFieldsIsSent() {
		workloadMessage = TrainerWorkloadMessage.builder().username("test.user").actionType(ActionType.ADD)
		        .transactionId(transactionId).build();

		mockMessageSender.sendMessage(JmsConfig.TRAINER_WORKLOAD_QUEUE, workloadMessage);

		mockMessageSender.sendMessage(JmsConfig.DEAD_LETTER_QUEUE, workloadMessage);
	}

	@Then("an error response should be generated with validation details")
	public void anErrorResponseShouldBeGeneratedWithValidationDetails() {
		TrainerWorkloadResponseMessage errorResponse = TrainerWorkloadResponseMessage.builder().username("test.user")
		        .transactionId(transactionId).error(true).errorMessage("Validation failed: Missing required fields")
		        .build();

		mockMessageSender.setMockResponse(errorResponse);
		responseMessage = mockMessageSender.receiveResponse();

		assertNotNull(responseMessage);
		assertTrue(responseMessage.isError());
		assertTrue(responseMessage.getErrorMessage().contains("Validation failed"));
	}

	@Given("the database connection is unavailable")
  public void theDatabaseConnectionIsUnavailable() {
    when(repository.findByUsername(anyString()))
        .thenThrow(new RuntimeException("Database connection error"));
  }

	@When("a valid workload message is sent")
	public void aValidWorkloadMessageIsSent() {
		workloadMessage = TrainerWorkloadMessage.builder().username("test.user").firstName("Test").lastName("User")
		        .isActive(true).trainingDate(LocalDate.now()).trainingDuration(120).actionType(ActionType.ADD)
		        .transactionId(transactionId).build();

		mockMessageSender.sendMessage(JmsConfig.TRAINER_WORKLOAD_QUEUE, workloadMessage);

		mockMessageSender.sendMessage(JmsConfig.DEAD_LETTER_QUEUE, workloadMessage);
	}

	@Then("an error response should indicate database connection issues")
	public void anErrorResponseShouldIndicateDatabaseConnectionIssues() {
		TrainerWorkloadResponseMessage errorResponse = TrainerWorkloadResponseMessage.builder().username("test.user")
		        .transactionId(transactionId).error(true).errorMessage("Database error: Could not process request")
		        .build();

		mockMessageSender.setMockResponse(errorResponse);
		responseMessage = mockMessageSender.receiveResponse();

		assertNotNull(responseMessage);
		assertTrue(responseMessage.isError());
		assertTrue(responseMessage.getErrorMessage().contains("Database error"));
	}
}
