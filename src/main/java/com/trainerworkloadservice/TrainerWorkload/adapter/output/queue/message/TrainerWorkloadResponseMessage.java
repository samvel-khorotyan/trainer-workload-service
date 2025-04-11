package com.trainerworkloadservice.TrainerWorkload.adapter.output.queue.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkloadResponseMessage {
	private String username;
	private String firstName;
	private String lastName;
	private Boolean isActive;
	private Integer year;
	private Integer month;
	private Integer summaryDuration;
	private String transactionId;

	@Builder.Default
	private boolean error = false;
	private String errorMessage;
}
