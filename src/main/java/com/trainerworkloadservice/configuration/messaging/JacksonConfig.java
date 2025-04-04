package com.trainerworkloadservice.configuration.messaging;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.trainerworkloadservice.TrainerWorkload.domain.ActionType;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
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
			if (value == null || value.isEmpty()) {
				return null;
			}

			try {
				return ActionType.valueOf(value.toUpperCase());
			} catch (IllegalArgumentException e) {
				for (ActionType type : ActionType.values()) {
					if (type.name().equalsIgnoreCase(value)) {
						return type;
					}
				}
				return null;
			}
		}
	}
}
