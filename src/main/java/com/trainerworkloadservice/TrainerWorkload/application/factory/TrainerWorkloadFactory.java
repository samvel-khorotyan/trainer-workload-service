package com.trainerworkloadservice.TrainerWorkload.application.factory;

import com.trainerworkloadservice.TrainerWorkload.application.port.input.ProcessTrainerWorkloadCommand;
import com.trainerworkloadservice.TrainerWorkload.domain.TrainerWorkload;
import org.springframework.stereotype.Component;

@Component
public class TrainerWorkloadFactory {
	public TrainerWorkload createFrom(ProcessTrainerWorkloadCommand command) {
		return TrainerWorkload.builder().username(command.getUsername()).firstName(command.getFirstName())
		        .lastName(command.getLastName()).isActive(command.getIsActive()).build();
	}
}
