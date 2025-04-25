package com.trainerworkloadservice.config;

import com.trainerworkloadservice.TrainerWorkload.adapter.output.queue.message.TrainerWorkloadResponseMessage;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class MockMessageSender {
	private Map<String, Object> sentMessages = new HashMap<>();
	private TrainerWorkloadResponseMessage mockResponse;

	public void sendMessage(String destination, Object message) {
		sentMessages.put(destination, message);
	}

	public boolean wasMessageSent(String destination) {
		return sentMessages.containsKey(destination);
	}

	public TrainerWorkloadResponseMessage receiveResponse() {
		return mockResponse;
	}

	public void reset() {
		sentMessages.clear();
		mockResponse = null;
	}
}
