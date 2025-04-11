package com.trainerworkloadservice.TrainerWorkload.domain;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "trainer_workloads")
@CompoundIndex(name = "name_idx",def = "{'firstName': 1, 'lastName': 1}")
public class TrainerWorkload {
	@Id
	private String id;
	private String username;
	private String firstName;
	private String lastName;
	private Boolean isActive;
	@Builder.Default
	private List<YearWorkload> years = new ArrayList<>();
}
