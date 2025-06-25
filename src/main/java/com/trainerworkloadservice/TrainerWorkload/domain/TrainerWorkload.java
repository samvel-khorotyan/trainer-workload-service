package com.trainerworkloadservice.TrainerWorkload.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
@DynamoDBTable(tableName = "TrainerWorkload")
public class TrainerWorkload {
	@DynamoDBHashKey(attributeName = "username")
	private String username;

	@DynamoDBAttribute(attributeName = "firstName")
	@DynamoDBIndexHashKey(globalSecondaryIndexName = "TrainerName-Index")
	private String firstName;

	@DynamoDBAttribute(attributeName = "lastName")
	@DynamoDBIndexRangeKey(globalSecondaryIndexName = "TrainerName-Index")
	private String lastName;

	@DynamoDBAttribute(attributeName = "isActive")
	private Boolean isActive;

	@DynamoDBAttribute(attributeName = "years")
	@DynamoDBTypeConverted(converter = YearWorkloadListConverter.class)
	@Builder.Default
	private List<YearWorkload> years = new ArrayList<>();

	// Converter for YearWorkload List
	public static class YearWorkloadListConverter implements DynamoDBTypeConverter<String, List<YearWorkload>> {
		private static final ObjectMapper objectMapper = new ObjectMapper();

		@Override
		public String convert(List<YearWorkload> yearWorkloads) {
			try {
				return objectMapper.writeValueAsString(yearWorkloads);
			} catch (JsonProcessingException e) {
				throw new RuntimeException("Error converting YearWorkload list to JSON", e);
			}
		}

		@Override
		public List<YearWorkload> unconvert(String jsonString) {
			try {
				if (jsonString == null || jsonString.isEmpty()) {
					return new ArrayList<>();
				}
				return objectMapper.readValue(jsonString, new TypeReference<List<YearWorkload>>() {
				});
			} catch (JsonProcessingException e) {
				throw new RuntimeException("Error converting JSON to YearWorkload list", e);
			}
		}
	}
}
