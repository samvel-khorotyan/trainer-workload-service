package com.trainerworkloadservice.TrainerWorkload.application.port.output;

import com.trainerworkloadservice.TrainerWorkload.domain.TrainerWorkload;

public interface UpdateTrainerWorkloadPort {
	void save(TrainerWorkload trainerWorkload);
}
