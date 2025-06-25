package com.trainerworkloadservice.TrainerWorkload.application.factory;

import com.trainerworkloadservice.TrainerWorkload.application.port.input.ProcessTrainerWorkloadCommand;
import com.trainerworkloadservice.TrainerWorkload.domain.TrainerWorkload;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TrainerWorkloadFactory {

	public TrainerWorkload createFrom(ProcessTrainerWorkloadCommand command) {
		log.debug("Transaction [{}]: Creating new TrainerWorkload for username: {}", command.getTransactionId(),
		        command.getUsername());

		return TrainerWorkload.builder().username(command.getUsername()).firstName(command.getFirstName())
		        .lastName(command.getLastName()).isActive(command.getIsActive()).years(new ArrayList<>()).build();
	}
}
