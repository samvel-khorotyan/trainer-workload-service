package com.trainerworkloadservice.unit.TrainerWorkload.adapter.output.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.trainerworkloadservice.TrainerWorkload.adapter.output.persistence.TrainerWorkloadPersistenceRepository;
import com.trainerworkloadservice.TrainerWorkload.adapter.output.persistence.TrainerWorkloadRepository;
import com.trainerworkloadservice.TrainerWorkload.application.exception.TrainerWorkloadNotFoundException;
import com.trainerworkloadservice.TrainerWorkload.domain.TrainerWorkload;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadRepositoryTest {
	@Mock
	private TrainerWorkloadPersistenceRepository persistenceRepository;

	@InjectMocks
	private TrainerWorkloadRepository trainerWorkloadRepository;

	private String username;
	private TrainerWorkload trainerWorkload;

	@BeforeEach
	void setUp() {
		username = "trainer.username";
		trainerWorkload = TrainerWorkload.builder().username(username).firstName("John").lastName("Doe").isActive(true)
		        .build();
	}

	@Test
  void findByUsername_ShouldReturnTrainerWorkload_WhenUsernameExists() {
    when(persistenceRepository.findByUsername(username)).thenReturn(Optional.of(trainerWorkload));

    TrainerWorkload result = trainerWorkloadRepository.findByUsername(username);

    assertNotNull(result);
    assertEquals(username, result.getUsername());
    assertEquals("John", result.getFirstName());
    assertEquals("Doe", result.getLastName());
    assertTrue(result.getIsActive());
    verify(persistenceRepository).findByUsername(username);
  }

	@Test
  void findByUsername_ShouldThrowTrainerWorkloadNotFoundException_WhenUsernameDoesNotExist() {
    when(persistenceRepository.findByUsername(username)).thenReturn(Optional.empty());

    TrainerWorkloadNotFoundException exception =
        assertThrows(
            TrainerWorkloadNotFoundException.class,
            () -> trainerWorkloadRepository.findByUsername(username));

    assertTrue(exception.getMessage().contains(username));
    verify(persistenceRepository).findByUsername(username);
  }

	@Test
	void save_ShouldCallPersistenceRepositorySave() {
		trainerWorkloadRepository.save(trainerWorkload);

		verify(persistenceRepository).save(trainerWorkload);
	}
}
