package com.trainerworkloadservice.configuration.messaging.sqs;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.messaging.config.QueueMessageHandlerFactory;
import io.awspring.cloud.messaging.config.SimpleMessageListenerContainerFactory;
import io.awspring.cloud.messaging.core.QueueMessagingTemplate;
import io.awspring.cloud.messaging.listener.QueueMessageHandler;
import io.awspring.cloud.messaging.listener.SimpleMessageListenerContainer;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.PayloadMethodArgumentResolver;

@Configuration
public class SqsConfig {
	private final Module actionTypeModule;

	public SqsConfig(@Qualifier("actionTypeModule") Module actionTypeModule) {
		this.actionTypeModule = actionTypeModule;
	}

	@Bean
	@Primary
	public QueueMessagingTemplate queueMessagingTemplate(AmazonSQSAsync amazonSQSAsync) {
		return new QueueMessagingTemplate(amazonSQSAsync);
	}

	@Bean
	public SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory(AmazonSQSAsync amazonSQSAsync) {
		SimpleMessageListenerContainerFactory factory = new SimpleMessageListenerContainerFactory();
		factory.setAmazonSqs(amazonSQSAsync);
		factory.setMaxNumberOfMessages(10);
		factory.setWaitTimeOut(20);
		factory.setAutoStartup(true);
		return factory;
	}

	@Bean
	public QueueMessageHandler queueMessageHandler(AmazonSQSAsync amazonSQSAsync, ObjectMapper objectMapper) {
		QueueMessageHandlerFactory factory = new QueueMessageHandlerFactory();
		factory.setAmazonSqs(amazonSQSAsync);

		MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
		messageConverter.setStrictContentTypeMatch(false);

		// Use your existing ObjectMapper with ActionType module
		objectMapper.registerModule(actionTypeModule);
		messageConverter.setObjectMapper(objectMapper);

		factory.setArgumentResolvers(Collections.singletonList(new PayloadMethodArgumentResolver(messageConverter)));

		return factory.createQueueMessageHandler();
	}

	@Bean
	public SimpleMessageListenerContainer simpleMessageListenerContainer(AmazonSQSAsync amazonSQSAsync,
	        QueueMessageHandler queueMessageHandler,
	        SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory) {
		SimpleMessageListenerContainer container = simpleMessageListenerContainerFactory
		        .createSimpleMessageListenerContainer();
		container.setMessageHandler(queueMessageHandler);
		return container;
	}
}
