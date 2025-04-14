package com.trainerworkloadservice.unit.TrainerWorkload.adapter.input.consumer.listener;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.trainerworkloadservice.TrainerWorkload.adapter.input.consumer.listener.TrainerWorkloadMessageListener;
import com.trainerworkloadservice.TrainerWorkload.adapter.output.queue.message.TrainerWorkloadMessage;
import com.trainerworkloadservice.TrainerWorkload.adapter.output.queue.message.TrainerWorkloadResponseMessage;
import com.trainerworkloadservice.TrainerWorkload.adapter.output.queue.sender.MessageSender;
import com.trainerworkloadservice.TrainerWorkload.application.port.input.LoadTrainerMonthlyWorkloadUseCase;
import com.trainerworkloadservice.TrainerWorkload.application.port.input.ProcessTrainerWorkloadUseCase;
import com.trainerworkloadservice.TrainerWorkload.domain.ActionType;
import com.trainerworkloadservice.TrainerWorkload.domain.TrainerMonthlyWorkload;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadMessageListenerTest {
	@Mock
	private ProcessTrainerWorkloadUseCase processTrainerWorkloadUseCase;

	@Mock
	private LoadTrainerMonthlyWorkloadUseCase loadTrainerMonthlyWorkloadUseCase;

	@Mock
	private MessageSender messageSender;

	@InjectMocks
	private TrainerWorkloadMessageListener listener;

	private TrainerWorkloadMessage message;
	private TrainerMonthlyWorkload workload;
	private String transactionId;

	@BeforeEach
	void setUp() {
		transactionId = "12345";
		message = TrainerWorkloadMessage.builder().username("trainer1").firstName("John").lastName("Doe").isActive(true)
		        .trainingDate(LocalDate.now()).trainingDuration(60).actionType(ActionType.ADD)
		        .transactionId(transactionId).year(2025).month(3).build();

		workload = new TrainerMonthlyWorkload();
		workload.setUsername("trainer1");
		workload.setFirstName("John");
		workload.setLastName("Doe");
		workload.setIsActive(true);
		workload.setYear(2025);
		workload.setMonth(3);
		workload.setSummaryDuration(120);
	}

	@Test
	void handleWorkloadMessage_WithAddAction_SuccessfulProcessing_ShouldProcessMessage() {
		listener.handleWorkloadMessage(message);

		verify(processTrainerWorkloadUseCase).processTrainerWorkload(any());
		verify(messageSender, never()).sendToDeadLetterQueue(any(), anyString());
	}

	@Test
	void handleWorkloadMessage_WithAddAction_WhenDataAccessExceptionThrown_ShouldSendToDLQ() {
		doThrow(new DataAccessException("Database error") {
		}).when(processTrainerWorkloadUseCase).processTrainerWorkload(any());

		listener.handleWorkloadMessage(message);

		verify(messageSender).sendToDeadLetterQueue(eq(message), anyString());
	}

	@Test
	void handleWorkloadMessage_WithAddAction_WhenUnexpectedExceptionThrown_ShouldSendToDLQ() {
		doThrow(new RuntimeException("Unexpected error")).when(processTrainerWorkloadUseCase)
		        .processTrainerWorkload(any());

		listener.handleWorkloadMessage(message);

		verify(messageSender).sendToDeadLetterQueue(eq(message), anyString());
	}

	@Test
	void handleWorkloadMessage_WithGetAction_SuccessfulProcessing_ShouldSendResponse() {
		message.setActionType(ActionType.GET);
		when(loadTrainerMonthlyWorkloadUseCase.loadTrainerMonthlyWorkload(eq("trainer1"), eq(2025), eq(3),
		        eq(transactionId))).thenReturn(workload);

		listener.handleWorkloadMessage(message);

		verify(messageSender).sendResponse(any(TrainerWorkloadResponseMessage.class));
		verify(messageSender, never()).sendToDeadLetterQueue(any(), anyString());
	}

	@Test
	void handleWorkloadMessage_WithGetAction_WhenDataAccessExceptionThrown_ShouldSendErrorResponse() {
		message.setActionType(ActionType.GET);
		when(loadTrainerMonthlyWorkloadUseCase.loadTrainerMonthlyWorkload(eq("trainer1"), eq(2025), eq(3),
		        eq(transactionId))).thenThrow(new DataAccessException("Database error") {
		        });

		listener.handleWorkloadMessage(message);

		verify(messageSender).sendResponse(any(TrainerWorkloadResponseMessage.class));
		verify(messageSender, never()).sendToDeadLetterQueue(any(), anyString());
	}

	@Test
	void handleWorkloadMessage_WithGetAction_WhenUnexpectedExceptionThrown_ShouldSendErrorResponse() {
		message.setActionType(ActionType.GET);
		when(loadTrainerMonthlyWorkloadUseCase.loadTrainerMonthlyWorkload(eq("trainer1"), eq(2025), eq(3),
		        eq(transactionId))).thenThrow(new RuntimeException("Unexpected error"));

		listener.handleWorkloadMessage(message);

		verify(messageSender).sendResponse(any(TrainerWorkloadResponseMessage.class));
		verify(messageSender, never()).sendToDeadLetterQueue(any(), anyString());
	}

	@Test
	void handleWorkloadMessage_WhenValidationFails_ShouldSendToDLQ() {
		message.setUsername(null);

		listener.handleWorkloadMessage(message);

		verify(messageSender).sendToDeadLetterQueue(eq(message), anyString());
		verify(processTrainerWorkloadUseCase, never()).processTrainerWorkload(any());
		verify(loadTrainerMonthlyWorkloadUseCase, never()).loadTrainerMonthlyWorkload(anyString(), anyInt(), anyInt(),
		        anyString());
	}

	@Test
	void processMessageByActionType_WithModifyingAction_ShouldCallProcessModifyingAction() throws Exception {
		Method method = TrainerWorkloadMessageListener.class.getDeclaredMethod("processMessageByActionType",
		        TrainerWorkloadMessage.class, String.class);
		method.setAccessible(true);

		message.setActionType(ActionType.ADD);
		method.invoke(listener, message, transactionId);
	}

	@Test
	void processMessageByActionType_WithGetAction_ShouldCallProcessGetAction() throws Exception {
		Method method = TrainerWorkloadMessageListener.class.getDeclaredMethod("processMessageByActionType",
		        TrainerWorkloadMessage.class, String.class);
		method.setAccessible(true);

		message.setActionType(ActionType.GET);
		when(loadTrainerMonthlyWorkloadUseCase.loadTrainerMonthlyWorkload(eq("trainer1"), eq(2025), eq(3),
		        eq(transactionId))).thenReturn(workload);

		method.invoke(listener, message, transactionId);

		verify(messageSender).sendResponse(any(TrainerWorkloadResponseMessage.class));
	}

	@Test
	void isModifyingAction_WithAddAction_ShouldReturnTrue() throws Exception {
		Method method = TrainerWorkloadMessageListener.class.getDeclaredMethod("isModifyingAction", ActionType.class);
		method.setAccessible(true);

		boolean result = (boolean) method.invoke(listener, ActionType.ADD);
		assertTrue(result);
	}

	@Test
	void isModifyingAction_WithUpdateAction_ShouldReturnTrue() throws Exception {
		Method method = TrainerWorkloadMessageListener.class.getDeclaredMethod("isModifyingAction", ActionType.class);
		method.setAccessible(true);

		boolean result = (boolean) method.invoke(listener, ActionType.UPDATE);
		assertTrue(result);
	}

	@Test
	void isModifyingAction_WithDeleteAction_ShouldReturnTrue() throws Exception {
		Method method = TrainerWorkloadMessageListener.class.getDeclaredMethod("isModifyingAction", ActionType.class);
		method.setAccessible(true);

		boolean result = (boolean) method.invoke(listener, ActionType.DELETE);
		assertTrue(result);
	}

	@Test
	void isModifyingAction_WithGetAction_ShouldReturnFalse() throws Exception {
		Method method = TrainerWorkloadMessageListener.class.getDeclaredMethod("isModifyingAction", ActionType.class);
		method.setAccessible(true);

		boolean result = (boolean) method.invoke(listener, ActionType.GET);
		assertFalse(result);
	}

	@Test
	void processModifyingAction_SuccessfulProcessing_ShouldProcessWorkload() throws Exception {
		Method method = TrainerWorkloadMessageListener.class.getDeclaredMethod("processModifyingAction",
		        TrainerWorkloadMessage.class, String.class);
		method.setAccessible(true);

		method.invoke(listener, message, transactionId);

		verify(processTrainerWorkloadUseCase).processTrainerWorkload(any());
	}

	@Test
	void processModifyingAction_WhenDataAccessExceptionThrown_ShouldHandleException() throws Exception {
		doThrow(new DataAccessException("Database error") {
		}).when(processTrainerWorkloadUseCase).processTrainerWorkload(any());

		Method method = TrainerWorkloadMessageListener.class.getDeclaredMethod("processModifyingAction",
		        TrainerWorkloadMessage.class, String.class);
		method.setAccessible(true);

		method.invoke(listener, message, transactionId);

		verify(messageSender).sendToDeadLetterQueue(eq(message), anyString());
	}

	@Test
	void processModifyingAction_WhenUnexpectedExceptionThrown_ShouldHandleException() throws Exception {
		doThrow(new RuntimeException("Unexpected error")).when(processTrainerWorkloadUseCase)
		        .processTrainerWorkload(any());

		Method method = TrainerWorkloadMessageListener.class.getDeclaredMethod("processModifyingAction",
		        TrainerWorkloadMessage.class, String.class);
		method.setAccessible(true);

		method.invoke(listener, message, transactionId);

		verify(messageSender).sendToDeadLetterQueue(eq(message), anyString());
	}

	@Test
  void processGetAction_SuccessfulProcessing_ShouldSendResponse() throws Exception {
    when(loadTrainerMonthlyWorkloadUseCase.loadTrainerMonthlyWorkload(
            eq("trainer1"), eq(2025), eq(3), eq(transactionId)))
        .thenReturn(workload);

    Method method =
        TrainerWorkloadMessageListener.class.getDeclaredMethod(
            "processGetAction", TrainerWorkloadMessage.class, String.class);
    method.setAccessible(true);

    method.invoke(listener, message, transactionId);

    verify(messageSender).sendResponse(any(TrainerWorkloadResponseMessage.class));
  }

	@Test
  void processGetAction_WhenDataAccessExceptionThrown_ShouldSendErrorResponse() throws Exception {
    when(loadTrainerMonthlyWorkloadUseCase.loadTrainerMonthlyWorkload(
            eq("trainer1"), eq(2025), eq(3), eq(transactionId)))
        .thenThrow(new DataAccessException("Database error") {});

    Method method =
        TrainerWorkloadMessageListener.class.getDeclaredMethod(
            "processGetAction", TrainerWorkloadMessage.class, String.class);
    method.setAccessible(true);

    method.invoke(listener, message, transactionId);

    verify(messageSender).sendResponse(any(TrainerWorkloadResponseMessage.class));
  }

	@Test
  void processGetAction_WhenUnexpectedExceptionThrown_ShouldSendErrorResponse() throws Exception {
    when(loadTrainerMonthlyWorkloadUseCase.loadTrainerMonthlyWorkload(
            eq("trainer1"), eq(2025), eq(3), eq(transactionId)))
        .thenThrow(new RuntimeException("Unexpected error"));

    Method method =
        TrainerWorkloadMessageListener.class.getDeclaredMethod(
            "processGetAction", TrainerWorkloadMessage.class, String.class);
    method.setAccessible(true);

    method.invoke(listener, message, transactionId);

    verify(messageSender).sendResponse(any(TrainerWorkloadResponseMessage.class));
  }

	@Test
  void loadTrainerMonthlyWorkload_ShouldCallUseCase() throws Exception {
    when(loadTrainerMonthlyWorkloadUseCase.loadTrainerMonthlyWorkload(
            eq("trainer1"), eq(2025), eq(3), eq(transactionId)))
        .thenReturn(workload);

    Method method =
        TrainerWorkloadMessageListener.class.getDeclaredMethod(
            "loadTrainerMonthlyWorkload", TrainerWorkloadMessage.class, String.class);
    method.setAccessible(true);

    TrainerMonthlyWorkload result =
        (TrainerMonthlyWorkload) method.invoke(listener, message, transactionId);

    assertEquals(workload, result);
    verify(loadTrainerMonthlyWorkloadUseCase)
        .loadTrainerMonthlyWorkload("trainer1", 2025, 3, transactionId);
  }

	@Test
	void sendSuccessResponse_ShouldSendResponse() throws Exception {
		Method method = TrainerWorkloadMessageListener.class.getDeclaredMethod("sendSuccessResponse",
		        TrainerMonthlyWorkload.class, String.class);
		method.setAccessible(true);

		method.invoke(listener, workload, transactionId);

		verify(messageSender).sendResponse(any(TrainerWorkloadResponseMessage.class));
	}

	@Test
	void createResponseFromWorkload_ShouldCreateResponse() throws Exception {
		Method method = TrainerWorkloadMessageListener.class.getDeclaredMethod("createResponseFromWorkload",
		        TrainerMonthlyWorkload.class, String.class);
		method.setAccessible(true);

		TrainerWorkloadResponseMessage result = (TrainerWorkloadResponseMessage) method.invoke(listener, workload,
		        transactionId);

		assertEquals("trainer1", result.getUsername());
		assertEquals("John", result.getFirstName());
		assertEquals("Doe", result.getLastName());
		assertTrue(result.getIsActive());
		assertEquals(2025, result.getYear());
		assertEquals(3, result.getMonth());
		assertEquals(120, result.getSummaryDuration());
		assertEquals(transactionId, result.getTransactionId());
	}

	@Test
	void handleDataAccessException_WithModifyingAction_ShouldSendToDLQ() throws Exception {
		DataAccessException e = new DataAccessException("Database error") {
		};

		Method method = TrainerWorkloadMessageListener.class.getDeclaredMethod("handleException",
		        TrainerWorkloadMessage.class, String.class, Exception.class, String.class, boolean.class,
		        boolean.class);
		method.setAccessible(true);

		message.setActionType(ActionType.ADD);
		method.invoke(listener, message, transactionId, e, "processing", true, false);

		verify(messageSender).sendToDeadLetterQueue(eq(message), anyString());
	}

	@Test
	void handleDataAccessException_WithGetAction_ShouldNotSendToDLQ() throws Exception {
		DataAccessException e = new DataAccessException("Database error") {
		};

		Method method = TrainerWorkloadMessageListener.class.getDeclaredMethod("handleException",
		        TrainerWorkloadMessage.class, String.class, Exception.class, String.class, boolean.class,
		        boolean.class);
		method.setAccessible(true);

		message.setActionType(ActionType.GET);
		method.invoke(listener, message, transactionId, e, "loading", false, true);

		verify(messageSender, never()).sendToDeadLetterQueue(any(), anyString());
		verify(messageSender).sendResponse(any(TrainerWorkloadResponseMessage.class));
	}

	@Test
	void handleUnexpectedException_WithModifyingAction_ShouldSendToDLQ() throws Exception {
		Exception e = new RuntimeException("Unexpected error");

		Method method = TrainerWorkloadMessageListener.class.getDeclaredMethod("handleException",
		        TrainerWorkloadMessage.class, String.class, Exception.class, String.class, boolean.class,
		        boolean.class);
		method.setAccessible(true);

		message.setActionType(ActionType.ADD);
		method.invoke(listener, message, transactionId, e, "processing", true, false);

		verify(messageSender).sendToDeadLetterQueue(eq(message), anyString());
	}

	@Test
	void handleUnexpectedException_WithGetAction_ShouldNotSendToDLQ() throws Exception {
		Exception e = new RuntimeException("Unexpected error");

		Method method = TrainerWorkloadMessageListener.class.getDeclaredMethod("handleException",
		        TrainerWorkloadMessage.class, String.class, Exception.class, String.class, boolean.class,
		        boolean.class);
		method.setAccessible(true);

		message.setActionType(ActionType.GET);
		method.invoke(listener, message, transactionId, e, "loading", false, true);

		verify(messageSender, never()).sendToDeadLetterQueue(any(), anyString());
		verify(messageSender).sendResponse(any(TrainerWorkloadResponseMessage.class));
	}

	@Test
	void validateActionSpecificFields_WithGetActionMissingFields_ShouldThrowException() throws Exception {
		message.setActionType(ActionType.GET);
		message.setYear(null);

		Method method = TrainerWorkloadMessageListener.class.getDeclaredMethod("validateActionSpecificFields",
		        TrainerWorkloadMessage.class);
		method.setAccessible(true);

		InvocationTargetException thrown = assertThrows(InvocationTargetException.class,
		        () -> method.invoke(listener, message));
		assertInstanceOf(IllegalArgumentException.class, thrown.getCause(),
		        "Expected cause to be IllegalArgumentException");
		assertEquals("Year and month are required for GET action", thrown.getCause().getMessage());
	}

	@Test
	void validateMessage_WhenActionTypeIsNull_ShouldThrowException() throws Exception {
		message.setActionType(null);

		Method method = TrainerWorkloadMessageListener.class.getDeclaredMethod("validateMessage",
		        TrainerWorkloadMessage.class);
		method.setAccessible(true);

		InvocationTargetException thrown = assertThrows(InvocationTargetException.class,
		        () -> method.invoke(listener, message));
		assertInstanceOf(IllegalArgumentException.class, thrown.getCause(),
		        "Expected cause to be IllegalArgumentException");
		assertEquals("Action type is required", thrown.getCause().getMessage());
	}

	@Test
	void validateMessage_WhenValidMessage_ShouldNotThrowException() throws Exception {
		Method method = TrainerWorkloadMessageListener.class.getDeclaredMethod("validateMessage",
		        TrainerWorkloadMessage.class);
		method.setAccessible(true);

		method.invoke(listener, message);
	}

	@Test
	void validateActionSpecificFields_WithAddActionValidFields_ShouldNotThrow() throws Exception {
		message.setActionType(ActionType.ADD);
		message.setTrainingDate(LocalDate.now());
		message.setTrainingDuration(60);

		Method method = TrainerWorkloadMessageListener.class.getDeclaredMethod("validateActionSpecificFields",
		        TrainerWorkloadMessage.class);
		method.setAccessible(true);

		method.invoke(listener, message);
	}

	@Test
	void validateActionSpecificFields_WithDeleteAction_ShouldNotThrowException() throws Exception {
		message.setActionType(ActionType.DELETE);
		message.setTrainingDate(null);
		message.setTrainingDuration(null);

		Method method = TrainerWorkloadMessageListener.class.getDeclaredMethod("validateActionSpecificFields",
		        TrainerWorkloadMessage.class);
		method.setAccessible(true);

		method.invoke(listener, message);
	}

	@Test
	void sendErrorResponse_ShouldSendErrorResponse() throws Exception {
		Method method = TrainerWorkloadMessageListener.class.getDeclaredMethod("sendErrorResponse",
		        TrainerWorkloadMessage.class, String.class, String.class);
		method.setAccessible(true);

		method.invoke(listener, message, transactionId, "Test error");

		verify(messageSender).sendResponse(any(TrainerWorkloadResponseMessage.class));
	}
}
