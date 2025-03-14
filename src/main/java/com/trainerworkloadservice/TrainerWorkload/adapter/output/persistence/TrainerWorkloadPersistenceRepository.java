package com.trainerworkloadservice.TrainerWorkload.adapter.output.persistence;

import com.trainerworkloadservice.TrainerWorkload.domain.TrainerWorkload;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TrainerWorkloadPersistenceRepository extends MongoRepository<TrainerWorkload, String> {
	Optional<TrainerWorkload> findByUsername(String username);
}
