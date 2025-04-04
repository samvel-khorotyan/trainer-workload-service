package com.trainerworkloadservice.TrainerWorkload.adapter.output.queue.message;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkloadResponseMessage implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	private String username;
	private String firstName;
	private String lastName;
	private Boolean isActive;
	private Integer year;
	private Integer month;
	private Integer summaryDuration;
	private String transactionId;
}
