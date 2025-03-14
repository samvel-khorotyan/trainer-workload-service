package com.trainerworkloadservice.unit.TrainerWorkload.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.trainerworkloadservice.TrainerWorkload.application.TrainerWorkloadService;
import com.trainerworkloadservice.TrainerWorkload.application.exception.TrainerWorkloadNotFoundException;
import com.trainerworkloadservice.TrainerWorkload.application.factory.TrainerWorkloadFactory;
import com.trainerworkloadservice.TrainerWorkload.application.port.input.ProcessTrainerWorkloadCommand;
import com.trainerworkloadservice.TrainerWorkload.application.port.output.LoadTrainerWorkloadPort;
import com.trainerworkloadservice.TrainerWorkload.application.port.output.UpdateTrainerWorkloadPort;
import com.trainerworkloadservice.TrainerWorkload.domain.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceTest {
	@Mock
	private LoadTrainerWorkloadPort loadTrainerWorkloadPort;

	@Mock
	private UpdateTrainerWorkloadPort updateTrainerWorkloadPort;

	@Mock
	private TrainerWorkloadFactory trainerWorkloadFactory;

	@InjectMocks
	private TrainerWorkloadService trainerWorkloadService;

	private String username;
	private String firstName;
	private String lastName;
	private boolean isActive;
	private LocalDate trainingDate;
	private int trainingDuration;
	private String transactionId;
	private int year;
	private int month;
	private TrainerWorkload existingTrainerWorkload;
	private ProcessTrainerWorkloadCommand command;

	@BeforeEach
	void setUp() {
		username = "trainer.username";
		firstName = "John";
		lastName = "Doe";
		isActive = true;
		trainingDate = LocalDate.of(2023, 5, 15);
		trainingDuration = 60;
		transactionId = "transaction-123";
		year = trainingDate.getYear();
		month = trainingDate.getMonthValue();

		command = ProcessTrainerWorkloadCommand.builder().username(username).firstName(firstName).lastName(lastName)
		        .isActive(isActive).trainingDate(trainingDate).trainingDuration(trainingDuration)
		        .actionType(ActionType.ADD).transactionId(transactionId).build();

		existingTrainerWorkload = TrainerWorkload.builder().username(username).firstName("Old First Name")
		        .lastName("Old Last Name").isActive(false).years(new ArrayList<>()).build();
	}

	@Test
  void processTrainerWorkload_ShouldCreateNewTrainerWorkload_WhenUsernameDoesNotExist() {
    // Arrange
    when(loadTrainerWorkloadPort.findByUsername(username))
        .thenThrow(new TrainerWorkloadNotFoundException("Not found"));

    TrainerWorkload newTrainerWorkload =
        TrainerWorkload.builder()
            .username(username)
            .firstName(firstName)
            .lastName(lastName)
            .isActive(isActive)
            .years(new ArrayList<>())
            .build();

    when(trainerWorkloadFactory.createFrom(command)).thenReturn(newTrainerWorkload);

    // Act
    trainerWorkloadService.processTrainerWorkload(command);

    // Assert
    ArgumentCaptor<TrainerWorkload> workloadCaptor = ArgumentCaptor.forClass(TrainerWorkload.class);
    verify(updateTrainerWorkloadPort).save(workloadCaptor.capture());

    TrainerWorkload savedWorkload = workloadCaptor.getValue();
    assertEquals(username, savedWorkload.getUsername());
    assertEquals(firstName, savedWorkload.getFirstName());
    assertEquals(lastName, savedWorkload.getLastName());
    assertEquals(isActive, savedWorkload.getIsActive());

    // Verify year and month were created
    assertEquals(1, savedWorkload.getYears().size());
    YearWorkload yearWorkload = savedWorkload.getYears().get(0);
    assertEquals(year, yearWorkload.getYear());

    assertEquals(1, yearWorkload.getMonths().size());
    MonthWorkload monthWorkload = yearWorkload.getMonths().get(0);
    assertEquals(month, monthWorkload.getMonth());
    assertEquals(trainingDuration, monthWorkload.getSummaryDuration());
  }

	@Test
  void processTrainerWorkload_ShouldUpdateExistingTrainerWorkload_WhenUsernameExists() {
    // Arrange
    when(loadTrainerWorkloadPort.findByUsername(username)).thenReturn(existingTrainerWorkload);

    // Act
    trainerWorkloadService.processTrainerWorkload(command);

    // Assert
    ArgumentCaptor<TrainerWorkload> workloadCaptor = ArgumentCaptor.forClass(TrainerWorkload.class);
    verify(updateTrainerWorkloadPort).save(workloadCaptor.capture());

    TrainerWorkload savedWorkload = workloadCaptor.getValue();
    assertEquals(username, savedWorkload.getUsername());
    assertEquals(firstName, savedWorkload.getFirstName()); // Should be updated
    assertEquals(lastName, savedWorkload.getLastName()); // Should be updated
    assertEquals(isActive, savedWorkload.getIsActive()); // Should be updated

    // Verify year and month were created
    assertEquals(1, savedWorkload.getYears().size());
    YearWorkload yearWorkload = savedWorkload.getYears().get(0);
    assertEquals(year, yearWorkload.getYear());

    assertEquals(1, yearWorkload.getMonths().size());
    MonthWorkload monthWorkload = yearWorkload.getMonths().get(0);
    assertEquals(month, monthWorkload.getMonth());
    assertEquals(trainingDuration, monthWorkload.getSummaryDuration());
  }

	@Test
	void processTrainerWorkload_ShouldAddDuration_WhenActionTypeIsAdd() {
		// Arrange
		// Create existing workload with existing year and month
		MonthWorkload existingMonthWorkload = MonthWorkload.builder().month(month).summaryDuration(100).build();

		YearWorkload existingYearWorkload = YearWorkload.builder().year(year)
		        .months(new ArrayList<>(Collections.singletonList(existingMonthWorkload))).build();

		existingTrainerWorkload.setYears(new ArrayList<>(Collections.singletonList(existingYearWorkload)));

		when(loadTrainerWorkloadPort.findByUsername(username)).thenReturn(existingTrainerWorkload);

		// Act
		trainerWorkloadService.processTrainerWorkload(command); // command has ActionType.ADD

		// Assert
		ArgumentCaptor<TrainerWorkload> workloadCaptor = ArgumentCaptor.forClass(TrainerWorkload.class);
		verify(updateTrainerWorkloadPort).save(workloadCaptor.capture());

		TrainerWorkload savedWorkload = workloadCaptor.getValue();
		MonthWorkload updatedMonthWorkload = savedWorkload.getYears().get(0).getMonths().get(0);

		// Original 100 + new 60 = 160
		assertEquals(160, updatedMonthWorkload.getSummaryDuration());
	}

	@Test
	void processTrainerWorkload_ShouldSubtractDuration_WhenActionTypeIsDelete() {
		// Arrange
		// Create existing workload with existing year and month
		MonthWorkload existingMonthWorkload = MonthWorkload.builder().month(month).summaryDuration(100).build();

		YearWorkload existingYearWorkload = YearWorkload.builder().year(year)
		        .months(new ArrayList<>(Collections.singletonList(existingMonthWorkload))).build();

		existingTrainerWorkload.setYears(new ArrayList<>(Collections.singletonList(existingYearWorkload)));

		when(loadTrainerWorkloadPort.findByUsername(username)).thenReturn(existingTrainerWorkload);

		// Change action type to DELETE
		command = ProcessTrainerWorkloadCommand.builder().username(username).firstName(firstName).lastName(lastName)
		        .isActive(isActive).trainingDate(trainingDate).trainingDuration(trainingDuration)
		        .actionType(ActionType.DELETE).transactionId(transactionId).build();

		// Act
		trainerWorkloadService.processTrainerWorkload(command);

		// Assert
		ArgumentCaptor<TrainerWorkload> workloadCaptor = ArgumentCaptor.forClass(TrainerWorkload.class);
		verify(updateTrainerWorkloadPort).save(workloadCaptor.capture());

		TrainerWorkload savedWorkload = workloadCaptor.getValue();
		MonthWorkload updatedMonthWorkload = savedWorkload.getYears().get(0).getMonths().get(0);

		// Original 100 - new 60 = 40
		assertEquals(40, updatedMonthWorkload.getSummaryDuration());
	}

	@Test
	void processTrainerWorkload_ShouldNotAllowNegativeDuration_WhenDeletingMoreThanExists() {
		// Arrange
		// Create existing workload with existing year and month
		MonthWorkload existingMonthWorkload = MonthWorkload.builder().month(month).summaryDuration(30) // Only 30
		        // minutes exist
		        .build();

		YearWorkload existingYearWorkload = YearWorkload.builder().year(year)
		        .months(new ArrayList<>(Collections.singletonList(existingMonthWorkload))).build();

		existingTrainerWorkload.setYears(new ArrayList<>(Collections.singletonList(existingYearWorkload)));

		when(loadTrainerWorkloadPort.findByUsername(username)).thenReturn(existingTrainerWorkload);

		// Try to delete 60 minutes when only 30 exist
		command = ProcessTrainerWorkloadCommand.builder().username(username).firstName(firstName).lastName(lastName)
		        .isActive(isActive).trainingDate(trainingDate).trainingDuration(trainingDuration) // 60 minutes
		        .actionType(ActionType.DELETE).transactionId(transactionId).build();

		// Act
		trainerWorkloadService.processTrainerWorkload(command);

		// Assert
		ArgumentCaptor<TrainerWorkload> workloadCaptor = ArgumentCaptor.forClass(TrainerWorkload.class);
		verify(updateTrainerWorkloadPort).save(workloadCaptor.capture());

		TrainerWorkload savedWorkload = workloadCaptor.getValue();
		MonthWorkload updatedMonthWorkload = savedWorkload.getYears().get(0).getMonths().get(0);

		// Should be 0, not negative
		assertEquals(0, updatedMonthWorkload.getSummaryDuration());
	}

	@Test
	void loadTrainerMonthlyWorkload_ShouldReturnWorkload_WhenDataExists() {
		// Arrange
		// Create existing workload with existing year and month
		MonthWorkload existingMonthWorkload = MonthWorkload.builder().month(month).summaryDuration(120).build();

		YearWorkload existingYearWorkload = YearWorkload.builder().year(year)
		        .months(new ArrayList<>(Collections.singletonList(existingMonthWorkload))).build();

		existingTrainerWorkload.setYears(new ArrayList<>(Collections.singletonList(existingYearWorkload)));
		existingTrainerWorkload.setFirstName(firstName);
		existingTrainerWorkload.setLastName(lastName);
		existingTrainerWorkload.setIsActive(isActive);

		when(loadTrainerWorkloadPort.findByUsername(username)).thenReturn(existingTrainerWorkload);

		// Act
		TrainerMonthlyWorkload result = trainerWorkloadService.loadTrainerMonthlyWorkload(username, year, month,
		        transactionId);

		// Assert
		assertNotNull(result);
		assertEquals(username, result.getUsername());
		assertEquals(firstName, result.getFirstName());
		assertEquals(lastName, result.getLastName());
		assertEquals(isActive, result.getIsActive());
		assertEquals(year, result.getYear());
		assertEquals(month, result.getMonth());
		assertEquals(120, result.getSummaryDuration());
	}

	@Test
	void loadTrainerMonthlyWorkload_ShouldReturnZeroDuration_WhenYearDoesNotExist() {
		// Arrange
		// Create existing workload with no years
		existingTrainerWorkload.setFirstName(firstName);
		existingTrainerWorkload.setLastName(lastName);
		existingTrainerWorkload.setIsActive(isActive);

		when(loadTrainerWorkloadPort.findByUsername(username)).thenReturn(existingTrainerWorkload);

		// Act
		TrainerMonthlyWorkload result = trainerWorkloadService.loadTrainerMonthlyWorkload(username, year, month,
		        transactionId);

		// Assert
		assertNotNull(result);
		assertEquals(username, result.getUsername());
		assertEquals(firstName, result.getFirstName());
		assertEquals(lastName, result.getLastName());
		assertEquals(isActive, result.getIsActive());
		assertEquals(year, result.getYear());
		assertEquals(month, result.getMonth());
		assertEquals(0, result.getSummaryDuration());
	}

	@Test
	void loadTrainerMonthlyWorkload_ShouldReturnZeroDuration_WhenMonthDoesNotExist() {
		// Arrange
		// Create existing workload with year but no matching month
		YearWorkload existingYearWorkload = YearWorkload.builder().year(year).months(new ArrayList<>()).build();

		existingTrainerWorkload.setYears(new ArrayList<>(Collections.singletonList(existingYearWorkload)));
		existingTrainerWorkload.setFirstName(firstName);
		existingTrainerWorkload.setLastName(lastName);
		existingTrainerWorkload.setIsActive(isActive);

		when(loadTrainerWorkloadPort.findByUsername(username)).thenReturn(existingTrainerWorkload);

		// Act
		TrainerMonthlyWorkload result = trainerWorkloadService.loadTrainerMonthlyWorkload(username, year, month,
		        transactionId);

		// Assert
		assertNotNull(result);
		assertEquals(username, result.getUsername());
		assertEquals(firstName, result.getFirstName());
		assertEquals(lastName, result.getLastName());
		assertEquals(isActive, result.getIsActive());
		assertEquals(year, result.getYear());
		assertEquals(month, result.getMonth());
		assertEquals(0, result.getSummaryDuration());
	}

	@Test
  void loadTrainerMonthlyWorkload_ShouldReturnEmptyWorkload_WhenTrainerDoesNotExist() {
    // Arrange
    when(loadTrainerWorkloadPort.findByUsername(username))
        .thenThrow(new TrainerWorkloadNotFoundException("Not found"));

    // Act
    TrainerMonthlyWorkload result =
        trainerWorkloadService.loadTrainerMonthlyWorkload(username, year, month, transactionId);

    // Assert
    assertNotNull(result);
    assertEquals(username, result.getUsername());
    assertNull(result.getFirstName());
    assertNull(result.getLastName());
    assertNull(result.getIsActive());
    assertEquals(year, result.getYear());
    assertEquals(month, result.getMonth());
    assertEquals(0, result.getSummaryDuration());
  }

	@Test
	void processTrainerWorkload_ShouldCreateNewYearAndMonth_WhenProcessingForNewPeriod() {
		// Arrange
		// Create existing workload with a different year
		MonthWorkload existingMonthWorkload = MonthWorkload.builder().month(3).summaryDuration(100).build();

		YearWorkload existingYearWorkload = YearWorkload.builder().year(2022) // Different year
		        .months(new ArrayList<>(Collections.singletonList(existingMonthWorkload))).build();

		existingTrainerWorkload.setYears(new ArrayList<>(Collections.singletonList(existingYearWorkload)));

		when(loadTrainerWorkloadPort.findByUsername(username)).thenReturn(existingTrainerWorkload);

		// Act
		trainerWorkloadService.processTrainerWorkload(command); // command has year 2023, month 5

		// Assert
		ArgumentCaptor<TrainerWorkload> workloadCaptor = ArgumentCaptor.forClass(TrainerWorkload.class);
		verify(updateTrainerWorkloadPort).save(workloadCaptor.capture());

		TrainerWorkload savedWorkload = workloadCaptor.getValue();

		// Should now have 2 years
		assertEquals(2, savedWorkload.getYears().size());

		// Find the new year (2023)
		YearWorkload newYearWorkload = savedWorkload.getYears().stream().filter(y -> y.getYear().equals(year))
		        .findFirst().orElse(null);

		assertNotNull(newYearWorkload);
		assertEquals(1, newYearWorkload.getMonths().size());

		MonthWorkload newMonthWorkload = newYearWorkload.getMonths().get(0);
		assertEquals(month, newMonthWorkload.getMonth());
		assertEquals(trainingDuration, newMonthWorkload.getSummaryDuration());

		// Original year and month should remain unchanged
		YearWorkload oldYearWorkload = savedWorkload.getYears().stream().filter(y -> y.getYear().equals(2022))
		        .findFirst().orElse(null);

		assertNotNull(oldYearWorkload);
		assertEquals(1, oldYearWorkload.getMonths().size());
		assertEquals(3, oldYearWorkload.getMonths().get(0).getMonth());
		assertEquals(100, oldYearWorkload.getMonths().get(0).getSummaryDuration());
	}

	@Test
	void processTrainerWorkload_ShouldAddNewMonth_ToExistingYear() {
		// Arrange
		// Create existing workload with the same year but different month
		MonthWorkload existingMonthWorkload = MonthWorkload.builder().month(3) // Different month
		        .summaryDuration(100).build();

		YearWorkload existingYearWorkload = YearWorkload.builder().year(year) // Same year as command
		        .months(new ArrayList<>(Collections.singletonList(existingMonthWorkload))).build();

		existingTrainerWorkload.setYears(new ArrayList<>(Collections.singletonList(existingYearWorkload)));

		when(loadTrainerWorkloadPort.findByUsername(username)).thenReturn(existingTrainerWorkload);

		// Act
		trainerWorkloadService.processTrainerWorkload(command); // command has month 5

		// Assert
		ArgumentCaptor<TrainerWorkload> workloadCaptor = ArgumentCaptor.forClass(TrainerWorkload.class);
		verify(updateTrainerWorkloadPort).save(workloadCaptor.capture());

		TrainerWorkload savedWorkload = workloadCaptor.getValue();

		// Should still have 1 year
		assertEquals(1, savedWorkload.getYears().size());

		YearWorkload updatedYearWorkload = savedWorkload.getYears().get(0);
		assertEquals(year, updatedYearWorkload.getYear());

		// Should now have 2 months
		assertEquals(2, updatedYearWorkload.getMonths().size());

		// Find the new month (5)
		MonthWorkload newMonthWorkload = updatedYearWorkload.getMonths().stream()
		        .filter(m -> m.getMonth().equals(month)).findFirst().orElse(null);

		assertNotNull(newMonthWorkload);
		assertEquals(trainingDuration, newMonthWorkload.getSummaryDuration());

		// Original month should remain unchanged
		MonthWorkload oldMonthWorkload = updatedYearWorkload.getMonths().stream().filter(m -> m.getMonth().equals(3))
		        .findFirst().orElse(null);

		assertNotNull(oldMonthWorkload);
		assertEquals(100, oldMonthWorkload.getSummaryDuration());
	}

	@Test
	void fallbackProcessTrainerWorkload_ShouldNotThrowException() {
		// Arrange
		Exception exception = new RuntimeException("Test exception");

		// Act & Assert - should not throw any exception
		assertDoesNotThrow(() -> trainerWorkloadService.fallbackProcessTrainerWorkload(command, exception));
	}

	@Test
	void fallbackGetTrainerMonthlyWorkload_ShouldReturnEmptyWorkload() {
		// Arrange
		Exception exception = new RuntimeException("Test exception");

		// Act
		TrainerMonthlyWorkload result = trainerWorkloadService.fallbackGetTrainerMonthlyWorkload(username, year, month,
		        transactionId, exception);

		// Assert
		assertNotNull(result);
		assertEquals(username, result.getUsername());
		assertNull(result.getFirstName());
		assertNull(result.getLastName());
		assertNull(result.getIsActive());
		assertEquals(year, result.getYear());
		assertEquals(month, result.getMonth());
		assertEquals(0, result.getSummaryDuration());
	}
}
