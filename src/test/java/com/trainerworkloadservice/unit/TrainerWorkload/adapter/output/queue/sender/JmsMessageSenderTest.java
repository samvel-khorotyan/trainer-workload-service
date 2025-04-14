package com.trainerworkloadservice.unit.TrainerWorkload.adapter.output.queue.sender;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

import com.trainerworkloadservice.TrainerWorkload.adapter.output.queue.message.TrainerWorkloadMessage;
import com.trainerworkloadservice.TrainerWorkload.adapter.output.queue.message.TrainerWorkloadResponseMessage;
import com.trainerworkloadservice.TrainerWorkload.adapter.output.queue.sender.JmsMessageSender;
import com.trainerworkloadservice.TrainerWorkload.domain.ActionType;
import com.trainerworkloadservice.configuration.messaging.JmsConfig;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;

@ExtendWith(MockitoExtension.class)
class JmsMessageSenderTest {
	@Mock
	private JmsTemplate jmsTemplate;

	@InjectMocks
	private JmsMessageSender messageSender;

	@Captor
	private ArgumentCaptor<String> dlqMessageCaptor;

	private TrainerWorkloadMessage workloadMessage;
	private TrainerWorkloadResponseMessage responseMessage;
	private String errorMessage;

	@BeforeEach
	void setUp() {
		String transactionId = "12345";
		errorMessage = "Test error message";

		workloadMessage = TrainerWorkloadMessage.builder().username("trainer1").firstName("John").lastName("Doe")
		        .isActive(true).trainingDate(LocalDate.now()).trainingDuration(60).actionType(ActionType.ADD)
		        .transactionId(transactionId).year(2025).month(3).build();

		responseMessage = TrainerWorkloadResponseMessage.builder().username("trainer1").firstName("John")
		        .lastName("Doe").isActive(true).year(2025).month(3).summaryDuration(120).transactionId(transactionId)
		        .error(false).build();
	}

	@Test
	void sendToDeadLetterQueue_ShouldSendFormattedMessageToDLQ() {
		messageSender.sendToDeadLetterQueue(workloadMessage, errorMessage);

		verify(jmsTemplate).convertAndSend(eq(JmsConfig.DEAD_LETTER_QUEUE), dlqMessageCaptor.capture());

		String capturedMessage = dlqMessageCaptor.getValue();
		assert capturedMessage.contains(errorMessage);
		assert capturedMessage.contains(workloadMessage.toString());
	}

	@Test
	void sendResponse_ShouldSendResponseToResponseQueue() {
		messageSender.sendResponse(responseMessage);

		verify(jmsTemplate).convertAndSend(JmsConfig.TRAINER_WORKLOAD_RESPONSE_QUEUE, responseMessage);
	}

	@Test
	void sendToDeadLetterQueue_WithNullMessage_ShouldStillSendToDLQ() {
		messageSender.sendToDeadLetterQueue(null, errorMessage);

		verify(jmsTemplate).convertAndSend(eq(JmsConfig.DEAD_LETTER_QUEUE), dlqMessageCaptor.capture());

		String capturedMessage = dlqMessageCaptor.getValue();
		assert capturedMessage.contains(errorMessage);
		assert capturedMessage.contains("null");
	}

	@Test
	void sendToDeadLetterQueue_WithNullErrorMessage_ShouldStillSendToDLQ() {
		messageSender.sendToDeadLetterQueue(workloadMessage, null);

		verify(jmsTemplate).convertAndSend(eq(JmsConfig.DEAD_LETTER_QUEUE), dlqMessageCaptor.capture());

		String capturedMessage = dlqMessageCaptor.getValue();
		assert capturedMessage.contains("null");
		assert capturedMessage.contains(workloadMessage.toString());
	}

	@Test
	void sendResponse_WithErrorResponse_ShouldSendToResponseQueue() {
		responseMessage.setError(true);
		responseMessage.setErrorMessage("Error occurred");

		messageSender.sendResponse(responseMessage);

		verify(jmsTemplate).convertAndSend(JmsConfig.TRAINER_WORKLOAD_RESPONSE_QUEUE, responseMessage);
	}
}
