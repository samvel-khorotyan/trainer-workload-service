package com.trainerworkloadservice.TrainerWorkload.application.port.input;

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
public class ProcessTrainerWorkloadCommand {
	private String username;
	private String firstName;
	private String lastName;
	private Boolean isActive;
	private LocalDate trainingDate;
	private Integer trainingDuration;
	private ActionType actionType;
	private String transactionId;
}
