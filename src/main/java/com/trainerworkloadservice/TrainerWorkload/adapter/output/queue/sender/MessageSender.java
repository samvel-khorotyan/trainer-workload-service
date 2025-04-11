package com.trainerworkloadservice.TrainerWorkload.adapter.output.queue.sender;

import com.trainerworkloadservice.TrainerWorkload.adapter.output.queue.message.TrainerWorkloadMessage;
import com.trainerworkloadservice.TrainerWorkload.adapter.output.queue.message.TrainerWorkloadResponseMessage;

public interface MessageSender {
	void sendToDeadLetterQueue(TrainerWorkloadMessage message, String errorMessage);

	void sendResponse(TrainerWorkloadResponseMessage response);
}
