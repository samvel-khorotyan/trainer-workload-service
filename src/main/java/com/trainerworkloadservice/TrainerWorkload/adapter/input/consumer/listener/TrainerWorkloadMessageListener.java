package com.trainerworkloadservice.TrainerWorkload.adapter.input.consumer.listener;

import com.trainerworkloadservice.TrainerWorkload.adapter.output.queue.message.TrainerWorkloadMessage;
import com.trainerworkloadservice.TrainerWorkload.adapter.output.queue.message.TrainerWorkloadResponseMessage;
import com.trainerworkloadservice.TrainerWorkload.application.port.input.LoadTrainerMonthlyWorkloadUseCase;
import com.trainerworkloadservice.TrainerWorkload.application.port.input.ProcessTrainerWorkloadUseCase;
import com.trainerworkloadservice.TrainerWorkload.domain.ActionType;
import com.trainerworkloadservice.TrainerWorkload.domain.TrainerMonthlyWorkload;
import com.trainerworkloadservice.configuration.messaging.JmsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrainerWorkloadMessageListener {
	private final ProcessTrainerWorkloadUseCase processTrainerWorkloadUseCase;
	private final LoadTrainerMonthlyWorkloadUseCase loadTrainerMonthlyWorkloadUseCase;
	private final JmsTemplate jmsTemplate;

	@JmsListener(destination = JmsConfig.TRAINER_WORKLOAD_QUEUE)
	public void handleWorkloadMessage(TrainerWorkloadMessage message) {
		String transactionId = message.getTransactionId();
		log.info("Transaction [{}]: Received workload message for trainer: {}, action: {}", transactionId,
		        message.getUsername(), message.getActionType());

		try {
			validateMessage(message);
			processMessageByActionType(message, transactionId);
		} catch (IllegalArgumentException e) {
			log.error("Transaction [{}]: Invalid message: {}", transactionId, e.getMessage());
			sendToDeadLetterQueue(message, "Validation error: " + e.getMessage());
		} catch (Exception e) {
			log.error("Transaction [{}]: Error processing workload message: {}", transactionId, e.getMessage(), e);
			sendToDeadLetterQueue(message, "Processing error: " + e.getMessage());
		}
	}

	private void processMessageByActionType(TrainerWorkloadMessage message, String transactionId) {
		if (isModifyingAction(message.getActionType())) {
			processModifyingAction(message, transactionId);
		} else if (ActionType.GET.equals(message.getActionType())) {
			processGetAction(message, transactionId);
		}
	}

	private boolean isModifyingAction(ActionType actionType) {
		return ActionType.ADD.equals(actionType) || ActionType.UPDATE.equals(actionType)
		        || ActionType.DELETE.equals(actionType);
	}

	private void processModifyingAction(TrainerWorkloadMessage message, String transactionId) {
		try {
			processTrainerWorkloadUseCase.processTrainerWorkload(message.toCommand(transactionId));
			log.info("Transaction [{}]: Successfully processed workload for trainer: {}", transactionId,
			        message.getUsername());
		} catch (DataAccessException e) {
			handleDataAccessException(message, transactionId, e, "processing");
		} catch (Exception e) {
			handleUnexpectedException(message, transactionId, e, "processing");
		}
	}

	private void processGetAction(TrainerWorkloadMessage message, String transactionId) {
		try {
			TrainerMonthlyWorkload workload = loadTrainerMonthlyWorkload(message, transactionId);
			sendSuccessResponse(workload, transactionId);
		} catch (DataAccessException e) {
			handleDataAccessException(message, transactionId, e, "loading");
			sendErrorResponse(message, transactionId, "Database error: " + e.getMessage());
		} catch (Exception e) {
			handleUnexpectedException(message, transactionId, e, "loading");
			sendErrorResponse(message, transactionId, "Unexpected error: " + e.getMessage());
		}
	}

	private TrainerMonthlyWorkload loadTrainerMonthlyWorkload(TrainerWorkloadMessage message, String transactionId) {
		return loadTrainerMonthlyWorkloadUseCase.loadTrainerMonthlyWorkload(message.getUsername(), message.getYear(),
		        message.getMonth(), transactionId);
	}

	private void sendSuccessResponse(TrainerMonthlyWorkload workload, String transactionId) {
		TrainerWorkloadResponseMessage response = createResponseFromWorkload(workload, transactionId);
		jmsTemplate.convertAndSend(JmsConfig.TRAINER_WORKLOAD_RESPONSE_QUEUE, response);
		log.info("Transaction [{}]: Sent workload response for trainer: {}", transactionId, workload.getUsername());
	}

	private TrainerWorkloadResponseMessage createResponseFromWorkload(TrainerMonthlyWorkload workload,
	        String transactionId) {
		return TrainerWorkloadResponseMessage.builder().username(workload.getUsername())
		        .firstName(workload.getFirstName()).lastName(workload.getLastName()).isActive(workload.getIsActive())
		        .year(workload.getYear()).month(workload.getMonth()).summaryDuration(workload.getSummaryDuration())
		        .transactionId(transactionId).build();
	}

	private void handleDataAccessException(TrainerWorkloadMessage message, String transactionId, DataAccessException e,
	        String operation) {
		log.error("Transaction [{}]: Database error {} workload: {}", transactionId, operation, e.getMessage(), e);
		if (!ActionType.GET.equals(message.getActionType())) {
			sendToDeadLetterQueue(message, "Database error: " + e.getMessage());
		}
	}

	private void handleUnexpectedException(TrainerWorkloadMessage message, String transactionId, Exception e,
	        String operation) {
		log.error("Transaction [{}]: Unexpected error {} workload: {}", transactionId, operation, e.getMessage(), e);
		if (!ActionType.GET.equals(message.getActionType())) {
			sendToDeadLetterQueue(message, "Unexpected error: " + e.getMessage());
		}
	}

	private void validateMessage(TrainerWorkloadMessage message) {
		if (message.getUsername() == null || message.getUsername().isEmpty()) {
			throw new IllegalArgumentException("Username is required");
		}

		if (message.getActionType() == null) {
			throw new IllegalArgumentException("Action type is required");
		}

		validateActionSpecificFields(message);
	}

	private void validateActionSpecificFields(TrainerWorkloadMessage message) {
		if (isModifyingAction(message.getActionType()) && !ActionType.DELETE.equals(message.getActionType())
		        && (message.getTrainingDate() == null || message.getTrainingDuration() == null)) {
			throw new IllegalArgumentException("Training date and duration are required for ADD/UPDATE actions");
		}

		if (ActionType.GET.equals(message.getActionType())
		        && (message.getYear() == null || message.getMonth() == null)) {
			throw new IllegalArgumentException("Year and month are required for GET action");
		}
	}

	private void sendToDeadLetterQueue(TrainerWorkloadMessage message, String errorMessage) {
		String dlqMessage = String.format("Error: %s, Original message: %s", errorMessage, message);
		jmsTemplate.convertAndSend(JmsConfig.DEAD_LETTER_QUEUE, dlqMessage);
	}

	private void sendErrorResponse(TrainerWorkloadMessage message, String transactionId, String errorMessage) {
		TrainerWorkloadResponseMessage errorResponse = TrainerWorkloadResponseMessage.builder()
		        .username(message.getUsername()).firstName("Error").lastName(errorMessage).isActive(false)
		        .year(message.getYear()).month(message.getMonth()).summaryDuration(0).transactionId(transactionId)
		        .build();

		jmsTemplate.convertAndSend(JmsConfig.TRAINER_WORKLOAD_RESPONSE_QUEUE, errorResponse);
	}
}
