package com.trainerworkloadservice.TrainerWorkload.adapter.input.consumer.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.messaging.listener.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadLetterQueueSqsListener {
	private final ObjectMapper objectMapper;

	@SqsListener("${aws.sqs.dead-letter-queue}")
	public void handleDeadLetterMessage(String message) {
		log.error("Received message in Dead Letter Queue: {}", message);

		try {
			// Attempt to log more details about the message
			Object jsonMessage = objectMapper.readValue(message, Object.class);
			log.error("Dead Letter Message details: {}", jsonMessage);
		} catch (Exception e) {
			log.error("Could not parse Dead Letter message: {}", e.getMessage());
		}
	}
}
