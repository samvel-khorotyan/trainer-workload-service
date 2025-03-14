package com.trainerworkloadservice.TrainerWorkload.application.port.input;

import com.trainerworkloadservice.TrainerWorkload.domain.TrainerMonthlyWorkload;

public interface LoadTrainerMonthlyWorkloadUseCase {
	TrainerMonthlyWorkload loadTrainerMonthlyWorkload(String username, int year, int month, String transactionId);
}
