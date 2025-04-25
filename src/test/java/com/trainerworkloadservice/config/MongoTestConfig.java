package com.trainerworkloadservice.config;

import static org.mockito.Mockito.mock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@TestConfiguration
public class MongoTestConfig {
	@Bean
	@Primary
	public MongoDatabaseFactory mongoDatabaseFactory() {
		return mock(MongoDatabaseFactory.class);
	}

	@Bean
	@Primary
	public MongoTemplate mongoTemplate() {
		return mock(MongoTemplate.class);
	}

	@Bean
	@Primary
	public MongoConverter mongoConverter() {
		return mock(MappingMongoConverter.class);
	}

	@Bean
	@Primary
	public MongoMappingContext mongoMappingContext() {
		return mock(MongoMappingContext.class);
	}

	@Bean
	@Primary
	public MongoCustomConversions mongoCustomConversions() {
		return mock(MongoCustomConversions.class);
	}
}
