package com.trainerworkloadservice.TrainerWorkload.adapter.input.web.response;

import com.trainerworkloadservice.TrainerWorkload.domain.TrainerMonthlyWorkload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerMonthlyWorkloadResponse {
	private String username;
	private String firstName;
	private String lastName;
	private Boolean isActive;
	private Integer year;
	private Integer month;
	private Integer summaryDuration;

	public static TrainerMonthlyWorkloadResponse form(TrainerMonthlyWorkload trainerMonthlyWorkload) {
		return TrainerMonthlyWorkloadResponse.builder().username(trainerMonthlyWorkload.getUsername())
		        .firstName(trainerMonthlyWorkload.getFirstName()).lastName(trainerMonthlyWorkload.getLastName())
		        .isActive(trainerMonthlyWorkload.getIsActive()).year(trainerMonthlyWorkload.getYear())
		        .month(trainerMonthlyWorkload.getMonth()).summaryDuration(trainerMonthlyWorkload.getSummaryDuration())
		        .build();
	}
}
