package com.trainerworkloadservice.TrainerWorkload.adapter.output.persistence;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.trainerworkloadservice.TrainerWorkload.application.exception.TrainerWorkloadNotFoundException;
import com.trainerworkloadservice.TrainerWorkload.application.port.output.LoadTrainerWorkloadPort;
import com.trainerworkloadservice.TrainerWorkload.application.port.output.UpdateTrainerWorkloadPort;
import com.trainerworkloadservice.TrainerWorkload.domain.TrainerWorkload;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DynamoDBTrainerWorkloadRepository implements LoadTrainerWorkloadPort, UpdateTrainerWorkloadPort {
	private final DynamoDBMapper dynamoDBMapper;

	@Override
	public TrainerWorkload findByUsername(String username) {
		log.info("DynamoDB Repository: Finding trainer workload for username: {}", username);

		TrainerWorkload trainerWorkload = dynamoDBMapper.load(TrainerWorkload.class, username);

		if (trainerWorkload == null) {
			throw TrainerWorkloadNotFoundException.by(username);
		}

		log.info("DynamoDB Repository: Found trainer workload for username: {}", username);
		return trainerWorkload;
	}

	@Override
	public void save(TrainerWorkload trainerWorkload) {
		log.info("DynamoDB Repository: Saving trainer workload for username: {}", trainerWorkload.getUsername());

		dynamoDBMapper.save(trainerWorkload);

		log.info("DynamoDB Repository: Successfully saved workload for username: {}", trainerWorkload.getUsername());
	}

	// Optional: Query by firstName and lastName using GSI
	public List<TrainerWorkload> findByFirstNameAndLastName(String firstName, String lastName) {
		log.info("DynamoDB Repository: Querying by firstName: {} and lastName: {}", firstName, lastName);

		Map<String, AttributeValue> eav = new HashMap<>();
		eav.put(":firstName", new AttributeValue().withS(firstName));
		eav.put(":lastName", new AttributeValue().withS(lastName));

		DynamoDBQueryExpression<TrainerWorkload> queryExpression = new DynamoDBQueryExpression<TrainerWorkload>()
		        .withIndexName("TrainerName-Index").withConsistentRead(false)
		        .withKeyConditionExpression("firstName = :firstName AND lastName = :lastName")
		        .withExpressionAttributeValues(eav);

		List<TrainerWorkload> results = dynamoDBMapper.query(TrainerWorkload.class, queryExpression);

		log.info("DynamoDB Repository: Found {} results for firstName: {} and lastName: {}", results.size(), firstName,
		        lastName);

		return results;
	}
}
