package com.contractpulse.contract;

import com.contractpulse.auth.CurrentUserService;
import com.contractpulse.contract.dto.*;
import com.contractpulse.contract.exception.ContractInactiveException;
import com.contractpulse.contract.exception.ContractNotFoundException;
import com.contractpulse.contract.exception.InvalidContractConfigException;
import com.contractpulse.contract.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ContractController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.contractpulse\\.auth\\.JwtSupabaseFilter"
        )
)
@DisplayName("ContractController")
class ContractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContractService contractService;

    @MockBean
    private ClientDashboardService clientDashboardService;

    @MockBean
    private CurrentUserService currentUserService;

    private UUID contractId;
    private UUID organizationId;
    private UUID clientUserId;

    @BeforeEach
    void setUp() {
        contractId = UUID.randomUUID();
        organizationId = UUID.randomUUID();
        clientUserId = UUID.randomUUID();
    }

    private ContractResponse buildContractResponse() {
        return new ContractResponse(
                contractId,
                organizationId,
                clientUserId,
                "Test Contract",
                ContractType.RETAINER,
                ContractCurrency.BRL,
                15,
                LocalDate.of(2025, 1, 1),
                null,
                ContractStatus.ACTIVE,
                Map.of("monthlyHours", 40, "hourlyRate", 150.0),
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );
    }

    // --- POST /api/contracts ---

    @Test
    @WithMockUser
    @DisplayName("POST /api/contracts — deve criar contrato com 201")
    void shouldCreateContract() throws Exception {
        var request = new CreateContractRequest(
                organizationId, clientUserId, "New Contract",
                ContractType.RETAINER, ContractCurrency.BRL, 15,
                LocalDate.of(2025, 1, 1), null,
                new RetainerConfigRequest(40, new BigDecimal("150.00"),
                        RolloverPolicy.EXPIRE, 80, false, null)
        );
        var response = buildContractResponse();

        when(contractService.createContract(any(CreateContractRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/contracts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Test Contract")))
                .andExpect(jsonPath("$.type", is("RETAINER")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.currency", is("BRL")))
                .andExpect(jsonPath("$.billingDay", is(15)))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.organizationId", is(organizationId.toString())))
                .andExpect(jsonPath("$.clientUserId", is(clientUserId.toString())));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/contracts — deve retornar 400 com título em branco")
    void shouldReturn400WhenTitleIsBlank() throws Exception {
        var request = new CreateContractRequest(
                organizationId, clientUserId, "",
                ContractType.RETAINER, ContractCurrency.BRL, 15,
                LocalDate.of(2025, 1, 1), null,
                new RetainerConfigRequest(40, new BigDecimal("150.00"),
                        RolloverPolicy.EXPIRE, 80, false, null)
        );

        mockMvc.perform(post("/api/contracts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(contractService, never()).createContract(any());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/contracts — deve retornar 400 com billingDay fora do range")
    void shouldReturn400WhenBillingDayOutOfRange() throws Exception {
        var request = new CreateContractRequest(
                organizationId, clientUserId, "Valid Title",
                ContractType.RETAINER, ContractCurrency.BRL, 30,
                LocalDate.of(2025, 1, 1), null,
                new RetainerConfigRequest(40, new BigDecimal("150.00"),
                        RolloverPolicy.EXPIRE, 80, false, null)
        );

        mockMvc.perform(post("/api/contracts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(contractService, never()).createContract(any());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/contracts — deve retornar 400 com mensalHours negativo no retainerConfig")
    void shouldReturn400WhenMonthlyHoursNegative() throws Exception {
        var request = new CreateContractRequest(
                organizationId, clientUserId, "Valid Title",
                ContractType.RETAINER, ContractCurrency.BRL, 15,
                LocalDate.of(2025, 1, 1), null,
                new RetainerConfigRequest(-10, new BigDecimal("150.00"),
                        RolloverPolicy.EXPIRE, 80, false, null)
        );

        mockMvc.perform(post("/api/contracts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(contractService, never()).createContract(any());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/contracts — deve retornar 400 quando config inválida")
    void shouldReturn400WhenInvalidConfig() throws Exception {
        var request = new CreateContractRequest(
                organizationId, clientUserId, "Valid Title",
                ContractType.RETAINER, ContractCurrency.BRL, 15,
                LocalDate.of(2025, 1, 1), null, null
        );

        when(contractService.createContract(any(CreateContractRequest.class)))
                .thenThrow(new InvalidContractConfigException("Retainer config is required"));

        mockMvc.perform(post("/api/contracts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("Retainer config")));
    }

    // --- GET /api/contracts/{id} ---

    @Test
    @WithMockUser
    @DisplayName("GET /api/contracts/{id} — deve retornar contrato existente")
    void shouldReturnContractById() throws Exception {
        var response = buildContractResponse();
        when(contractService.findById(contractId)).thenReturn(response);

        mockMvc.perform(get("/api/contracts/" + contractId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(contractId.toString())))
                .andExpect(jsonPath("$.title", is("Test Contract")))
                .andExpect(jsonPath("$.type", is("RETAINER")));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/contracts/{id} — deve retornar 404 quando não existe")
    void shouldReturn404WhenContractNotFound() throws Exception {
        UUID missingId = UUID.randomUUID();
        when(contractService.findById(missingId))
                .thenThrow(new ContractNotFoundException(missingId));

        mockMvc.perform(get("/api/contracts/" + missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", containsString(missingId.toString())));
    }

    // --- GET /api/contracts/organization/{organizationId} ---

    @Test
    @WithMockUser
    @DisplayName("GET /api/contracts/organization/{orgId} — deve listar contratos")
    void shouldListContractsByOrganization() throws Exception {
        var response = buildContractResponse();
        when(contractService.findByOrganization(organizationId))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/contracts/organization/" + organizationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Test Contract")))
                .andExpect(jsonPath("$[0].organizationId", is(organizationId.toString())));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/contracts/organization/{orgId} — deve retornar lista vazia")
    void shouldReturnEmptyListForOrganization() throws Exception {
        when(contractService.findByOrganization(organizationId)).thenReturn(List.of());

        mockMvc.perform(get("/api/contracts/organization/" + organizationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // --- GET /api/contracts/organization/{organizationId}/active ---

    @Test
    @WithMockUser
    @DisplayName("GET /api/contracts/organization/{orgId}/active — deve listar contratos ativos")
    void shouldListActiveContractsByOrganization() throws Exception {
        var response = buildContractResponse();
        when(contractService.findActiveByOrganization(organizationId))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/contracts/organization/" + organizationId + "/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("ACTIVE")));
    }

    // --- GET /api/contracts/client/{clientUserId} ---

    @Test
    @WithMockUser
    @DisplayName("GET /api/contracts/client/{clientId} — deve listar contratos do cliente")
    void shouldListContractsByClient() throws Exception {
        var response = buildContractResponse();
        when(contractService.findByClient(clientUserId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/contracts/client/" + clientUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].clientUserId", is(clientUserId.toString())));
    }

    // --- PUT /api/contracts/{id} ---

    @Test
    @WithMockUser
    @DisplayName("PUT /api/contracts/{id} — deve atualizar contrato")
    void shouldUpdateContract() throws Exception {
        var request = new UpdateContractRequest("Updated Title", null, null, null, null);
        var response = new ContractResponse(
                contractId, organizationId, clientUserId, "Updated Title",
                ContractType.RETAINER, ContractCurrency.BRL, 15,
                LocalDate.of(2025, 1, 1), null, ContractStatus.ACTIVE,
                Map.of("monthlyHours", 40), ZonedDateTime.now(), ZonedDateTime.now()
        );

        when(contractService.updateContract(eq(contractId), any(UpdateContractRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/contracts/" + contractId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Title")));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/contracts/{id} — deve retornar 404 quando contrato não existe")
    void shouldReturn404OnUpdateWhenNotFound() throws Exception {
        UUID missingId = UUID.randomUUID();
        var request = new UpdateContractRequest("Try", null, null, null, null);

        when(contractService.updateContract(eq(missingId), any(UpdateContractRequest.class)))
                .thenThrow(new ContractNotFoundException(missingId));

        mockMvc.perform(put("/api/contracts/" + missingId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/contracts/{id} — deve retornar 409 quando contrato inativo")
    void shouldReturn409OnUpdateWhenInactive() throws Exception {
        var request = new UpdateContractRequest("Try", null, null, null, null);

        when(contractService.updateContract(eq(contractId), any(UpdateContractRequest.class)))
                .thenThrow(new ContractInactiveException(contractId));

        mockMvc.perform(put("/api/contracts/" + contractId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // --- PATCH /api/contracts/{id}/pause ---

    @Test
    @WithMockUser
    @DisplayName("PATCH /api/contracts/{id}/pause — deve pausar contrato com 200")
    void shouldPauseContract() throws Exception {
        var response = new ContractResponse(
                contractId, organizationId, clientUserId, "Test Contract",
                ContractType.RETAINER, ContractCurrency.BRL, 15,
                LocalDate.of(2025, 1, 1), null, ContractStatus.PAUSED,
                Map.of(), ZonedDateTime.now(), ZonedDateTime.now()
        );

        when(contractService.pauseContract(contractId)).thenReturn(response);

        mockMvc.perform(patch("/api/contracts/" + contractId + "/pause")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PAUSED")));
    }

    @Test
    @WithMockUser
    @DisplayName("PATCH /api/contracts/{id}/pause — deve retornar 409 quando já inativo")
    void shouldReturn409OnPauseWhenInactive() throws Exception {
        when(contractService.pauseContract(contractId))
                .thenThrow(new ContractInactiveException(contractId));

        mockMvc.perform(patch("/api/contracts/" + contractId + "/pause")
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    // --- PATCH /api/contracts/{id}/resume ---

    @Test
    @WithMockUser
    @DisplayName("PATCH /api/contracts/{id}/resume — deve reativar contrato com 200")
    void shouldResumeContract() throws Exception {
        var response = new ContractResponse(
                contractId, organizationId, clientUserId, "Test Contract",
                ContractType.RETAINER, ContractCurrency.BRL, 15,
                LocalDate.of(2025, 1, 1), null, ContractStatus.ACTIVE,
                Map.of(), ZonedDateTime.now(), ZonedDateTime.now()
        );

        when(contractService.resumeContract(contractId)).thenReturn(response);

        mockMvc.perform(patch("/api/contracts/" + contractId + "/resume")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    @WithMockUser
    @DisplayName("PATCH /api/contracts/{id}/resume — deve retornar 409 quando não está pausado")
    void shouldReturn409OnResumeWhenNotPaused() throws Exception {
        when(contractService.resumeContract(contractId))
                .thenThrow(new ContractInactiveException(contractId));

        mockMvc.perform(patch("/api/contracts/" + contractId + "/resume")
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    // --- PATCH /api/contracts/{id}/terminate ---

    @Test
    @WithMockUser
    @DisplayName("PATCH /api/contracts/{id}/terminate — deve encerrar contrato com 200")
    void shouldTerminateContract() throws Exception {
        var response = new ContractResponse(
                contractId, organizationId, clientUserId, "Test Contract",
                ContractType.RETAINER, ContractCurrency.BRL, 15,
                LocalDate.of(2025, 1, 1), null, ContractStatus.TERMINATED,
                Map.of(), ZonedDateTime.now(), ZonedDateTime.now()
        );

        when(contractService.terminateContract(contractId)).thenReturn(response);

        mockMvc.perform(patch("/api/contracts/" + contractId + "/terminate")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("TERMINATED")));
    }

    @Test
    @WithMockUser
    @DisplayName("PATCH /api/contracts/{id}/terminate — deve retornar 409 quando já encerrado")
    void shouldReturn409OnTerminateWhenAlreadyTerminated() throws Exception {
        when(contractService.terminateContract(contractId))
                .thenThrow(new ContractInactiveException(contractId));

        mockMvc.perform(patch("/api/contracts/" + contractId + "/terminate")
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    // --- Autenticação ---

    @Test
    @DisplayName("Deve retornar 401 sem autenticação")
    void shouldReturn401WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/contracts/" + contractId))
                .andExpect(status().isUnauthorized());
    }
}
