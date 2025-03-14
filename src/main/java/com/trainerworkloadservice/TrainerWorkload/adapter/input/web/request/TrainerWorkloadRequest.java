package com.trainerworkloadservice.TrainerWorkload.adapter.input.web.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.trainerworkloadservice.TrainerWorkload.application.port.input.ProcessTrainerWorkloadCommand;
import com.trainerworkloadservice.TrainerWorkload.domain.ActionType;
import java.time.LocalDate;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkloadRequest {
	@NotBlank(message = "Username is required")
	private String username;

	@NotBlank(message = "First name is required")
	@JsonProperty("first_name")
	private String firstName;

	@NotBlank(message = "Last name is required")
	@JsonProperty("last_name")
	private String lastName;

	@NotNull(message = "Active status is required")
	@JsonProperty("is_active")
	private Boolean isActive;

	@NotNull(message = "Training date is required")
	@JsonProperty("training_date")
	private LocalDate trainingDate;

	@NotNull(message = "Training duration is required")
	@Positive(message = "Training duration must be positive")
	@JsonProperty("training_duration")
	private Integer trainingDuration;

	@NotNull(message = "Action type is required")
	@JsonProperty("action_type")
	private ActionType actionType;

	public ProcessTrainerWorkloadCommand toCommand(String transactionId) {
		return ProcessTrainerWorkloadCommand.builder().username(username).firstName(firstName).lastName(lastName)
		        .isActive(isActive).trainingDate(trainingDate).trainingDuration(trainingDuration).actionType(actionType)
		        .transactionId(transactionId).build();
	}
}
