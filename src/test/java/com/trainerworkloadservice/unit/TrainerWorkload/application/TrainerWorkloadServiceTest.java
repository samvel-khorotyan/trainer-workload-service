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

    trainerWorkloadService.processTrainerWorkload(command);

    ArgumentCaptor<TrainerWorkload> workloadCaptor = ArgumentCaptor.forClass(TrainerWorkload.class);
    verify(updateTrainerWorkloadPort).save(workloadCaptor.capture());

    TrainerWorkload savedWorkload = workloadCaptor.getValue();
    assertEquals(username, savedWorkload.getUsername());
    assertEquals(firstName, savedWorkload.getFirstName());
    assertEquals(lastName, savedWorkload.getLastName());
    assertEquals(isActive, savedWorkload.getIsActive());

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
    when(loadTrainerWorkloadPort.findByUsername(username)).thenReturn(existingTrainerWorkload);

    trainerWorkloadService.processTrainerWorkload(command);

    ArgumentCaptor<TrainerWorkload> workloadCaptor = ArgumentCaptor.forClass(TrainerWorkload.class);
    verify(updateTrainerWorkloadPort).save(workloadCaptor.capture());

    TrainerWorkload savedWorkload = workloadCaptor.getValue();
    assertEquals(username, savedWorkload.getUsername());
    assertEquals(firstName, savedWorkload.getFirstName());
    assertEquals(lastName, savedWorkload.getLastName());
    assertEquals(isActive, savedWorkload.getIsActive());

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
		MonthWorkload existingMonthWorkload = MonthWorkload.builder().month(month).summaryDuration(100).build();

		YearWorkload existingYearWorkload = YearWorkload.builder().year(year)
		        .months(new ArrayList<>(Collections.singletonList(existingMonthWorkload))).build();

		existingTrainerWorkload.setYears(new ArrayList<>(Collections.singletonList(existingYearWorkload)));

		when(loadTrainerWorkloadPort.findByUsername(username)).thenReturn(existingTrainerWorkload);

		trainerWorkloadService.processTrainerWorkload(command);

		ArgumentCaptor<TrainerWorkload> workloadCaptor = ArgumentCaptor.forClass(TrainerWorkload.class);
		verify(updateTrainerWorkloadPort).save(workloadCaptor.capture());

		TrainerWorkload savedWorkload = workloadCaptor.getValue();
		MonthWorkload updatedMonthWorkload = savedWorkload.getYears().get(0).getMonths().get(0);

		assertEquals(160, updatedMonthWorkload.getSummaryDuration());
	}

	@Test
	void processTrainerWorkload_ShouldSubtractDuration_WhenActionTypeIsDelete() {
		MonthWorkload existingMonthWorkload = MonthWorkload.builder().month(month).summaryDuration(100).build();

		YearWorkload existingYearWorkload = YearWorkload.builder().year(year)
		        .months(new ArrayList<>(Collections.singletonList(existingMonthWorkload))).build();

		existingTrainerWorkload.setYears(new ArrayList<>(Collections.singletonList(existingYearWorkload)));

		when(loadTrainerWorkloadPort.findByUsername(username)).thenReturn(existingTrainerWorkload);

		command = ProcessTrainerWorkloadCommand.builder().username(username).firstName(firstName).lastName(lastName)
		        .isActive(isActive).trainingDate(trainingDate).trainingDuration(trainingDuration)
		        .actionType(ActionType.DELETE).transactionId(transactionId).build();

		trainerWorkloadService.processTrainerWorkload(command);

		ArgumentCaptor<TrainerWorkload> workloadCaptor = ArgumentCaptor.forClass(TrainerWorkload.class);
		verify(updateTrainerWorkloadPort).save(workloadCaptor.capture());

		TrainerWorkload savedWorkload = workloadCaptor.getValue();
		MonthWorkload updatedMonthWorkload = savedWorkload.getYears().get(0).getMonths().get(0);

		assertEquals(40, updatedMonthWorkload.getSummaryDuration());
	}

	@Test
	void processTrainerWorkload_ShouldNotAllowNegativeDuration_WhenDeletingMoreThanExists() {
		MonthWorkload existingMonthWorkload = MonthWorkload.builder().month(month).summaryDuration(30).build();

		YearWorkload existingYearWorkload = YearWorkload.builder().year(year)
		        .months(new ArrayList<>(Collections.singletonList(existingMonthWorkload))).build();

		existingTrainerWorkload.setYears(new ArrayList<>(Collections.singletonList(existingYearWorkload)));

		when(loadTrainerWorkloadPort.findByUsername(username)).thenReturn(existingTrainerWorkload);

		command = ProcessTrainerWorkloadCommand.builder().username(username).firstName(firstName).lastName(lastName)
		        .isActive(isActive).trainingDate(trainingDate).trainingDuration(trainingDuration)
		        .actionType(ActionType.DELETE).transactionId(transactionId).build();

		trainerWorkloadService.processTrainerWorkload(command);

		ArgumentCaptor<TrainerWorkload> workloadCaptor = ArgumentCaptor.forClass(TrainerWorkload.class);
		verify(updateTrainerWorkloadPort).save(workloadCaptor.capture());

		TrainerWorkload savedWorkload = workloadCaptor.getValue();
		MonthWorkload updatedMonthWorkload = savedWorkload.getYears().get(0).getMonths().get(0);

		assertEquals(0, updatedMonthWorkload.getSummaryDuration());
	}

	@Test
	void loadTrainerMonthlyWorkload_ShouldReturnWorkload_WhenDataExists() {
		MonthWorkload existingMonthWorkload = MonthWorkload.builder().month(month).summaryDuration(120).build();

		YearWorkload existingYearWorkload = YearWorkload.builder().year(year)
		        .months(new ArrayList<>(Collections.singletonList(existingMonthWorkload))).build();

		existingTrainerWorkload.setYears(new ArrayList<>(Collections.singletonList(existingYearWorkload)));
		existingTrainerWorkload.setFirstName(firstName);
		existingTrainerWorkload.setLastName(lastName);
		existingTrainerWorkload.setIsActive(isActive);

		when(loadTrainerWorkloadPort.findByUsername(username)).thenReturn(existingTrainerWorkload);

		TrainerMonthlyWorkload result = trainerWorkloadService.loadTrainerMonthlyWorkload(username, year, month,
		        transactionId);

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
		existingTrainerWorkload.setFirstName(firstName);
		existingTrainerWorkload.setLastName(lastName);
		existingTrainerWorkload.setIsActive(isActive);

		when(loadTrainerWorkloadPort.findByUsername(username)).thenReturn(existingTrainerWorkload);

		TrainerMonthlyWorkload result = trainerWorkloadService.loadTrainerMonthlyWorkload(username, year, month,
		        transactionId);

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
		YearWorkload existingYearWorkload = YearWorkload.builder().year(year).months(new ArrayList<>()).build();

		existingTrainerWorkload.setYears(new ArrayList<>(Collections.singletonList(existingYearWorkload)));
		existingTrainerWorkload.setFirstName(firstName);
		existingTrainerWorkload.setLastName(lastName);
		existingTrainerWorkload.setIsActive(isActive);

		when(loadTrainerWorkloadPort.findByUsername(username)).thenReturn(existingTrainerWorkload);

		TrainerMonthlyWorkload result = trainerWorkloadService.loadTrainerMonthlyWorkload(username, year, month,
		        transactionId);

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
    when(loadTrainerWorkloadPort.findByUsername(username))
        .thenThrow(new TrainerWorkloadNotFoundException("Not found"));

    TrainerMonthlyWorkload result =
        trainerWorkloadService.loadTrainerMonthlyWorkload(username, year, month, transactionId);

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
		MonthWorkload existingMonthWorkload = MonthWorkload.builder().month(3).summaryDuration(100).build();

		YearWorkload existingYearWorkload = YearWorkload.builder().year(2022)
		        .months(new ArrayList<>(Collections.singletonList(existingMonthWorkload))).build();

		existingTrainerWorkload.setYears(new ArrayList<>(Collections.singletonList(existingYearWorkload)));

		when(loadTrainerWorkloadPort.findByUsername(username)).thenReturn(existingTrainerWorkload);

		trainerWorkloadService.processTrainerWorkload(command);

		ArgumentCaptor<TrainerWorkload> workloadCaptor = ArgumentCaptor.forClass(TrainerWorkload.class);
		verify(updateTrainerWorkloadPort).save(workloadCaptor.capture());

		TrainerWorkload savedWorkload = workloadCaptor.getValue();

		assertEquals(2, savedWorkload.getYears().size());

		YearWorkload newYearWorkload = savedWorkload.getYears().stream().filter(y -> y.getYear().equals(year))
		        .findFirst().orElse(null);

		assertNotNull(newYearWorkload);
		assertEquals(1, newYearWorkload.getMonths().size());

		MonthWorkload newMonthWorkload = newYearWorkload.getMonths().get(0);
		assertEquals(month, newMonthWorkload.getMonth());
		assertEquals(trainingDuration, newMonthWorkload.getSummaryDuration());

		YearWorkload oldYearWorkload = savedWorkload.getYears().stream().filter(y -> y.getYear().equals(2022))
		        .findFirst().orElse(null);

		assertNotNull(oldYearWorkload);
		assertEquals(1, oldYearWorkload.getMonths().size());
		assertEquals(3, oldYearWorkload.getMonths().get(0).getMonth());
		assertEquals(100, oldYearWorkload.getMonths().get(0).getSummaryDuration());
	}

	@Test
	void processTrainerWorkload_ShouldAddNewMonth_ToExistingYear() {
		MonthWorkload existingMonthWorkload = MonthWorkload.builder().month(3).summaryDuration(100).build();

		YearWorkload existingYearWorkload = YearWorkload.builder().year(year)
		        .months(new ArrayList<>(Collections.singletonList(existingMonthWorkload))).build();

		existingTrainerWorkload.setYears(new ArrayList<>(Collections.singletonList(existingYearWorkload)));

		when(loadTrainerWorkloadPort.findByUsername(username)).thenReturn(existingTrainerWorkload);

		trainerWorkloadService.processTrainerWorkload(command);

		ArgumentCaptor<TrainerWorkload> workloadCaptor = ArgumentCaptor.forClass(TrainerWorkload.class);
		verify(updateTrainerWorkloadPort).save(workloadCaptor.capture());

		TrainerWorkload savedWorkload = workloadCaptor.getValue();

		assertEquals(1, savedWorkload.getYears().size());

		YearWorkload updatedYearWorkload = savedWorkload.getYears().get(0);
		assertEquals(year, updatedYearWorkload.getYear());

		assertEquals(2, updatedYearWorkload.getMonths().size());

		MonthWorkload newMonthWorkload = updatedYearWorkload.getMonths().stream()
		        .filter(m -> m.getMonth().equals(month)).findFirst().orElse(null);

		assertNotNull(newMonthWorkload);
		assertEquals(trainingDuration, newMonthWorkload.getSummaryDuration());

		MonthWorkload oldMonthWorkload = updatedYearWorkload.getMonths().stream().filter(m -> m.getMonth().equals(3))
		        .findFirst().orElse(null);

		assertNotNull(oldMonthWorkload);
		assertEquals(100, oldMonthWorkload.getSummaryDuration());
	}

	@Test
	void processTrainerWorkload_ShouldUpdateDuration_WhenActionTypeIsUpdate() {
		MonthWorkload existingMonthWorkload = MonthWorkload.builder().month(month).summaryDuration(100).build();

		YearWorkload existingYearWorkload = YearWorkload.builder().year(year)
		        .months(new ArrayList<>(Collections.singletonList(existingMonthWorkload))).build();

		existingTrainerWorkload.setYears(new ArrayList<>(Collections.singletonList(existingYearWorkload)));

		when(loadTrainerWorkloadPort.findByUsername(username)).thenReturn(existingTrainerWorkload);

		command = ProcessTrainerWorkloadCommand.builder().username(username).firstName(firstName).lastName(lastName)
		        .isActive(isActive).trainingDate(trainingDate).trainingDuration(trainingDuration)
		        .actionType(ActionType.UPDATE).transactionId(transactionId).build();

		trainerWorkloadService.processTrainerWorkload(command);

		ArgumentCaptor<TrainerWorkload> workloadCaptor = ArgumentCaptor.forClass(TrainerWorkload.class);
		verify(updateTrainerWorkloadPort).save(workloadCaptor.capture());

		TrainerWorkload savedWorkload = workloadCaptor.getValue();
		MonthWorkload updatedMonthWorkload = savedWorkload.getYears().get(0).getMonths().get(0);

		assertEquals(trainingDuration, updatedMonthWorkload.getSummaryDuration());
	}

	@Test
  void processTrainerWorkload_ShouldThrowRuntimeException_WhenLoadPortThrowsException() {
    when(loadTrainerWorkloadPort.findByUsername(username))
        .thenThrow(new RuntimeException("Database error"));

    assertThrows(
        RuntimeException.class, () -> trainerWorkloadService.processTrainerWorkload(command));
  }

	@Test
  void processTrainerWorkload_ShouldThrowRuntimeException_WhenUpdatePortThrowsException() {
    when(loadTrainerWorkloadPort.findByUsername(username)).thenReturn(existingTrainerWorkload);
    doThrow(new RuntimeException("Database error"))
        .when(updateTrainerWorkloadPort)
        .save(any(TrainerWorkload.class));

    assertThrows(
        RuntimeException.class, () -> trainerWorkloadService.processTrainerWorkload(command));
  }

	@Test
  void
      loadTrainerMonthlyWorkload_ShouldThrowRuntimeException_WhenLoadPortThrowsUnexpectedException() {
    when(loadTrainerWorkloadPort.findByUsername(username))
        .thenThrow(new RuntimeException("Database error"));

    assertThrows(
        RuntimeException.class,
        () ->
            trainerWorkloadService.loadTrainerMonthlyWorkload(
                username, year, month, transactionId));
  }

	@Test
  void processTrainerWorkload_ShouldThrowRuntimeException_WhenFactoryThrowsException() {
    when(loadTrainerWorkloadPort.findByUsername(username))
        .thenThrow(new TrainerWorkloadNotFoundException("Not found"));
    when(trainerWorkloadFactory.createFrom(command))
        .thenThrow(new RuntimeException("Factory error"));

    assertThrows(
        RuntimeException.class, () -> trainerWorkloadService.processTrainerWorkload(command));
  }
}
