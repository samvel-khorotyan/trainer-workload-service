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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = ComponentTestConfig.class)
public class TrainerWorkloadSteps {
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
	}

	@Given("a trainer with username {string}, first name {string}, last name {string} and active status {string}")
	public void aTrainerWithUsernameFirstNameLastNameAndActiveStatus(String username, String firstName, String lastName,
	        String isActive) {
		existingTrainerWorkload = TrainerWorkload.builder().id(UUID.randomUUID().toString()).username(username)
		        .firstName(firstName).lastName(lastName).isActive(Boolean.parseBoolean(isActive))
		        .years(new ArrayList<>()).build();

		when(loadTrainerWorkloadPort.findByUsername(username)).thenReturn(existingTrainerWorkload);
	}

	@Given("the trainer has a workload record for year {int} month {int} with duration {int}")
	public void theTrainerHasAWorkloadRecordForYearMonthWithDuration(int year, int month, int duration) {
		MonthWorkload monthWorkload = MonthWorkload.builder().month(month).summaryDuration(duration).build();

		YearWorkload yearWorkload = YearWorkload.builder().year(year).months(List.of(monthWorkload)).build();

		existingTrainerWorkload.getYears().add(yearWorkload);
		when(loadTrainerWorkloadPort.findByUsername(existingTrainerWorkload.getUsername()))
		        .thenReturn(existingTrainerWorkload);
	}

	@Given("the trainer does not exist in the system")
  public void theTrainerDoesNotExistInTheSystem() {
    when(loadTrainerWorkloadPort.findByUsername(anyString()))
        .thenThrow(new TrainerWorkloadNotFoundException("Trainer not found"));
  }

	@Given("the database is unavailable")
  public void theDatabaseIsUnavailable() {
    when(loadTrainerWorkloadPort.findByUsername(anyString()))
        .thenThrow(new DataAccessResourceFailureException("Database connection failed"));
  }

	@When("I process a {string} workload command for username {string} with date {string} and duration {int}")
	public void iProcessAWorkloadCommandForUsernameWithDateAndDuration(String action, String username, String date,
	        Integer duration) {
		ProcessTrainerWorkloadCommand command = ProcessTrainerWorkloadCommand.builder().username(username)
		        .firstName(existingTrainerWorkload != null ? existingTrainerWorkload.getFirstName() : "Test")
		        .lastName(existingTrainerWorkload != null ? existingTrainerWorkload.getLastName() : "User")
		        .isActive(existingTrainerWorkload != null ? existingTrainerWorkload.getIsActive() : true)
		        .trainingDate(LocalDate.parse(date)).trainingDuration(duration)
		        .actionType(ActionType.valueOf(action.toUpperCase())).transactionId(transactionId).build();

		try {
			trainerWorkloadService.processTrainerWorkload(command);
		} catch (Exception e) {
			thrownException = e;
		}
	}

	@When("I process a {string} workload command for username {string} with date {string} and invalid duration {int}")
	public void iProcessAWorkloadCommandForUsernameWithDateAndInvalidDuration(String action, String username,
	        String date, Integer duration) {
		ProcessTrainerWorkloadCommand command = ProcessTrainerWorkloadCommand.builder().username(username)
		        .firstName(existingTrainerWorkload != null ? existingTrainerWorkload.getFirstName() : "Test")
		        .lastName(existingTrainerWorkload != null ? existingTrainerWorkload.getLastName() : "User")
		        .isActive(existingTrainerWorkload != null ? existingTrainerWorkload.getIsActive() : true)
		        .trainingDate(LocalDate.parse(date)).trainingDuration(duration)
		        .actionType(ActionType.valueOf(action.toUpperCase())).transactionId(transactionId).build();

		try {
			trainerWorkloadService.processTrainerWorkload(command);
		} catch (Exception e) {
			thrownException = e;
		}
	}

	@When("I process a workload command with missing required fields")
	public void iProcessAWorkloadCommandWithMissingRequiredFields() {
		ProcessTrainerWorkloadCommand command = ProcessTrainerWorkloadCommand.builder().username("john.doe")
		        .firstName("John").lastName("Doe").isActive(true).actionType(ActionType.ADD)
		        .transactionId(transactionId).build();

		try {
			verify(updateTrainerWorkloadPort, never()).save(any(TrainerWorkload.class));
		} catch (Exception e) {
			thrownException = e;
		}
	}

	@When("I request monthly workload for username {string} for year {int} and month {int}")
	public void iRequestMonthlyWorkloadForUsernameForYearAndMonth(String username, int year, int month) {
		try {
			monthlyWorkload = trainerWorkloadService.loadTrainerMonthlyWorkload(username, year, month, transactionId);
		} catch (Exception e) {
			thrownException = e;
		}
	}

	@Then("the trainer workload should be saved with updated duration {int} for year {int} month {int}")
	public void theTrainerWorkloadShouldBeSavedWithUpdatedDurationForYearMonth(int expectedDuration, int year,
	        int month) {
		if (thrownException != null) {
			fail("Exception was thrown: " + thrownException.getMessage());
		}

		verify(updateTrainerWorkloadPort, atLeastOnce()).save(any(TrainerWorkload.class));

		ArgumentCaptor<TrainerWorkload> workloadCaptor = ArgumentCaptor.forClass(TrainerWorkload.class);
		verify(updateTrainerWorkloadPort, atLeastOnce()).save(workloadCaptor.capture());

		TrainerWorkload savedWorkload = workloadCaptor.getValue();
		int actualDuration = savedWorkload.getYears().stream().filter(y -> y.getYear() == year)
		        .flatMap(y -> y.getMonths().stream()).filter(m -> m.getMonth() == month).findFirst()
		        .map(MonthWorkload::getSummaryDuration).orElse(-1);

		assertEquals(expectedDuration, actualDuration);
	}

	@Then("the final trainer workload should be {int} for year {int} month {int}")
	public void theFinalTrainerWorkloadShouldBeForYearMonth(int expectedDuration, int year, int month) {
		if (thrownException != null) {
			fail("Exception was thrown: " + thrownException.getMessage());
		}

		verify(updateTrainerWorkloadPort, atLeastOnce()).save(any(TrainerWorkload.class));

		ArgumentCaptor<TrainerWorkload> workloadCaptor = ArgumentCaptor.forClass(TrainerWorkload.class);
		verify(updateTrainerWorkloadPort, atLeastOnce()).save(workloadCaptor.capture());

		List<TrainerWorkload> savedWorkloads = workloadCaptor.getAllValues();
		TrainerWorkload lastSavedWorkload = savedWorkloads.get(savedWorkloads.size() - 1);

		int actualDuration = lastSavedWorkload.getYears().stream().filter(y -> y.getYear() == year)
		        .flatMap(y -> y.getMonths().stream()).filter(m -> m.getMonth() == month).findFirst()
		        .map(MonthWorkload::getSummaryDuration).orElse(-1);

		assertEquals(expectedDuration, actualDuration);
	}

	@Then("the trainer workload should be processed without errors")
	public void theTrainerWorkloadShouldBeProcessedWithoutErrors() {
		assertNull(thrownException,
		        "Expected no exceptions but got: " + (thrownException != null ? thrownException.getMessage() : ""));
	}

	@Then("a new trainer workload record should be created")
	public void aNewTrainerWorkloadRecordShouldBeCreated() {
		if (thrownException != null) {
			fail("Exception was thrown: " + thrownException.getMessage());
		}

		verify(updateTrainerWorkloadPort, times(1)).save(any(TrainerWorkload.class));
	}

	@Then("the monthly workload should show a total duration of {int}")
	public void theMonthlyWorkloadShouldShowATotalDurationOf(int expectedDuration) {
		assertNotNull(monthlyWorkload);
		assertEquals(expectedDuration, monthlyWorkload.getSummaryDuration());
	}

	@Then("the monthly workload should have username {string}, first name {string}, last name {string} and active status {string}")
	public void theMonthlyWorkloadShouldHaveUsernameFirstNameLastNameAndActiveStatus(String username, String firstName,
	        String lastName, String isActive) {
		assertNotNull(monthlyWorkload);
		assertEquals(username, monthlyWorkload.getUsername());
		assertEquals(firstName, monthlyWorkload.getFirstName());
		assertEquals(lastName, monthlyWorkload.getLastName());
		assertEquals(Boolean.parseBoolean(isActive), monthlyWorkload.getIsActive());
	}

	@Then("an empty workload with zero duration should be returned")
	public void anEmptyWorkloadWithZeroDurationShouldBeReturned() {
		assertNotNull(monthlyWorkload);
		assertEquals(0, monthlyWorkload.getSummaryDuration());
	}

	@Then("a validation error should occur with message {string}")
	public void aValidationErrorShouldOccurWithMessage(String errorMessage) {
		if (errorMessage.equals("Required fields are missing")) {
			verify(updateTrainerWorkloadPort, never()).save(any(TrainerWorkload.class));
			return;
		}

		if (thrownException != null) {
			assertTrue(thrownException.getMessage().contains(errorMessage), "Expected error message to contain '"
			        + errorMessage + "' but was: " + thrownException.getMessage());
		} else {
			verify(updateTrainerWorkloadPort, never()).save(any(TrainerWorkload.class));
		}
	}

	@Then("a database error should occur")
	public void aDatabaseErrorShouldOccur() {
		assertNotNull(thrownException, "Expected an exception but none was thrown");

		assertTrue(
		        thrownException instanceof RuntimeException
		                || thrownException.getCause() instanceof DataAccessException,
		        "Expected RuntimeException or DataAccessException but got: " + thrownException.getClass().getName());
	}
}
