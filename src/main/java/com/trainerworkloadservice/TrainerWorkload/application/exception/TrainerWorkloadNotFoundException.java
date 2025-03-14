package com.trainerworkloadservice.TrainerWorkload.application.exception;

import com.trainerworkloadservice.common.exception.NotFoundException;

public class TrainerWorkloadNotFoundException extends NotFoundException {
	public TrainerWorkloadNotFoundException(String message) {
		super(message);
	}

	public static TrainerWorkloadNotFoundException by(String username) {
		return new TrainerWorkloadNotFoundException("Trainer not found with username: " + username);
	}
}
