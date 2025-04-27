package com.trainerworkloadservice.component.steps;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.trainerworkloadservice.TrainerWorkload.application.TrainerWorkloadService;
import com.trainerworkloadservice.TrainerWorkload.application.exception.TrainerWorkloadNotFoundException;
import com.trainerworkloadservice.TrainerWorkload.application.port.input.ProcessTrainerWorkloadCommand;
import com.trainerworkloadservice.TrainerWorkload.application.port.output.LoadTrainerWorkloadPort;
import com.trainerworkloadservice.TrainerWorkload.application.port.output.UpdateTrainerWorkloadPort;
import com.trainerworkloadservice.TrainerWorkload.domain.*;
import com.trainerworkloadservice.component.config.ComponentTestConfig;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = ComponentTestConfig.class)
public class TrainerWorkloadSteps {
	private static final Logger logger = LoggerFactory.getLogger(TrainerWorkloadSteps.class);

	private static final String DEFAULT_FIRST_NAME = "Test";
	private static final String DEFAULT_LAST_NAME = "User";
	private static final String TRAINER_NOT_FOUND_MESSAGE = "Trainer not found";
	private static final String DATABASE_CONNECTION_ERROR = "Database connection failed";

	private static final String MDC_TEST_ID = "testId";
	private static final String MDC_USERNAME = "username";
	private static final String MDC_ACTION = "action";
	private static final String MDC_YEAR = "year";
	private static final String MDC_MONTH = "month";
	private static final String MDC_DURATION = "duration";
	private static final String MDC_TRANSACTION_ID = "transactionId";

	@Autowired
	private TrainerWorkloadService trainerWorkloadService;

	@Autowired
	private LoadTrainerWorkloadPort loadTrainerWorkloadPort;

	@Autowired
	private UpdateTrainerWorkloadPort updateTrainerWorkloadPort;

	private ProcessTrainerWorkloadCommand command;
	private TrainerWorkload existingTrainerWorkload;
	private TrainerMonthlyWorkload monthlyWorkload;
	private Exception thrownException;
	private String transactionId;

	@Before
	public void setup() {
		reset(loadTrainerWorkloadPort, updateTrainerWorkloadPort);
		transactionId = UUID.randomUUID().toString();
		thrownException = null;
		command = null;
		existingTrainerWorkload = null;
		monthlyWorkload = null;

		MDC.put(MDC_TEST_ID, UUID.randomUUID().toString());
		MDC.put(MDC_TRANSACTION_ID, transactionId);

		logger.info("Starting trainer workload test with transaction ID: {}", transactionId);
	}

	@Given("a trainer with username {string}, first name {string}, last name {string} and active status {string}")
	public void aTrainerWithUsernameFirstNameLastNameAndActiveStatus(String username, String firstName, String lastName,
	        String isActive) {
		MDC.put(MDC_USERNAME, username);

		logger.info("Setting up trainer: {}, {}, {}, active: {}", username, firstName, lastName, isActive);

		existingTrainerWorkload = TrainerWorkload.builder().id(UUID.randomUUID().toString()).username(username)
		        .firstName(firstName).lastName(lastName).isActive(Boolean.parseBoolean(isActive))
		        .years(new ArrayList<>()).build();

		when(loadTrainerWorkloadPort.findByUsername(username)).thenReturn(existingTrainerWorkload);

		logger.debug("Trainer workload mock configured for username: {}", username);
	}

	@Given("the trainer has a workload record for year {int} month {int} with duration {int}")
	public void theTrainerHasAWorkloadRecordForYearMonthWithDuration(int year, int month, int duration) {
		MDC.put(MDC_YEAR, String.valueOf(year));
		MDC.put(MDC_MONTH, String.valueOf(month));
		MDC.put(MDC_DURATION, String.valueOf(duration));

		logger.info("Setting up workload record: year {}, month {}, duration {}", year, month, duration);

		MonthWorkload monthWorkload = MonthWorkload.builder().month(month).summaryDuration(duration).build();

		YearWorkload yearWorkload = YearWorkload.builder().year(year).months(List.of(monthWorkload)).build();

		existingTrainerWorkload.getYears().add(yearWorkload);

		when(loadTrainerWorkloadPort.findByUsername(existingTrainerWorkload.getUsername()))
		        .thenReturn(existingTrainerWorkload);

		logger.debug("Workload record configured for trainer: {}", existingTrainerWorkload.getUsername());

		MDC.remove(MDC_YEAR);
		MDC.remove(MDC_MONTH);
		MDC.remove(MDC_DURATION);
	}

	@Given("the trainer does not exist in the system")
	public void theTrainerDoesNotExistInTheSystem() {
		logger.info("Setting up scenario: trainer does not exist");

		when(loadTrainerWorkloadPort.findByUsername(anyString()))
		        .thenThrow(new TrainerWorkloadNotFoundException(TRAINER_NOT_FOUND_MESSAGE));

		logger.debug("Configured mock to throw TrainerWorkloadNotFoundException");
	}

	@Given("the database is unavailable")
	public void theDatabaseIsUnavailable() {
		logger.info("Setting up scenario: database is unavailable");

		when(loadTrainerWorkloadPort.findByUsername(anyString()))
		        .thenThrow(new DataAccessResourceFailureException(DATABASE_CONNECTION_ERROR));

		logger.debug("Configured mock to throw DataAccessResourceFailureException");
	}

	@When("I process a {string} workload command for username {string} with date {string} and duration {int}")
	public void iProcessAWorkloadCommandForUsernameWithDateAndDuration(String action, String username, String date,
	        Integer duration) {
		MDC.put(MDC_ACTION, action);
		MDC.put(MDC_USERNAME, username);
		MDC.put(MDC_DURATION, String.valueOf(duration));

		logger.info("Processing {} workload command for {}: date={}, duration={}", action, username, date, duration);

		command = buildWorkloadCommand(action, username, date, duration);

		try {
			trainerWorkloadService.processTrainerWorkload(command);
			logger.info("Workload command processed successfully");
		} catch (Exception e) {
			logger.error("Error processing workload command: {}", e.getMessage(), e);
			thrownException = e;
		} finally {
			MDC.remove(MDC_ACTION);
			MDC.remove(MDC_USERNAME);
			MDC.remove(MDC_DURATION);
		}
	}

	@When("I process a {string} workload command for username {string} with date {string} and invalid duration {int}")
	public void iProcessAWorkloadCommandForUsernameWithDateAndInvalidDuration(String action, String username,
	        String date, Integer duration) {
		MDC.put(MDC_ACTION, action);
		MDC.put(MDC_USERNAME, username);
		MDC.put(MDC_DURATION, String.valueOf(duration));

		logger.info("Processing {} workload command with invalid duration for {}: date={}, duration={}", action,
		        username, date, duration);

		command = buildWorkloadCommand(action, username, date, duration);

		try {
			trainerWorkloadService.processTrainerWorkload(command);
			logger.info("Workload command processed without validation errors");
		} catch (Exception e) {
			logger.info("Expected validation error occurred: {}", e.getMessage());
			thrownException = e;
		} finally {
			MDC.remove(MDC_ACTION);
			MDC.remove(MDC_USERNAME);
			MDC.remove(MDC_DURATION);
		}
	}

	@When("I process a workload command with missing required fields")
	public void iProcessAWorkloadCommandWithMissingRequiredFields() {
		MDC.put(MDC_ACTION, "INVALID");

		logger.info("Processing workload command with missing required fields");

		command = ProcessTrainerWorkloadCommand.builder().username("john.doe").firstName("John").lastName("Doe")
		        .isActive(true).actionType(ActionType.ADD).transactionId(transactionId).build();

		try {
			trainerWorkloadService.processTrainerWorkload(command);
			logger.info("Workload command processed without validation errors");
		} catch (Exception e) {
			logger.info("Expected validation error occurred: {}", e.getMessage());
			thrownException = e;
		} finally {
			MDC.remove(MDC_ACTION);
		}

		verify(updateTrainerWorkloadPort, never()).save(any(TrainerWorkload.class));
	}

	@When("I request monthly workload for username {string} for year {int} and month {int}")
	public void iRequestMonthlyWorkloadForUsernameForYearAndMonth(String username, int year, int month) {
		MDC.put(MDC_USERNAME, username);
		MDC.put(MDC_YEAR, String.valueOf(year));
		MDC.put(MDC_MONTH, String.valueOf(month));

		logger.info("Requesting monthly workload for {}: year={}, month={}", username, year, month);

		try {
			monthlyWorkload = trainerWorkloadService.loadTrainerMonthlyWorkload(username, year, month, transactionId);
			logger.info("Monthly workload retrieved successfully");
		} catch (Exception e) {
			logger.error("Error retrieving monthly workload: {}", e.getMessage(), e);
			thrownException = e;
		} finally {
			MDC.remove(MDC_USERNAME);
			MDC.remove(MDC_YEAR);
			MDC.remove(MDC_MONTH);
		}
	}

	@Then("the trainer workload should be saved with updated duration {int} for year {int} month {int}")
	public void theTrainerWorkloadShouldBeSavedWithUpdatedDurationForYearMonth(int expectedDuration, int year,
	        int month) {
		MDC.put(MDC_YEAR, String.valueOf(year));
		MDC.put(MDC_MONTH, String.valueOf(month));
		MDC.put(MDC_DURATION, String.valueOf(expectedDuration));

		logger.info("Verifying workload saved with duration {} for year {} month {}", expectedDuration, year, month);

		if (thrownException != null) {
			fail("Exception was thrown: " + thrownException.getMessage());
		}

		verify(updateTrainerWorkloadPort, atLeastOnce()).save(any(TrainerWorkload.class));

		ArgumentCaptor<TrainerWorkload> workloadCaptor = ArgumentCaptor.forClass(TrainerWorkload.class);
		verify(updateTrainerWorkloadPort, atLeastOnce()).save(workloadCaptor.capture());

		TrainerWorkload savedWorkload = workloadCaptor.getValue();
		int actualDuration = findDurationForYearAndMonth(savedWorkload, year, month);

		assertEquals(expectedDuration, actualDuration,
		        "Expected duration " + expectedDuration + " but got " + actualDuration);

		logger.info("Workload verification successful: duration={}", actualDuration);

		MDC.remove(MDC_YEAR);
		MDC.remove(MDC_MONTH);
		MDC.remove(MDC_DURATION);
	}

	@Then("the final trainer workload should be {int} for year {int} month {int}")
	public void theFinalTrainerWorkloadShouldBeForYearMonth(int expectedDuration, int year, int month) {
		MDC.put(MDC_YEAR, String.valueOf(year));
		MDC.put(MDC_MONTH, String.valueOf(month));
		MDC.put(MDC_DURATION, String.valueOf(expectedDuration));

		logger.info("Verifying final workload is {} for year {} month {}", expectedDuration, year, month);

		if (thrownException != null) {
			fail("Exception was thrown: " + thrownException.getMessage());
		}

		verify(updateTrainerWorkloadPort, atLeastOnce()).save(any(TrainerWorkload.class));

		ArgumentCaptor<TrainerWorkload> workloadCaptor = ArgumentCaptor.forClass(TrainerWorkload.class);
		verify(updateTrainerWorkloadPort, atLeastOnce()).save(workloadCaptor.capture());

		List<TrainerWorkload> savedWorkloads = workloadCaptor.getAllValues();
		TrainerWorkload lastSavedWorkload = savedWorkloads.get(savedWorkloads.size() - 1);

		int actualDuration = findDurationForYearAndMonth(lastSavedWorkload, year, month);

		assertEquals(expectedDuration, actualDuration,
		        "Expected final duration " + expectedDuration + " but got " + actualDuration);

		logger.info("Final workload verification successful: duration={}", actualDuration);

		MDC.remove(MDC_YEAR);
		MDC.remove(MDC_MONTH);
		MDC.remove(MDC_DURATION);
	}

	@Then("the trainer workload should be processed without errors")
	public void theTrainerWorkloadShouldBeProcessedWithoutErrors() {
		logger.info("Verifying workload processed without errors");

		assertNull(thrownException,
		        "Expected no exceptions but got: " + (thrownException != null ? thrownException.getMessage() : ""));

		logger.info("Workload processed successfully without errors");
	}

	@Then("a new trainer workload record should be created")
	public void aNewTrainerWorkloadRecordShouldBeCreated() {
		logger.info("Verifying new workload record created");

		if (thrownException != null) {
			fail("Exception was thrown: " + thrownException.getMessage());
		}

		verify(updateTrainerWorkloadPort, times(1)).save(any(TrainerWorkload.class));

		logger.info("New workload record creation verified");
	}

	@Then("the monthly workload should show a total duration of {int}")
	public void theMonthlyWorkloadShouldShowATotalDurationOf(int expectedDuration) {
		MDC.put(MDC_DURATION, String.valueOf(expectedDuration));

		logger.info("Verifying monthly workload duration is {}", expectedDuration);

		assertNotNull(monthlyWorkload, "Monthly workload should not be null");
		assertEquals(expectedDuration, monthlyWorkload.getSummaryDuration(),
		        "Expected duration " + expectedDuration + " but got " + monthlyWorkload.getSummaryDuration());

		logger.info("Monthly workload duration verified: {}", monthlyWorkload.getSummaryDuration());

		MDC.remove(MDC_DURATION);
	}

	@Then("the monthly workload should have username {string}, first name {string}, last name {string} and active status {string}")
	public void theMonthlyWorkloadShouldHaveUsernameFirstNameLastNameAndActiveStatus(String username, String firstName,
	        String lastName, String isActive) {
		MDC.put(MDC_USERNAME, username);

		logger.info("Verifying monthly workload details for {}", username);

		assertNotNull(monthlyWorkload, "Monthly workload should not be null");
		assertEquals(username, monthlyWorkload.getUsername(),
		        "Expected username " + username + " but got " + monthlyWorkload.getUsername());
		assertEquals(firstName, monthlyWorkload.getFirstName(),
		        "Expected first name " + firstName + " but got " + monthlyWorkload.getFirstName());
		assertEquals(lastName, monthlyWorkload.getLastName(),
		        "Expected last name " + lastName + " but got " + monthlyWorkload.getLastName());
		assertEquals(Boolean.parseBoolean(isActive), monthlyWorkload.getIsActive(),
		        "Expected active status " + isActive + " but got " + monthlyWorkload.getIsActive());

		logger.info("Monthly workload details verified for {}", username);

		MDC.remove(MDC_USERNAME);
	}

	@Then("an empty workload with zero duration should be returned")
	public void anEmptyWorkloadWithZeroDurationShouldBeReturned() {
		logger.info("Verifying empty workload with zero duration");

		assertNotNull(monthlyWorkload, "Monthly workload should not be null");
		assertEquals(0, monthlyWorkload.getSummaryDuration(),
		        "Expected zero duration but got " + monthlyWorkload.getSummaryDuration());

		logger.info("Empty workload verification successful");
	}

	@Then("a validation error should occur with message {string}")
	public void aValidationErrorShouldOccurWithMessage(String errorMessage) {
		logger.info("Verifying validation error with message: {}", errorMessage);

		if (errorMessage.equals("Required fields are missing")) {
			verify(updateTrainerWorkloadPort, never()).save(any(TrainerWorkload.class));
			logger.info("Verified that no save operation was performed due to missing fields");
			return;
		}

		if (thrownException != null) {
			assertTrue(thrownException.getMessage().contains(errorMessage), "Expected error message to contain '"
			        + errorMessage + "' but was: " + thrownException.getMessage());
			logger.info("Validation error verified: {}", thrownException.getMessage());
		} else {
			verify(updateTrainerWorkloadPort, never()).save(any(TrainerWorkload.class));
			logger.info("Verified that no save operation was performed due to validation error");
		}
	}

	@Then("a database error should occur")
	public void aDatabaseErrorShouldOccur() {
		logger.info("Verifying database error occurred");

		assertNotNull(thrownException, "Expected an exception but none was thrown");

		boolean isExpectedExceptionType = thrownException instanceof RuntimeException
		        || thrownException.getCause() instanceof DataAccessException;

		assertTrue(isExpectedExceptionType,
		        "Expected RuntimeException or DataAccessException but got: " + thrownException.getClass().getName());

		logger.info("Database error verified: {}", thrownException.getMessage());
	}

	private ProcessTrainerWorkloadCommand buildWorkloadCommand(String action, String username, String date,
	        Integer duration) {
		return ProcessTrainerWorkloadCommand.builder().username(username)
		        .firstName(
		                existingTrainerWorkload != null ? existingTrainerWorkload.getFirstName() : DEFAULT_FIRST_NAME)
		        .lastName(existingTrainerWorkload != null ? existingTrainerWorkload.getLastName() : DEFAULT_LAST_NAME)
		        .isActive(existingTrainerWorkload != null ? existingTrainerWorkload.getIsActive() : true)
		        .trainingDate(LocalDate.parse(date)).trainingDuration(duration)
		        .actionType(ActionType.valueOf(action.toUpperCase())).transactionId(transactionId).build();
	}

	private int findDurationForYearAndMonth(TrainerWorkload workload, int year, int month) {
		return workload.getYears().stream().filter(y -> y.getYear() == year).flatMap(y -> y.getMonths().stream())
		        .filter(m -> m.getMonth() == month).findFirst().map(MonthWorkload::getSummaryDuration).orElse(-1);
	}
}
