package com.trainerworkloadservice.TrainerWorkload.adapter.output.queue.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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

	@JsonProperty("first_name")
	private String firstName;

	@JsonProperty("last_name")
	private String lastName;

	@JsonProperty("is_active")
	private Boolean isActive;

	@JsonProperty("training_date")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate trainingDate;

	@JsonProperty("training_duration")
	private Integer trainingDuration;

	@JsonProperty("action_type")
	private ActionType actionType;

	private Integer year;
	private Integer month;

	@JsonProperty("transaction_id")
	private String transactionId;

	public ProcessTrainerWorkloadCommand toCommand(String transactionId) {
		return ProcessTrainerWorkloadCommand.builder().username(username).firstName(firstName).lastName(lastName)
		        .isActive(isActive).trainingDate(trainingDate).trainingDuration(trainingDuration).actionType(actionType)
		        .transactionId(transactionId).build();
	}
}
