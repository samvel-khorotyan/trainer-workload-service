package com.trainerworkloadservice.configuration;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AWSConfig {
	@Value("${cloud.aws.region.static}")
	private String region;

	@Value("${aws.credentials.access-key:}")
	private String accessKey;

	@Value("${aws.credentials.secret-key:}")
	private String secretKey;

	@Bean
	public AWSCredentialsProvider awsCredentialsProvider() {
		// Try different credential sources in order
		if (accessKey != null && !accessKey.isEmpty() && secretKey != null && !secretKey.isEmpty()) {
			return new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
		}

		return DefaultAWSCredentialsProviderChain.getInstance();
	}
}
