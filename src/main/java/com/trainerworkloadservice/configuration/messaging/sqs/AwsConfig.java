package com.trainerworkloadservice.configuration.messaging.sqs;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsConfig {
	@Value("${aws.region:eu-north-1}")
	private String region;

	@Bean
	public AmazonSQSAsync amazonSQSAsync() {
		return AmazonSQSAsyncClientBuilder.standard().withRegion(Regions.fromName(region))
		        .withCredentials(DefaultAWSCredentialsProviderChain.getInstance()).build();
	}
}
