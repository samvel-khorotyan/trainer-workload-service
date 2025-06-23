// package com.trainerworkloadservice.TrainerWorkload.adapter.output.persistence;
//
// import
// com.trainerworkloadservice.TrainerWorkload.application.exception.TrainerWorkloadNotFoundException;
// import
// com.trainerworkloadservice.TrainerWorkload.application.port.output.LoadTrainerWorkloadPort;
// import
// com.trainerworkloadservice.TrainerWorkload.application.port.output.UpdateTrainerWorkloadPort;
// import com.trainerworkloadservice.TrainerWorkload.domain.TrainerWorkload;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// import org.springframework.stereotype.Repository;
//
// @Slf4j
// @Repository
// @ConditionalOnProperty(name = "app.persistence.mode", havingValue = "mongodb")
// public class TrainerWorkloadMongoRepository implements LoadTrainerWorkloadPort,
// UpdateTrainerWorkloadPort {
//
//	private final TrainerWorkloadPersistenceRepository repository;
//
//	public TrainerWorkloadMongoRepository(TrainerWorkloadPersistenceRepository repository) {
//		this.repository = repository;
//	}
//
//	@Override
//	public TrainerWorkload findByUsername(String username) {
//		log.info("MongoDB Repository: Finding trainer workload for username: {}", username);
//		return repository.findByUsername(username)
//				.orElseThrow(() -> TrainerWorkloadNotFoundException.by(username));
//	}
//
//	@Override
//	public void save(TrainerWorkload trainerWorkload) {
//		log.info("MongoDB Repository: Saving trainer workload for username: {}",
// trainerWorkload.getUsername());
//		repository.save(trainerWorkload);
//		log.info("MongoDB Repository: Successfully saved workload for username: {}",
// trainerWorkload.getUsername());
//	}
// }
package com.trainerworkloadservice.TrainerWorkload.adapter.output.persistence;

import com.trainerworkloadservice.TrainerWorkload.application.port.output.LoadTrainerWorkloadPort;
import com.trainerworkloadservice.TrainerWorkload.application.port.output.UpdateTrainerWorkloadPort;
import com.trainerworkloadservice.TrainerWorkload.domain.MonthWorkload;
import com.trainerworkloadservice.TrainerWorkload.domain.TrainerWorkload;
import com.trainerworkloadservice.TrainerWorkload.domain.YearWorkload;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@ConditionalOnProperty(name = "app.persistence.mode",havingValue = "mock",matchIfMissing = true)
public class TrainerWorkloadRepository implements LoadTrainerWorkloadPort, UpdateTrainerWorkloadPort {

	// In-memory storage for mock data
	private final Map<String, TrainerWorkload> mockStorage = new ConcurrentHashMap<>();

	@Override
	public TrainerWorkload findByUsername(String username) {
		log.info("Mock Repository: Finding trainer workload for username: {}", username);

		TrainerWorkload workload = mockStorage.get(username);
		if (workload != null) {
			log.info("Mock Repository: Found existing workload for username: {}", username);
			return workload;
		}

		// Create mock data if not exists
		TrainerWorkload mockWorkload = createMockTrainerWorkload(username);
		mockStorage.put(username, mockWorkload);

		log.info("Mock Repository: Created mock workload for username: {}", username);
		return mockWorkload;
	}

	@Override
	public void save(TrainerWorkload trainerWorkload) {
		log.info("Mock Repository: Saving trainer workload for username: {}", trainerWorkload.getUsername());

		// Simply store in memory
		mockStorage.put(trainerWorkload.getUsername(), trainerWorkload);

		log.info("Mock Repository: Successfully saved workload for username: {} with {} years",
		        trainerWorkload.getUsername(),
		        trainerWorkload.getYears() != null ? trainerWorkload.getYears().size() : 0);
	}

	private TrainerWorkload createMockTrainerWorkload(String username) {
		// Create mock trainer workload with some default data
		TrainerWorkload mockWorkload = TrainerWorkload.builder().username(username).firstName("Mock")
		        .lastName("Trainer").isActive(true).years(new ArrayList<>()).build();

		// Add some mock year/month data for current year
		int currentYear = java.time.LocalDate.now().getYear();
		YearWorkload currentYearWorkload = YearWorkload.builder().year(currentYear).months(new ArrayList<>()).build();

		// Add current month with 0 duration
		int currentMonth = java.time.LocalDate.now().getMonthValue();
		MonthWorkload currentMonthWorkload = MonthWorkload.builder().month(currentMonth).summaryDuration(0).build();

		currentYearWorkload.getMonths().add(currentMonthWorkload);
		mockWorkload.getYears().add(currentYearWorkload);

		return mockWorkload;
	}

	// Helper method to get all stored workloads (for debugging)
	public Map<String, TrainerWorkload> getAllWorkloads() {
		return new ConcurrentHashMap<>(mockStorage);
	}

	// Helper method to clear all data (for testing)
	public void clearAll() {
		mockStorage.clear();
		log.info("Mock Repository: Cleared all stored workloads");
	}
}
