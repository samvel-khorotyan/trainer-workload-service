package com.trainerworkloadservice.TrainerWorkload.adapter.output.queue.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.trainerworkloadservice.TrainerWorkload.application.port.input.ProcessTrainerWorkloadCommand;
import com.trainerworkloadservice.TrainerWorkload.domain.ActionType;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkloadMessage {
	private String username;
	private String firstName;
	private String lastName;
	private Boolean isActive;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate trainingDate;

	private Integer trainingDuration;
	private ActionType actionType;
	private Integer year;
	private Integer month;
	private String transactionId;

	public ProcessTrainerWorkloadCommand toCommand(String transactionId) {
		return ProcessTrainerWorkloadCommand.builder().username(username).firstName(firstName).lastName(lastName)
		        .isActive(isActive).trainingDate(trainingDate).trainingDuration(trainingDuration).actionType(actionType)
		        .transactionId(transactionId).build();
	}
}
