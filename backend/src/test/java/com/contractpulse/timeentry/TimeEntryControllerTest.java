package com.contractpulse.timeentry;

import com.contractpulse.auth.CurrentUserService;
import com.contractpulse.timeentry.dto.CreateTimeEntryRequest;
import com.contractpulse.timeentry.dto.ReviewTimeEntryRequest;
import com.contractpulse.timeentry.dto.TimeEntryResponse;
import com.contractpulse.timeentry.model.TimeEntryStatus;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes da camada web para TimeEntryController.
 */
@WebMvcTest(
        controllers = TimeEntryController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.contractpulse\\.auth\\.JwtSupabaseFilter"
        )
)
@DisplayName("TimeEntryController")
class TimeEntryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TimeEntryService timeEntryService;

    @MockBean
    private CurrentUserService currentUserService;

    private UUID userId;
    private UUID contractId;
    private UUID timeEntryId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        contractId = UUID.randomUUID();
        timeEntryId = UUID.randomUUID();
        when(currentUserService.getCurrentUserId()).thenReturn(userId);
    }

    private TimeEntryResponse buildResponse(TimeEntryStatus status) {
        return new TimeEntryResponse(
                timeEntryId, contractId, userId, "Desenvolvimento",
                new BigDecimal("4.0"), LocalDate.now(), status,
                null, null, null,
                ZonedDateTime.now(), ZonedDateTime.now()
        );
    }

    // --- POST /api/time-entries ---

    @Test
    @WithMockUser
    @DisplayName("POST /api/time-entries — deve criar lançamento com 201")
    void shouldCreateTimeEntry() throws Exception {
        var request = new CreateTimeEntryRequest(
                contractId, "Desenvolvimento feature Y", new BigDecimal("4.0"), LocalDate.now());

        when(timeEntryService.createTimeEntry(eq(userId), any(CreateTimeEntryRequest.class)))
                .thenReturn(buildResponse(TimeEntryStatus.PENDING));

        mockMvc.perform(post("/api/time-entries")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(timeEntryId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/time-entries — deve retornar 400 com descrição vazia")
    void shouldReturn400ForBlankDescription() throws Exception {
        var request = new CreateTimeEntryRequest(
                contractId, "", new BigDecimal("4.0"), LocalDate.now());

        mockMvc.perform(post("/api/time-entries")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/time-entries — deve retornar 400 com horas zero")
    void shouldReturn400ForZeroHours() throws Exception {
        var request = new CreateTimeEntryRequest(
                contractId, "Dev", new BigDecimal("0"), LocalDate.now());

        mockMvc.perform(post("/api/time-entries")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/time-entries — deve retornar 400 sem contractId")
    void shouldReturn400ForNullContractId() throws Exception {
        var request = new CreateTimeEntryRequest(
                null, "Dev", new BigDecimal("4.0"), LocalDate.now());

        mockMvc.perform(post("/api/time-entries")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // --- GET /api/time-entries/contract/{contractId} ---

    @Test
    @WithMockUser
    @DisplayName("GET /api/time-entries/contract/{id} — deve listar lançamentos")
    void shouldListByContract() throws Exception {
        when(timeEntryService.findByContract(contractId))
                .thenReturn(List.of(buildResponse(TimeEntryStatus.PENDING)));

        mockMvc.perform(get("/api/time-entries/contract/" + contractId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/time-entries/contract/{id} — deve retornar lista vazia")
    void shouldReturnEmptyListByContract() throws Exception {
        when(timeEntryService.findByContract(contractId)).thenReturn(List.of());

        mockMvc.perform(get("/api/time-entries/contract/" + contractId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // --- GET /api/time-entries/contract/{contractId}/pending ---

    @Test
    @WithMockUser
    @DisplayName("GET /api/time-entries/contract/{id}/pending — deve listar pendentes")
    void shouldListPendingByContract() throws Exception {
        when(timeEntryService.findPendingByContract(contractId))
                .thenReturn(List.of(buildResponse(TimeEntryStatus.PENDING)));

        mockMvc.perform(get("/api/time-entries/contract/" + contractId + "/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    // --- PATCH /api/time-entries/{id}/approve ---

    @Test
    @WithMockUser
    @DisplayName("PATCH /api/time-entries/{id}/approve — deve aprovar")
    void shouldApproveTimeEntry() throws Exception {
        TimeEntryResponse approved = new TimeEntryResponse(
                timeEntryId, contractId, UUID.randomUUID(), "Dev",
                new BigDecimal("3.0"), LocalDate.now(), TimeEntryStatus.APPROVED,
                userId, ZonedDateTime.now(), null,
                ZonedDateTime.now(), ZonedDateTime.now()
        );

        when(timeEntryService.approveTimeEntry(userId, timeEntryId)).thenReturn(approved);

        mockMvc.perform(patch("/api/time-entries/" + timeEntryId + "/approve")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    // --- PATCH /api/time-entries/{id}/dispute ---

    @Test
    @WithMockUser
    @DisplayName("PATCH /api/time-entries/{id}/dispute — deve disputar")
    void shouldDisputeTimeEntry() throws Exception {
        var request = new ReviewTimeEntryRequest("Horas excessivas");

        TimeEntryResponse disputed = new TimeEntryResponse(
                timeEntryId, contractId, UUID.randomUUID(), "Dev",
                new BigDecimal("8.0"), LocalDate.now(), TimeEntryStatus.DISPUTED,
                userId, ZonedDateTime.now(), "Horas excessivas",
                ZonedDateTime.now(), ZonedDateTime.now()
        );

        when(timeEntryService.disputeTimeEntry(eq(userId), eq(timeEntryId), any(ReviewTimeEntryRequest.class)))
                .thenReturn(disputed);

        mockMvc.perform(patch("/api/time-entries/" + timeEntryId + "/dispute")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISPUTED"))
                .andExpect(jsonPath("$.disputeReason").value("Horas excessivas"));
    }

    // --- Autenticação ---

    @Test
    @DisplayName("Deve retornar 401 sem autenticação")
    void shouldReturn401WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/time-entries/contract/" + contractId))
                .andExpect(status().isUnauthorized());
    }
}
