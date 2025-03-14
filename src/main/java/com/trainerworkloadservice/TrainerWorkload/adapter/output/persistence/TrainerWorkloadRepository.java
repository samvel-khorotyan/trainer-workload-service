package com.trainerworkloadservice.TrainerWorkload.adapter.output.persistence;

import com.trainerworkloadservice.TrainerWorkload.application.exception.TrainerWorkloadNotFoundException;
import com.trainerworkloadservice.TrainerWorkload.application.port.output.LoadTrainerWorkloadPort;
import com.trainerworkloadservice.TrainerWorkload.application.port.output.UpdateTrainerWorkloadPort;
import com.trainerworkloadservice.TrainerWorkload.domain.TrainerWorkload;
import org.springframework.stereotype.Repository;

@Repository
public class TrainerWorkloadRepository implements LoadTrainerWorkloadPort, UpdateTrainerWorkloadPort {
	private final TrainerWorkloadPersistenceRepository repository;

	public TrainerWorkloadRepository(TrainerWorkloadPersistenceRepository repository) {
		this.repository = repository;
	}

	@Override
	public TrainerWorkload findByUsername(String username) {
		return repository.findByUsername(username).orElseThrow(() -> TrainerWorkloadNotFoundException.by(username));
	}

	@Override
	public void save(TrainerWorkload trainerWorkload) {
		repository.save(trainerWorkload);
	}
}
