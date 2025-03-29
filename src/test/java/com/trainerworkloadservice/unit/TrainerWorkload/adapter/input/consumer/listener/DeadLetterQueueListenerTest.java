package com.trainerworkloadservice.unit.TrainerWorkload.adapter.input.consumer.listener;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.trainerworkloadservice.TrainerWorkload.adapter.input.consumer.listener.DeadLetterQueueListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeadLetterQueueListenerTest {
	@Mock
	private TextMessage textMessage;

	@Mock
	private Message nonTextMessage;

	@InjectMocks
	private DeadLetterQueueListener listener;

	@Test
	void handleDeadLetterMessage_WhenMessageIsTextMessage_ShouldLogMessageText() throws JMSException {
		String messageText = "Test message";
		when(textMessage.getText()).thenReturn(messageText);

		listener.handleDeadLetterMessage(textMessage);
	}

	@Test
	void handleDeadLetterMessage_WhenMessageIsNotTextMessage_ShouldLogMessageType() {
		listener.handleDeadLetterMessage(nonTextMessage);
	}

	@Test
	void handleDeadLetterMessage_WhenJMSExceptionThrown_ShouldLogError() throws JMSException {
		doThrow(new JMSException("JMS error")).when(textMessage).getText();

		listener.handleDeadLetterMessage(textMessage);
	}
}
