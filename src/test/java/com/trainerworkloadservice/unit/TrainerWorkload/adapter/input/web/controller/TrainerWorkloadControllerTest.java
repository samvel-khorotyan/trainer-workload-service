// package
// com.trainerworkloadservice.unit.TrainerWorkload.adapter.input.web.controller;
//
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.doNothing;
// import static org.mockito.Mockito.when;
//
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
// import
// com.trainerworkloadservice.TrainerWorkload.application.port.input.LoadTrainerMonthlyWorkloadUseCase;
// import
// com.trainerworkloadservice.TrainerWorkload.application.port.input.ProcessTrainerWorkloadCommand;
// import
// com.trainerworkloadservice.TrainerWorkload.application.port.input.ProcessTrainerWorkloadUseCase;
// import com.trainerworkloadservice.TrainerWorkload.domain.ActionType;
// import
// com.trainerworkloadservice.TrainerWorkload.domain.TrainerMonthlyWorkload;
// import java.time.LocalDate;
// import java.util.UUID;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mockito;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
// import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
// import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
// class TrainerWorkloadControllerTest {
// private MockMvc mockMvc;
// private ProcessTrainerWorkloadUseCase processTrainerWorkloadUseCase;
// private LoadTrainerMonthlyWorkloadUseCase loadTrainerMonthlyWorkloadUseCase;
// private ObjectMapper objectMapper;
//
// private String username;
// private String firstName;
// private String lastName;
// private Boolean isActive;
// private LocalDate trainingDate;
// private Integer trainingDuration;
// private ActionType actionType;
// private String transactionId;
// private int year;
// private int month;
//
// @BeforeEach
// public void setup() {
// processTrainerWorkloadUseCase =
// Mockito.mock(ProcessTrainerWorkloadUseCase.class);
// loadTrainerMonthlyWorkloadUseCase =
// Mockito.mock(LoadTrainerMonthlyWorkloadUseCase.class);
//
// TrainerWorkloadController controller = new
// TrainerWorkloadController(processTrainerWorkloadUseCase,
// loadTrainerMonthlyWorkloadUseCase);
//
// mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
//
// objectMapper = new ObjectMapper();
// objectMapper.registerModule(new JavaTimeModule());
//
// username = "trainer.username";
// firstName = "John";
// lastName = "Doe";
// isActive = true;
// trainingDate = LocalDate.of(2023, 5, 15);
// trainingDuration = 60;
// actionType = ActionType.ADD;
// transactionId = UUID.randomUUID().toString();
// year = 2023;
// month = 5;
// }
//
// @Test
// public void processTrainerWorkload_ShouldReturnOk_WhenRequestIsValid() throws
// Exception {
// doNothing().when(processTrainerWorkloadUseCase)
// .processTrainerWorkload(any(ProcessTrainerWorkloadCommand.class));
//
//
// mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/workload").contentType(MediaType.APPLICATION_JSON)
// .header("X-Transaction-ID", transactionId)
// .content(objectMapper.writeValueAsString(createWorkloadRequest())))
// .andExpect(MockMvcResultMatchers.status().isOk());
// }
//
// @Test
// public void
// processTrainerWorkload_ShouldGenerateTransactionId_WhenNotProvided() throws
// Exception
// {
// doNothing().when(processTrainerWorkloadUseCase)
// .processTrainerWorkload(any(ProcessTrainerWorkloadCommand.class));
//
//
// mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/workload").contentType(MediaType.APPLICATION_JSON)
// .content(objectMapper.writeValueAsString(createWorkloadRequest())))
// .andExpect(MockMvcResultMatchers.status().isOk());
// }
//
// @Test
// public void
// processTrainerWorkload_ShouldReturnBadRequest_WhenUsernameIsBlank() throws
// Exception
// {
// TrainerWorkloadRequest request = createWorkloadRequest();
// request.setUsername("");
//
//
// mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/workload").contentType(MediaType.APPLICATION_JSON)
// .content(objectMapper.writeValueAsString(request)))
// .andExpect(MockMvcResultMatchers.status().isBadRequest());
// }
//
// @Test
// public void
// processTrainerWorkload_ShouldReturnBadRequest_WhenFirstNameIsBlank() throws
// Exception
// {
// TrainerWorkloadRequest request = createWorkloadRequest();
// request.setFirstName("");
//
//
// mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/workload").contentType(MediaType.APPLICATION_JSON)
// .content(objectMapper.writeValueAsString(request)))
// .andExpect(MockMvcResultMatchers.status().isBadRequest());
// }
//
// @Test
// public void
// processTrainerWorkload_ShouldReturnBadRequest_WhenLastNameIsBlank() throws
// Exception
// {
// TrainerWorkloadRequest request = createWorkloadRequest();
// request.setLastName("");
//
//
// mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/workload").contentType(MediaType.APPLICATION_JSON)
// .content(objectMapper.writeValueAsString(request)))
// .andExpect(MockMvcResultMatchers.status().isBadRequest());
// }
//
// @Test
// public void
// processTrainerWorkload_ShouldReturnBadRequest_WhenIsActiveIsNull() throws
// Exception {
// TrainerWorkloadRequest request = createWorkloadRequest();
// request.setIsActive(null);
//
//
// mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/workload").contentType(MediaType.APPLICATION_JSON)
// .content(objectMapper.writeValueAsString(request)))
// .andExpect(MockMvcResultMatchers.status().isBadRequest());
// }
//
// @Test
// public void
// processTrainerWorkload_ShouldReturnBadRequest_WhenTrainingDateIsNull() throws
// Exception {
// TrainerWorkloadRequest request = createWorkloadRequest();
// request.setTrainingDate(null);
//
//
// mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/workload").contentType(MediaType.APPLICATION_JSON)
// .content(objectMapper.writeValueAsString(request)))
// .andExpect(MockMvcResultMatchers.status().isBadRequest());
// }
//
// @Test
// public void
// processTrainerWorkload_ShouldReturnBadRequest_WhenTrainingDurationIsNull()
// throws
// Exception {
// TrainerWorkloadRequest request = createWorkloadRequest();
// request.setTrainingDuration(null);
//
//
// mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/workload").contentType(MediaType.APPLICATION_JSON)
// .content(objectMapper.writeValueAsString(request)))
// .andExpect(MockMvcResultMatchers.status().isBadRequest());
// }
//
// @Test
// public void
// processTrainerWorkload_ShouldReturnBadRequest_WhenTrainingDurationIsNegative()
// throws
// Exception {
// TrainerWorkloadRequest request = createWorkloadRequest();
// request.setTrainingDuration(-60);
//
//
// mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/workload").contentType(MediaType.APPLICATION_JSON)
// .content(objectMapper.writeValueAsString(request)))
// .andExpect(MockMvcResultMatchers.status().isBadRequest());
// }
//
// @Test
// public void
// processTrainerWorkload_ShouldReturnBadRequest_WhenTrainingDurationIsZero()
// throws
// Exception {
// TrainerWorkloadRequest request = createWorkloadRequest();
// request.setTrainingDuration(0);
//
//
// mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/workload").contentType(MediaType.APPLICATION_JSON)
// .content(objectMapper.writeValueAsString(request)))
// .andExpect(MockMvcResultMatchers.status().isBadRequest());
// }
//
// @Test
// public void
// processTrainerWorkload_ShouldReturnBadRequest_WhenActionTypeIsNull() throws
// Exception
// {
// TrainerWorkloadRequest request = createWorkloadRequest();
// request.setActionType(null);
//
//
// mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/workload").contentType(MediaType.APPLICATION_JSON)
// .content(objectMapper.writeValueAsString(request)))
// .andExpect(MockMvcResultMatchers.status().isBadRequest());
// }
//
// @Test
// public void getTrainerMonthlyWorkload_ShouldReturnOk_WhenRequestIsValid()
// throws Exception {
// TrainerMonthlyWorkload workload =
// TrainerMonthlyWorkload.builder().username(username).firstName(firstName)
//
// .lastName(lastName).isActive(isActive).year(year).month(month).summaryDuration(120).build();
//
// when(loadTrainerMonthlyWorkloadUseCase.loadTrainerMonthlyWorkload(username,
// year, month,
// transactionId))
// .thenReturn(workload);
//
// mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/workload/{username}/{year}/{month}",
// username, year, month)
// .header("X-Transaction-ID",
// transactionId).contentType(MediaType.APPLICATION_JSON))
// .andExpect(MockMvcResultMatchers.status().isOk())
// .andExpect(MockMvcResultMatchers.jsonPath("$.username").value(username));
// }
//
// @Test
// public void
// getTrainerMonthlyWorkload_ShouldGenerateTransactionId_WhenNotProvided()
// throws
// Exception {
// TrainerMonthlyWorkload workload =
// TrainerMonthlyWorkload.builder().username(username).firstName(firstName)
//
// .lastName(lastName).isActive(isActive).year(year).month(month).summaryDuration(120).build();
//
// when(loadTrainerMonthlyWorkloadUseCase.loadTrainerMonthlyWorkload(eq(username),
// eq(year),
// eq(month),
// any(String.class))).thenReturn(workload);
//
// mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/workload/{username}/{year}/{month}",
// username, year, month)
//
// .contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk())
// .andExpect(MockMvcResultMatchers.jsonPath("$.username").value(username));
// }
//
// private TrainerWorkloadRequest createWorkloadRequest() {
// return
// TrainerWorkloadRequest.builder().username(username).firstName(firstName).lastName(lastName)
//
// .isActive(isActive).trainingDate(trainingDate).trainingDuration(trainingDuration).actionType(actionType)
// .build();
// }
//
// private static String eq(String value) {
// return Mockito.eq(value);
// }
//
// private static int eq(int value) {
// return Mockito.eq(value);
// }
// }
