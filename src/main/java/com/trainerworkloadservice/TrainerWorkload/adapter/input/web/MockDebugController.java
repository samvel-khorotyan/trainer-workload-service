package com.trainerworkloadservice.TrainerWorkload.adapter.input.web;

import com.trainerworkloadservice.TrainerWorkload.adapter.output.persistence.TrainerWorkloadRepository;
import com.trainerworkloadservice.TrainerWorkload.domain.TrainerWorkload;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.mode",havingValue = "mock")
public class MockDebugController {

	private final TrainerWorkloadRepository repository;

	@GetMapping("/workloads")
	public Map<String, TrainerWorkload> getAllWorkloads() {
		return repository.getAllWorkloads();
	}

	@GetMapping("/clear")
	public String clearAllWorkloads() {
		repository.clearAll();
		return "All mock data cleared";
	}
}
