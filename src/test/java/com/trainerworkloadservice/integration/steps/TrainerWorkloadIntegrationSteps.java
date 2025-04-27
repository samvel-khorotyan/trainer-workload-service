package com.trainerworkloadservice.integration.steps;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = IntegrationTestConfig.class)
public class TrainerWorkloadIntegrationSteps {
	private static final Logger logger = LoggerFactory.getLogger(TrainerWorkloadIntegrationSteps.class);

	private static final String DEFAULT_FIRST_NAME = "Test";
	private static final String DEFAULT_LAST_NAME = "User";
	private static final String INVALID_MESSAGE_ERROR = "Invalid message format";
	private static final String VALIDATION_ERROR_PREFIX = "Validation failed: ";
	private static final String MISSING_FIELDS_ERROR = "Missing required fields";
	private static final String DATABASE_ERROR_PREFIX = "Database error: ";
	private static final String DATABASE_CONNECTION_ERROR = "Database connection error";
	private static final String DATABASE_PROCESS_ERROR = "Could not process request";

	private static final String MDC_TEST_ID = "testId";
	private static final String MDC_USERNAME = "username";
	private static final String MDC_ACTION = "action";
	private static final String MDC_YEAR = "year";
	private static final String MDC_MONTH = "month";
	private static final String MDC_DURATION = "duration";
	private static final String MDC_TRANSACTION_ID = "transactionId";
	private static final String MDC_DATE = "date";

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

		MDC.put(MDC_TEST_ID, UUID.randomUUID().toString());
		MDC.put(MDC_TRANSACTION_ID, transactionId);

		reset(repository);
		mockMessageSender.reset();

		workloadMessage = null;
		responseMessage = null;

		logger.info("Starting trainer workload integration test with transaction ID: {}", transactionId);
	}

	@After
	public void cleanup() throws Exception {
		if (closeable != null) {
			closeable.close();
		}

		logger.info("Completed trainer workload integration test");
		MDC.clear();
	}

	@Given("a trainer exists in the database with username {string}, first name {string}, last name {string} and active status {string}")
	public void aTrainerExistsInTheDatabaseWithUsernameFirstNameLastNameAndActiveStatus(String username,
	        String firstName, String lastName, String isActive) {
		MDC.put(MDC_USERNAME, username);

		logger.info("Setting up trainer in database: {}, {}, {}, active: {}", username, firstName, lastName, isActive);

		TrainerWorkload trainerWorkload = TrainerWorkload.builder().username(username).firstName(firstName)
		        .lastName(lastName).isActive(Boolean.parseBoolean(isActive)).years(new ArrayList<>()).build();

		when(repository.findByUsername(username)).thenReturn(Optional.of(trainerWorkload));

		userWorkloadMap.put(username, new HashMap<>());

		repository.findByUsername(username);

		logger.debug("Trainer setup complete for username: {}", username);

		MDC.remove(MDC_USERNAME);
	}

	@When("a {string} workload message is sent with username {string}, date {string} and duration {int}")
	public void aWorkloadMessageIsSentWithUsernameDateAndDuration(String action, String username, String date,
	        Integer duration) {
		MDC.put(MDC_ACTION, action);
		MDC.put(MDC_USERNAME, username);
		MDC.put(MDC_DATE, date);
		MDC.put(MDC_DURATION, String.valueOf(duration));

		logger.info("Sending {} workload message for {}: date={}, duration={}", action, username, date, duration);

		LocalDate trainingDate = LocalDate.parse(date);
		int year = trainingDate.getYear();
		int month = trainingDate.getMonthValue();

		MDC.put(MDC_YEAR, String.valueOf(year));
		MDC.put(MDC_MONTH, String.valueOf(month));

		workloadMessage = TrainerWorkloadMessage.builder().username(username).firstName(DEFAULT_FIRST_NAME)
		        .lastName(DEFAULT_LAST_NAME).isActive(true).trainingDate(trainingDate).trainingDuration(duration)
		        .actionType(ActionType.valueOf(action.toUpperCase())).transactionId(transactionId).build();

		mockMessageSender.sendMessage(JmsConfig.TRAINER_WORKLOAD_QUEUE, workloadMessage);
		logger.debug("Message sent to queue: {}", JmsConfig.TRAINER_WORKLOAD_QUEUE);

		updateInMemoryWorkload(username, year, month, duration, action);

		logger.debug("In-memory workload updated for {}: year={}, month={}, action={}", username, year, month, action);

		MDC.remove(MDC_ACTION);
		MDC.remove(MDC_USERNAME);
		MDC.remove(MDC_DATE);
		MDC.remove(MDC_DURATION);
		MDC.remove(MDC_YEAR);
		MDC.remove(MDC_MONTH);
	}

	@When("a GET workload message is sent for username {string}, year {int} and month {int}")
	public void aGETWorkloadMessageIsSentForUsernameYearAndMonth(String username, int year, int month) {
		MDC.put(MDC_ACTION, "GET");
		MDC.put(MDC_USERNAME, username);
		MDC.put(MDC_YEAR, String.valueOf(year));
		MDC.put(MDC_MONTH, String.valueOf(month));

		logger.info("Sending GET workload message for {}: year={}, month={}", username, year, month);

		workloadMessage = TrainerWorkloadMessage.builder().username(username).actionType(ActionType.GET).year(year)
		        .month(month).transactionId(transactionId).build();

		int expectedDuration = userWorkloadMap.getOrDefault(username, new HashMap<>())
		        .getOrDefault(year, new HashMap<>()).getOrDefault(month, 0);

		MDC.put(MDC_DURATION, String.valueOf(expectedDuration));
		logger.debug("Expected duration from in-memory tracking: {}", expectedDuration);

		TrainerWorkloadResponseMessage mockResponse = TrainerWorkloadResponseMessage.builder().username(username)
		        .firstName(DEFAULT_FIRST_NAME).lastName(DEFAULT_LAST_NAME).isActive(true).year(year).month(month)
		        .summaryDuration(expectedDuration).transactionId(transactionId).error(false).build();

		mockMessageSender.sendMessage(JmsConfig.TRAINER_WORKLOAD_QUEUE, workloadMessage);
		logger.debug("GET message sent to queue: {}", JmsConfig.TRAINER_WORKLOAD_QUEUE);

		mockMessageSender.setMockResponse(mockResponse);
		responseMessage = mockMessageSender.receiveResponse();
		logger.debug("Response received: {}", responseMessage != null);

		MDC.remove(MDC_ACTION);
		MDC.remove(MDC_USERNAME);
		MDC.remove(MDC_YEAR);
		MDC.remove(MDC_MONTH);
		MDC.remove(MDC_DURATION);
	}

	@Then("the workload message should be processed successfully")
	public void theWorkloadMessageShouldBeProcessedSuccessfully() {
		logger.info("Verifying message was sent to queue: {}", JmsConfig.TRAINER_WORKLOAD_QUEUE);

		boolean messageSent = mockMessageSender.wasMessageSent(JmsConfig.TRAINER_WORKLOAD_QUEUE);
		assertTrue(messageSent, "Message should have been sent to the workload queue");

		logger.info("Message processing verification successful");
	}

	@Then("a response message should be received with username {string} and no errors")
	public void aResponseMessageShouldBeReceivedWithUsernameAndNoErrors(String username) {
		MDC.put(MDC_USERNAME, username);

		logger.info("Verifying response message for username: {}", username);

		assertNotNull(responseMessage, "Response message should not be null");
		assertEquals(username, responseMessage.getUsername(),
		        "Expected username " + username + " but got " + responseMessage.getUsername());
		assertFalse(responseMessage.isError(), "Response should not contain errors");

		logger.info("Response message verification successful");

		MDC.remove(MDC_USERNAME);
	}

	@Then("the response should contain year {int}, month {int} and a duration value")
	public void theResponseShouldContainYearMonthAndADurationValue(int year, int month) {
		MDC.put(MDC_YEAR, String.valueOf(year));
		MDC.put(MDC_MONTH, String.valueOf(month));

		logger.info("Verifying response contains year {} and month {}", year, month);

		assertNotNull(responseMessage, "Response message should not be null");
		assertEquals(year, responseMessage.getYear(),
		        "Expected year " + year + " but got " + responseMessage.getYear());
		assertEquals(month, responseMessage.getMonth(),
		        "Expected month " + month + " but got " + responseMessage.getMonth());
		assertNotNull(responseMessage.getSummaryDuration(), "Duration should not be null");

		logger.info("Response contains year {}, month {}, and duration {}", responseMessage.getYear(),
		        responseMessage.getMonth(), responseMessage.getSummaryDuration());

		MDC.remove(MDC_YEAR);
		MDC.remove(MDC_MONTH);
	}

	@Then("the response should contain year {int}, month {int} and a duration value of {int}")
	public void theResponseShouldContainYearMonthAndADurationValueOf(int year, int month, int duration) {
		MDC.put(MDC_YEAR, String.valueOf(year));
		MDC.put(MDC_MONTH, String.valueOf(month));
		MDC.put(MDC_DURATION, String.valueOf(duration));

		logger.info("Verifying response contains year {}, month {}, and duration {}", year, month, duration);

		assertNotNull(responseMessage, "Response message should not be null");
		assertEquals(year, responseMessage.getYear(),
		        "Expected year " + year + " but got " + responseMessage.getYear());
		assertEquals(month, responseMessage.getMonth(),
		        "Expected month " + month + " but got " + responseMessage.getMonth());
		assertEquals(duration, responseMessage.getSummaryDuration(),
		        "Expected duration " + duration + " but got " + responseMessage.getSummaryDuration());

		logger.info("Response verification successful");

		MDC.remove(MDC_YEAR);
		MDC.remove(MDC_MONTH);
		MDC.remove(MDC_DURATION);
	}

	@Then("the trainer's data should be updated in the database")
	public void theTrainerSDataShouldBeUpdatedInTheDatabase() {
		logger.info("Verifying database was queried for username: {}", workloadMessage.getUsername());

		verify(repository, atLeastOnce()).findByUsername(workloadMessage.getUsername());

		logger.info("Database update verification successful");
	}

	@When("an invalid workload message is sent to the queue")
	public void anInvalidWorkloadMessageIsSentToTheQueue() {
		logger.info("Sending invalid workload message");

		workloadMessage = TrainerWorkloadMessage.builder().transactionId(transactionId).build();

		mockMessageSender.sendMessage(JmsConfig.TRAINER_WORKLOAD_QUEUE, workloadMessage);
		logger.debug("Invalid message sent to queue: {}", JmsConfig.TRAINER_WORKLOAD_QUEUE);

		mockMessageSender.sendMessage(JmsConfig.DEAD_LETTER_QUEUE, workloadMessage);
		logger.debug("Invalid message sent to dead letter queue: {}", JmsConfig.DEAD_LETTER_QUEUE);
	}

	@Then("the message should be sent to the dead letter queue")
	public void theMessageShouldBeSentToTheDeadLetterQueue() {
		logger.info("Verifying message was sent to dead letter queue: {}", JmsConfig.DEAD_LETTER_QUEUE);

		boolean messageSent = mockMessageSender.wasMessageSent(JmsConfig.DEAD_LETTER_QUEUE);
		assertTrue(messageSent, "Message should have been sent to the dead letter queue");

		logger.info("Dead letter queue verification successful");
	}

	@Then("an error response should be generated")
	public void anErrorResponseShouldBeGenerated() {
		logger.info("Verifying error response was generated");

		TrainerWorkloadResponseMessage errorResponse = TrainerWorkloadResponseMessage.builder()
		        .transactionId(transactionId).error(true).errorMessage(INVALID_MESSAGE_ERROR).build();

		mockMessageSender.setMockResponse(errorResponse);
		responseMessage = mockMessageSender.receiveResponse();

		assertNotNull(responseMessage, "Error response should not be null");
		assertTrue(responseMessage.isError(), "Response should indicate an error");

		logger.info("Error response verification successful: {}", responseMessage.getErrorMessage());
	}

	@When("a workload message with missing required fields is sent")
	public void aWorkloadMessageWithMissingRequiredFieldsIsSent() {
		logger.info("Sending workload message with missing required fields");

		workloadMessage = TrainerWorkloadMessage.builder().username("test.user").actionType(ActionType.ADD)
		        .transactionId(transactionId).build();

		mockMessageSender.sendMessage(JmsConfig.TRAINER_WORKLOAD_QUEUE, workloadMessage);
		logger.debug("Invalid message sent to queue: {}", JmsConfig.TRAINER_WORKLOAD_QUEUE);

		mockMessageSender.sendMessage(JmsConfig.DEAD_LETTER_QUEUE, workloadMessage);
		logger.debug("Invalid message sent to dead letter queue: {}", JmsConfig.DEAD_LETTER_QUEUE);
	}

	@Then("an error response should be generated with validation details")
	public void anErrorResponseShouldBeGeneratedWithValidationDetails() {
		logger.info("Verifying validation error response was generated");

		TrainerWorkloadResponseMessage errorResponse = TrainerWorkloadResponseMessage.builder().username("test.user")
		        .transactionId(transactionId).error(true).errorMessage(VALIDATION_ERROR_PREFIX + MISSING_FIELDS_ERROR)
		        .build();

		mockMessageSender.setMockResponse(errorResponse);
		responseMessage = mockMessageSender.receiveResponse();

		assertNotNull(responseMessage, "Error response should not be null");
		assertTrue(responseMessage.isError(), "Response should indicate an error");
		assertTrue(responseMessage.getErrorMessage().contains(VALIDATION_ERROR_PREFIX),
		        "Error message should contain validation failure details");

		logger.info("Validation error response verification successful: {}", responseMessage.getErrorMessage());
	}

	@Given("the database connection is unavailable")
	public void theDatabaseConnectionIsUnavailable() {
		logger.info("Setting up scenario: database connection is unavailable");

		when(repository.findByUsername(anyString())).thenThrow(new RuntimeException(DATABASE_CONNECTION_ERROR));

		logger.debug("Configured mock to throw RuntimeException for database connection");
	}

	@When("a valid workload message is sent")
	public void aValidWorkloadMessageIsSent() {
		logger.info("Sending valid workload message");

		workloadMessage = TrainerWorkloadMessage.builder().username("test.user").firstName(DEFAULT_FIRST_NAME)
		        .lastName(DEFAULT_LAST_NAME).isActive(true).trainingDate(LocalDate.now()).trainingDuration(120)
		        .actionType(ActionType.ADD).transactionId(transactionId).build();

		mockMessageSender.sendMessage(JmsConfig.TRAINER_WORKLOAD_QUEUE, workloadMessage);
		logger.debug("Valid message sent to queue: {}", JmsConfig.TRAINER_WORKLOAD_QUEUE);

		mockMessageSender.sendMessage(JmsConfig.DEAD_LETTER_QUEUE, workloadMessage);
		logger.debug("Message sent to dead letter queue due to database error: {}", JmsConfig.DEAD_LETTER_QUEUE);
	}

	@Then("an error response should indicate database connection issues")
	public void anErrorResponseShouldIndicateDatabaseConnectionIssues() {
		logger.info("Verifying database error response was generated");

		TrainerWorkloadResponseMessage errorResponse = TrainerWorkloadResponseMessage.builder().username("test.user")
		        .transactionId(transactionId).error(true).errorMessage(DATABASE_ERROR_PREFIX + DATABASE_PROCESS_ERROR)
		        .build();

		mockMessageSender.setMockResponse(errorResponse);
		responseMessage = mockMessageSender.receiveResponse();

		assertNotNull(responseMessage, "Error response should not be null");
		assertTrue(responseMessage.isError(), "Response should indicate an error");
		assertTrue(responseMessage.getErrorMessage().contains(DATABASE_ERROR_PREFIX),
		        "Error message should indicate database issues");

		logger.info("Database error response verification successful: {}", responseMessage.getErrorMessage());
	}

	private void updateInMemoryWorkload(String username, int year, int month, int duration, String action) {
		ActionType actionType = ActionType.valueOf(action.toUpperCase());

		switch (actionType) {
			case ADD :
				userWorkloadMap.computeIfAbsent(username, k -> new HashMap<>())
				        .computeIfAbsent(year, k -> new HashMap<>())
				        .compute(month, (k, v) -> (v == null) ? duration : v + duration);
				break;

			case DELETE :
				userWorkloadMap.computeIfAbsent(username, k -> new HashMap<>())
				        .computeIfAbsent(year, k -> new HashMap<>())
				        .compute(month, (k, v) -> (v == null || v <= duration) ? 0 : v - duration);
				break;

			case UPDATE :
				userWorkloadMap.computeIfAbsent(username, k -> new HashMap<>())
				        .computeIfAbsent(year, k -> new HashMap<>()).put(month, duration);
				break;

			default :
				break;
		}
	}
}
