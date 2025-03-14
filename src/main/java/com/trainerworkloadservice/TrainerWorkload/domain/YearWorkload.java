package com.trainerworkloadservice.TrainerWorkload.domain;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YearWorkload {
	private Integer year;
	@Builder.Default
	private List<MonthWorkload> months = new ArrayList<>();
}
