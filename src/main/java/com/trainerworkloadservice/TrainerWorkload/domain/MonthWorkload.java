package com.trainerworkloadservice.TrainerWorkload.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthWorkload {
	private Integer month;

	@Builder.Default
	private Integer summaryDuration = 0;
}
