package com.trainerworkloadservice.TrainerWorkload.adapter.input.web.controller;

import com.trainerworkloadservice.TrainerWorkload.adapter.input.web.request.TrainerWorkloadRequest;
import com.trainerworkloadservice.TrainerWorkload.adapter.input.web.response.TrainerMonthlyWorkloadResponse;
import com.trainerworkloadservice.TrainerWorkload.application.port.input.LoadTrainerMonthlyWorkloadUseCase;
import com.trainerworkloadservice.TrainerWorkload.application.port.input.ProcessTrainerWorkloadUseCase;
import java.util.UUID;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workload")
@RequiredArgsConstructor
@Slf4j
public class TrainerWorkloadController {
	private final ProcessTrainerWorkloadUseCase processTrainerWorkloadUseCase;
	private final LoadTrainerMonthlyWorkloadUseCase loadTrainerMonthlyWorkloadUseCase;

	@PostMapping
	@ResponseStatus(HttpStatus.OK)
	public void processTrainerWorkload(@Valid @RequestBody TrainerWorkloadRequest request,
	        @RequestHeader(value = "X-Transaction-ID",required = false) String transactionId) {
		if (transactionId == null || transactionId.isEmpty()) {
			transactionId = UUID.randomUUID().toString();
		}

		log.info("Transaction [{}]: Received trainer workload request for username: {}", transactionId,
		        request.getUsername());
		processTrainerWorkloadUseCase.processTrainerWorkload(request.toCommand(transactionId));
	}

	@GetMapping("/{username}/{year}/{month}")
	@ResponseStatus(HttpStatus.OK)
	public TrainerMonthlyWorkloadResponse getTrainerMonthlyWorkload(@PathVariable String username,
	        @PathVariable int year, @PathVariable int month,
	        @RequestHeader(value = "X-Transaction-ID",required = false) String transactionId) {

		if (transactionId == null || transactionId.isEmpty()) {
			transactionId = UUID.randomUUID().toString();
		}

		log.info("Transaction [{}]: Getting monthly workload for trainer: {}, year: {}, month: {}", transactionId,
		        username, year, month);

		return TrainerMonthlyWorkloadResponse.form(
		        loadTrainerMonthlyWorkloadUseCase.loadTrainerMonthlyWorkload(username, year, month, transactionId));
	}
}
