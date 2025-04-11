package com.trainerworkloadservice.TrainerWorkload.adapter.input.consumer.listener;

import com.trainerworkloadservice.TrainerWorkload.adapter.output.queue.message.TrainerWorkloadMessage;
import com.trainerworkloadservice.TrainerWorkload.adapter.output.queue.message.TrainerWorkloadResponseMessage;
import com.trainerworkloadservice.TrainerWorkload.adapter.output.queue.sender.MessageSender;
import com.trainerworkloadservice.TrainerWorkload.application.port.input.LoadTrainerMonthlyWorkloadUseCase;
import com.trainerworkloadservice.TrainerWorkload.application.port.input.ProcessTrainerWorkloadUseCase;
import com.trainerworkloadservice.TrainerWorkload.domain.ActionType;
import com.trainerworkloadservice.TrainerWorkload.domain.TrainerMonthlyWorkload;
import com.trainerworkloadservice.configuration.messaging.JmsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrainerWorkloadMessageListener {
	private static final String USERNAME_REQUIRED = "Username is required";
	private static final String ACTION_TYPE_REQUIRED = "Action type is required";
	private static final String TRAINING_DURATION_NEGATIVE = "Training duration cannot be negative";
	private static final String TRAINING_DATE_DURATION_REQUIRED = "Training date and duration are required for ADD/UPDATE actions";
	private static final String YEAR_MONTH_REQUIRED = "Year and month are required for GET action";
	private static final String DATABASE_ERROR_PREFIX = "Database error: ";
	private static final String UNEXPECTED_ERROR_PREFIX = "Unexpected error: ";
	private static final String VALIDATION_ERROR_PREFIX = "Validation error: ";
	private static final String PROCESSING_ERROR_PREFIX = "Processing error: ";

	private final ProcessTrainerWorkloadUseCase processTrainerWorkloadUseCase;
	private final LoadTrainerMonthlyWorkloadUseCase loadTrainerMonthlyWorkloadUseCase;
	private final MessageSender messageSender;

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
			messageSender.sendToDeadLetterQueue(message, VALIDATION_ERROR_PREFIX + e.getMessage());
		} catch (Exception e) {
			log.error("Transaction [{}]: Error processing workload message: {}", transactionId, e.getMessage(), e);
			messageSender.sendToDeadLetterQueue(message, PROCESSING_ERROR_PREFIX + e.getMessage());
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
		} catch (Exception e) {
			handleException(message, transactionId, e, "processing", true, false);
		}
	}

	private void processGetAction(TrainerWorkloadMessage message, String transactionId) {
		try {
			TrainerMonthlyWorkload workload = loadTrainerMonthlyWorkload(message, transactionId);
			sendSuccessResponse(workload, transactionId);
		} catch (Exception e) {
			handleException(message, transactionId, e, "loading", false, true);
		}
	}

	private TrainerMonthlyWorkload loadTrainerMonthlyWorkload(TrainerWorkloadMessage message, String transactionId) {
		return loadTrainerMonthlyWorkloadUseCase.loadTrainerMonthlyWorkload(message.getUsername(), message.getYear(),
		        message.getMonth(), transactionId);
	}

	private void sendSuccessResponse(TrainerMonthlyWorkload workload, String transactionId) {
		TrainerWorkloadResponseMessage response = createResponseFromWorkload(workload, transactionId);
		messageSender.sendResponse(response);
		log.info("Transaction [{}]: Sent workload response for trainer: {}", transactionId, workload.getUsername());
	}

	private TrainerWorkloadResponseMessage createResponseFromWorkload(TrainerMonthlyWorkload workload,
	        String transactionId) {
		return TrainerWorkloadResponseMessage.builder().username(workload.getUsername())
		        .firstName(workload.getFirstName()).lastName(workload.getLastName()).isActive(workload.getIsActive())
		        .year(workload.getYear()).month(workload.getMonth()).summaryDuration(workload.getSummaryDuration())
		        .transactionId(transactionId).error(false).build();
	}

	private void handleException(TrainerWorkloadMessage message, String transactionId, Exception e, String operation,
	        boolean sendToDLQ, boolean sendErrorResponse) {
		String errorPrefix = e instanceof DataAccessException ? DATABASE_ERROR_PREFIX : UNEXPECTED_ERROR_PREFIX;
		String errorMessage = errorPrefix + e.getMessage();

		log.error("Transaction [{}]: Error {} workload: {}", transactionId, operation, e.getMessage(), e);

		if (sendToDLQ) {
			messageSender.sendToDeadLetterQueue(message, errorMessage);
		}

		if (sendErrorResponse) {
			sendErrorResponse(message, transactionId, errorMessage);
		}
	}

	private void validateMessage(TrainerWorkloadMessage message) {
		if (message.getUsername() == null || message.getUsername().isEmpty()) {
			throw new IllegalArgumentException(USERNAME_REQUIRED);
		}

		if (message.getActionType() == null) {
			throw new IllegalArgumentException(ACTION_TYPE_REQUIRED);
		}

		if (message.getTrainingDuration() != null && message.getTrainingDuration() < 0) {
			throw new IllegalArgumentException(TRAINING_DURATION_NEGATIVE);
		}

		validateActionSpecificFields(message);
	}

	private void validateActionSpecificFields(TrainerWorkloadMessage message) {
		if (isModifyingAction(message.getActionType()) && !ActionType.DELETE.equals(message.getActionType())
		        && (message.getTrainingDate() == null || message.getTrainingDuration() == null)) {
			throw new IllegalArgumentException(TRAINING_DATE_DURATION_REQUIRED);
		}

		if (ActionType.GET.equals(message.getActionType())
		        && (message.getYear() == null || message.getMonth() == null)) {
			throw new IllegalArgumentException(YEAR_MONTH_REQUIRED);
		}
	}

	private void sendErrorResponse(TrainerWorkloadMessage message, String transactionId, String errorMessage) {
		TrainerWorkloadResponseMessage errorResponse = TrainerWorkloadResponseMessage.builder()
		        .username(message.getUsername()).year(message.getYear()).month(message.getMonth()).summaryDuration(0)
		        .transactionId(transactionId).error(true).errorMessage(errorMessage).build();

		messageSender.sendResponse(errorResponse);
	}
}
