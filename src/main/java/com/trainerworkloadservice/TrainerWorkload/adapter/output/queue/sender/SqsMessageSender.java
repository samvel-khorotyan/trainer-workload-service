package com.trainerworkloadservice.TrainerWorkload.adapter.output.queue.sender;

import com.trainerworkloadservice.TrainerWorkload.adapter.output.queue.message.TrainerWorkloadMessage;
import com.trainerworkloadservice.TrainerWorkload.adapter.output.queue.message.TrainerWorkloadResponseMessage;
import com.trainerworkloadservice.configuration.messaging.sqs.SqsMessageTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class SqsMessageSender implements MessageSender {
	private final SqsMessageTemplate sqsMessageTemplate;

	@Override
	public void sendToDeadLetterQueue(TrainerWorkloadMessage message, String errorMessage) {
		String dlqMessage = String.format("Error: %s, Original message: %s", errorMessage, message);
		sqsMessageTemplate.sendToDeadLetterQueue(dlqMessage);
		log.info("Sent message to SQS Dead Letter Queue");
	}

	@Override
	public void sendResponse(TrainerWorkloadResponseMessage response) {
		sqsMessageTemplate.sendToResponseQueue(response);
		log.info("Sent response message to SQS response queue");
	}
}
