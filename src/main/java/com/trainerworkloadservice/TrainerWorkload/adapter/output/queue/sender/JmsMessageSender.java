package com.trainerworkloadservice.TrainerWorkload.adapter.output.queue.sender;

import com.trainerworkloadservice.TrainerWorkload.adapter.output.queue.message.TrainerWorkloadMessage;
import com.trainerworkloadservice.TrainerWorkload.adapter.output.queue.message.TrainerWorkloadResponseMessage;
import com.trainerworkloadservice.configuration.messaging.JmsConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JmsMessageSender implements MessageSender {
	private final JmsTemplate jmsTemplate;

	@Override
	public void sendToDeadLetterQueue(TrainerWorkloadMessage message, String errorMessage) {
		String dlqMessage = String.format("Error: %s, Original message: %s", errorMessage, message);
		jmsTemplate.convertAndSend(JmsConfig.DEAD_LETTER_QUEUE, dlqMessage);
	}

	@Override
	public void sendResponse(TrainerWorkloadResponseMessage response) {
		jmsTemplate.convertAndSend(JmsConfig.TRAINER_WORKLOAD_RESPONSE_QUEUE, response);
	}
}
