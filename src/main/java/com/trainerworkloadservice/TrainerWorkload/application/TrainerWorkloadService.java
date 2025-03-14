package com.trainerworkloadservice.TrainerWorkload.application;

import com.trainerworkloadservice.TrainerWorkload.application.exception.TrainerWorkloadNotFoundException;
import com.trainerworkloadservice.TrainerWorkload.application.factory.TrainerWorkloadFactory;
import com.trainerworkloadservice.TrainerWorkload.application.port.input.LoadTrainerMonthlyWorkloadUseCase;
import com.trainerworkloadservice.TrainerWorkload.application.port.input.ProcessTrainerWorkloadCommand;
import com.trainerworkloadservice.TrainerWorkload.application.port.input.ProcessTrainerWorkloadUseCase;
import com.trainerworkloadservice.TrainerWorkload.application.port.output.LoadTrainerWorkloadPort;
import com.trainerworkloadservice.TrainerWorkload.application.port.output.UpdateTrainerWorkloadPort;
import com.trainerworkloadservice.TrainerWorkload.domain.MonthWorkload;
import com.trainerworkloadservice.TrainerWorkload.domain.TrainerMonthlyWorkload;
import com.trainerworkloadservice.TrainerWorkload.domain.TrainerWorkload;
import com.trainerworkloadservice.TrainerWorkload.domain.YearWorkload;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerWorkloadService implements ProcessTrainerWorkloadUseCase, LoadTrainerMonthlyWorkloadUseCase {
	private final LoadTrainerWorkloadPort loadTrainerWorkloadPort;
	private final UpdateTrainerWorkloadPort updateTrainerWorkloadPort;
	private final TrainerWorkloadFactory trainerWorkloadFactory;

	@Override
	@CircuitBreaker(name = "trainerWorkloadService",fallbackMethod = "fallbackProcessTrainerWorkload")
	public void processTrainerWorkload(ProcessTrainerWorkloadCommand command) {
		log.debug("Transaction [{}]: Processing trainer workload for username: {}", command.getTransactionId(),
		        command.getUsername());

		LocalDate trainingDate = command.getTrainingDate();
		int year = trainingDate.getYear();
		int month = trainingDate.getMonthValue();
		int duration = command.getTrainingDuration();

		TrainerWorkload trainerWorkload;

		try {
			trainerWorkload = loadTrainerWorkloadPort.findByUsername(command.getUsername());
			log.debug("Transaction [{}]: Existing trainer workload found for username: {}", command.getTransactionId(),
			        command.getUsername());
		} catch (TrainerWorkloadNotFoundException e) {
			trainerWorkload = trainerWorkloadFactory.createFrom(command);
			log.info("Transaction [{}]: Created new trainer workload for username: {}", command.getTransactionId(),
			        command.getUsername());
		}

		// Update trainer info
		trainerWorkload.setFirstName(command.getFirstName());
		trainerWorkload.setLastName(command.getLastName());
		trainerWorkload.setIsActive(command.getIsActive());

		// Find or create year workload
		YearWorkload yearWorkload = findOrCreateYearWorkload(trainerWorkload, year);

		// Find or create month workload
		MonthWorkload monthWorkload = findOrCreateMonthWorkload(yearWorkload, month);

		// Update summary duration based on action type
		int currentDuration = monthWorkload.getSummaryDuration();
		int newDuration;

		switch (command.getActionType()) {
			case ADD :
				newDuration = currentDuration + duration;
				log.debug("Transaction [{}]: Added {} minutes to month {} of year {} for trainer: {}",
				        command.getTransactionId(), duration, month, year, command.getUsername());
				break;
			case DELETE :
				newDuration = Math.max(0, currentDuration - duration);
				log.debug(
				        "Transaction [{}]: Deleted {} minutes from month {} of year {} for trainer: {}. Resulting duration: {}",
				        command.getTransactionId(), duration, month, year, command.getUsername(), newDuration);
				break;
			default :
				throw new IllegalArgumentException("Unsupported action type: " + command.getActionType());
		}

		monthWorkload.setSummaryDuration(newDuration);

		// Save updated workload to MongoDB
		updateTrainerWorkloadPort.save(trainerWorkload);
		log.info(
		        "Transaction [{}]: Trainer workload processed successfully for username: {}, action: {}, new duration: {}",
		        command.getTransactionId(), command.getUsername(), command.getActionType(), newDuration);
	}

	@Override
	@CircuitBreaker(name = "trainerWorkloadService",fallbackMethod = "fallbackGetTrainerMonthlyWorkload")
	public TrainerMonthlyWorkload loadTrainerMonthlyWorkload(String username, int year, int month,
	        String transactionId) {
		log.debug("Transaction [{}]: Getting monthly workload for trainer: {}, year: {}, month: {}", transactionId,
		        username, year, month);

		TrainerWorkload trainerWorkload;
		try {
			trainerWorkload = loadTrainerWorkloadPort.findByUsername(username);
		} catch (TrainerWorkloadNotFoundException e) {
			log.warn("Transaction [{}]: Trainer workload not found for username: {}. Returning empty workload.",
			        transactionId, username);
			return TrainerMonthlyWorkload.builder().username(username).year(year).month(month).summaryDuration(0)
			        .build();
		}

		Optional<YearWorkload> yearWorkloadOpt = trainerWorkload.getYears().stream()
		        .filter(y -> y.getYear().equals(year)).findFirst();

		if (yearWorkloadOpt.isEmpty()) {
			log.debug("Transaction [{}]: No workload data found for year {} for trainer: {}", transactionId, year,
			        username);
			return TrainerMonthlyWorkload.builder().username(username).firstName(trainerWorkload.getFirstName())
			        .lastName(trainerWorkload.getLastName()).isActive(trainerWorkload.getIsActive()).year(year)
			        .month(month).summaryDuration(0).build();
		}

		Optional<MonthWorkload> monthWorkloadOpt = yearWorkloadOpt.get().getMonths().stream()
		        .filter(m -> m.getMonth().equals(month)).findFirst();

		int summaryDuration = monthWorkloadOpt.map(MonthWorkload::getSummaryDuration).orElse(0);

		TrainerMonthlyWorkload response = TrainerMonthlyWorkload.builder().username(username)
		        .firstName(trainerWorkload.getFirstName()).lastName(trainerWorkload.getLastName())
		        .isActive(trainerWorkload.getIsActive()).year(year).month(month).summaryDuration(summaryDuration)
		        .build();

		log.info(
		        "Transaction [{}]: Monthly workload retrieved successfully for trainer: {}, year: {}, month: {}, duration: {}",
		        transactionId, username, year, month, summaryDuration);
		return response;
	}

	// Fallback method for processTrainerWorkload
	public void fallbackProcessTrainerWorkload(ProcessTrainerWorkloadCommand command, Throwable t) {
		log.error(
		        "Transaction [{}]: Circuit breaker triggered for processTrainerWorkload. Failed to process workload for username: {}. Error: {}",
		        command.getTransactionId(), command.getUsername(), t.getMessage());
	}

	// Fallback method for loadTrainerMonthlyWorkload
	public TrainerMonthlyWorkload fallbackGetTrainerMonthlyWorkload(String username, int year, int month,
	        String transactionId, Throwable t) {
		log.error(
		        "Transaction [{}]: Circuit breaker triggered for loadTrainerMonthlyWorkload. Failed to retrieve workload for username: {}. Error: {}",
		        transactionId, username, t.getMessage());
		return TrainerMonthlyWorkload.builder().username(username).year(year).month(month).summaryDuration(0).build();
	}

	private YearWorkload findOrCreateYearWorkload(TrainerWorkload trainerWorkload, int year) {
		return trainerWorkload.getYears().stream().filter(y -> y.getYear().equals(year)).findFirst().orElseGet(() -> {
			YearWorkload newYearWorkload = YearWorkload.builder().year(year).build();
			trainerWorkload.getYears().add(newYearWorkload);
			log.debug("Created new YearWorkload for year: {}", year);
			return newYearWorkload;
		});
	}

	private MonthWorkload findOrCreateMonthWorkload(YearWorkload yearWorkload, int month) {
		return yearWorkload.getMonths().stream().filter(m -> m.getMonth().equals(month)).findFirst().orElseGet(() -> {
			MonthWorkload newMonthWorkload = MonthWorkload.builder().month(month).summaryDuration(0).build();
			yearWorkload.getMonths().add(newMonthWorkload);
			log.debug("Created new MonthWorkload for month: {}", month);
			return newMonthWorkload;
		});
	}
}
