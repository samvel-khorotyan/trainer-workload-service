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
	public void processTrainerWorkload(ProcessTrainerWorkloadCommand command) {
		log.debug("Transaction [{}]: Processing trainer workload for username: {}", command.getTransactionId(),
		        command.getUsername());

		TrainerWorkload trainerWorkload = getOrCreateTrainerWorkload(command);
		updateTrainerPersonalInfo(trainerWorkload, command);
		updateTrainerWorkloadDuration(trainerWorkload, command);

		updateTrainerWorkloadPort.save(trainerWorkload);
		log.info("Transaction [{}]: Trainer workload processed successfully for username: {}, action: {}",
		        command.getTransactionId(), command.getUsername(), command.getActionType());
	}

	@Override
	public TrainerMonthlyWorkload loadTrainerMonthlyWorkload(String username, int year, int month,
	        String transactionId) {
		log.debug("Transaction [{}]: Getting monthly workload for trainer: {}, year: {}, month: {}", transactionId,
		        username, year, month);

		TrainerWorkload trainerWorkload = getTrainerWorkloadOrNull(username, transactionId);
		if (trainerWorkload == null) {
			return createEmptyMonthlyWorkload(username, year, month);
		}

		return buildTrainerMonthlyWorkload(trainerWorkload, username, year, month, transactionId);
	}

	private TrainerWorkload getOrCreateTrainerWorkload(ProcessTrainerWorkloadCommand command) {
		try {
			TrainerWorkload existingWorkload = loadTrainerWorkloadPort.findByUsername(command.getUsername());
			log.debug("Transaction [{}]: Existing trainer workload found for username: {}", command.getTransactionId(),
			        command.getUsername());
			return existingWorkload;
		} catch (TrainerWorkloadNotFoundException e) {
			TrainerWorkload newWorkload = trainerWorkloadFactory.createFrom(command);
			log.info("Transaction [{}]: Created new trainer workload for username: {}", command.getTransactionId(),
			        command.getUsername());
			return newWorkload;
		}
	}

	private void updateTrainerPersonalInfo(TrainerWorkload trainerWorkload, ProcessTrainerWorkloadCommand command) {
		trainerWorkload.setFirstName(command.getFirstName());
		trainerWorkload.setLastName(command.getLastName());
		trainerWorkload.setIsActive(command.getIsActive());
	}

	private void updateTrainerWorkloadDuration(TrainerWorkload trainerWorkload, ProcessTrainerWorkloadCommand command) {
		LocalDate trainingDate = command.getTrainingDate();
		int year = trainingDate.getYear();
		int month = trainingDate.getMonthValue();
		int duration = command.getTrainingDuration();

		YearWorkload yearWorkload = findOrCreateYearWorkload(trainerWorkload, year);

		MonthWorkload monthWorkload = findOrCreateMonthWorkload(yearWorkload, month);

		// Calculate new duration based on action type
		int newDuration = calculateNewDuration(monthWorkload.getSummaryDuration(), duration, command);
		monthWorkload.setSummaryDuration(newDuration);

		log.debug("Transaction [{}]: Updated duration for month {} of year {} for trainer: {} to {}",
		        command.getTransactionId(), month, year, command.getUsername(), newDuration);
	}

	private int calculateNewDuration(int currentDuration, int duration, ProcessTrainerWorkloadCommand command) {
		switch (command.getActionType()) {
			case ADD :
				int newDuration = currentDuration + duration;
				log.debug("Transaction [{}]: Added {} minutes to current duration {}", command.getTransactionId(),
				        duration, currentDuration);
				return newDuration;
			case DELETE :
				int reducedDuration = Math.max(0, currentDuration - duration);
				log.debug("Transaction [{}]: Deleted {} minutes from current duration {}. Resulting duration: {}",
				        command.getTransactionId(), duration, currentDuration, reducedDuration);
				return reducedDuration;
			default :
				throw new IllegalArgumentException("Unsupported action type: " + command.getActionType());
		}
	}

	private TrainerWorkload getTrainerWorkloadOrNull(String username, String transactionId) {
		try {
			return loadTrainerWorkloadPort.findByUsername(username);
		} catch (TrainerWorkloadNotFoundException e) {
			log.warn("Transaction [{}]: Trainer workload not found for username: {}. Returning empty workload.",
			        transactionId, username);
			return null;
		}
	}

	private TrainerMonthlyWorkload createEmptyMonthlyWorkload(String username, int year, int month) {
		return TrainerMonthlyWorkload.builder().username(username).year(year).month(month).summaryDuration(0).build();
	}

	private TrainerMonthlyWorkload buildTrainerMonthlyWorkload(TrainerWorkload trainerWorkload, String username,
	        int year, int month, String transactionId) {

		int summaryDuration = findMonthlyWorkloadDuration(trainerWorkload, year, month, transactionId);

		TrainerMonthlyWorkload response = TrainerMonthlyWorkload.builder().username(username)
		        .firstName(trainerWorkload.getFirstName()).lastName(trainerWorkload.getLastName())
		        .isActive(trainerWorkload.getIsActive()).year(year).month(month).summaryDuration(summaryDuration)
		        .build();

		log.info(
		        "Transaction [{}]: Monthly workload retrieved successfully for trainer: {}, year: {}, month: {}, duration: {}",
		        transactionId, username, year, month, summaryDuration);
		return response;
	}

	private int findMonthlyWorkloadDuration(TrainerWorkload trainerWorkload, int year, int month,
	        String transactionId) {
		Optional<YearWorkload> yearWorkloadOpt = trainerWorkload.getYears().stream()
		        .filter(y -> y.getYear().equals(year)).findFirst();

		if (yearWorkloadOpt.isEmpty()) {
			log.debug("Transaction [{}]: No workload data found for year {} for trainer: {}", transactionId, year,
			        trainerWorkload.getUsername());
			return 0;
		}

		Optional<MonthWorkload> monthWorkloadOpt = yearWorkloadOpt.get().getMonths().stream()
		        .filter(m -> m.getMonth().equals(month)).findFirst();

		return monthWorkloadOpt.map(MonthWorkload::getSummaryDuration).orElse(0);
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
