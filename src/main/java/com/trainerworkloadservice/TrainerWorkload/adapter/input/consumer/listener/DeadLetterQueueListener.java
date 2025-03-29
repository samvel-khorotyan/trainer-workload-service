package com.trainerworkloadservice.TrainerWorkload.adapter.input.consumer.listener;

import com.trainerworkloadservice.configuration.messaging.JmsConfig;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeadLetterQueueListener {
	@JmsListener(destination = JmsConfig.DEAD_LETTER_QUEUE)
	public void handleDeadLetterMessage(Message message) {
		try {
			if (message instanceof TextMessage textMessage) {
				log.error("Received message in Dead Letter Queue: {}", textMessage.getText());
			} else {
				log.error("Received non-text message in Dead Letter Queue of type: {}", message.getClass().getName());
			}
		} catch (JMSException e) {
			log.error("Error processing message from Dead Letter Queue", e);
		}
	}
}
