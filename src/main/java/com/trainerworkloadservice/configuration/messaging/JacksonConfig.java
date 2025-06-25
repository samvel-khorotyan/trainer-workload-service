package com.trainerworkloadservice.configuration.messaging;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.trainerworkloadservice.TrainerWorkload.domain.ActionType;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
public class JacksonConfig {
	@Bean
	@Primary
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();

		// Register modules
		mapper.registerModule(new JavaTimeModule());
		mapper.registerModule(actionTypeModule());

		// Configure features
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

		return mapper;
	}

	@Bean
	public Module actionTypeModule() {
		SimpleModule module = new SimpleModule();
		module.addDeserializer(ActionType.class, new ActionTypeDeserializer());
		return module;
	}

	public static class ActionTypeDeserializer extends JsonDeserializer<ActionType> {
		@Override
		public ActionType deserialize(JsonParser p, DeserializationContext context) throws IOException {
			String value = p.getValueAsString();
			if (value == null || value.trim().isEmpty()) {
				return null;
			}

			try {
				return ActionType.valueOf(value.trim().toUpperCase());
			} catch (IllegalArgumentException e) {
				log.warn("Unknown ActionType: {}", value);
				for (ActionType type : ActionType.values()) {
					if (type.name().equalsIgnoreCase(value.trim())) {
						return type;
					}
				}
				throw new IllegalArgumentException("Unknown ActionType: " + value);
			}
		}
	}
}
