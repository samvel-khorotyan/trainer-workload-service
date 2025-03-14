package com.trainerworkloadservice.TrainerWorkload.application.port.output;

import com.trainerworkloadservice.TrainerWorkload.domain.TrainerWorkload;

public interface LoadTrainerWorkloadPort {
	TrainerWorkload findByUsername(String username);
}
