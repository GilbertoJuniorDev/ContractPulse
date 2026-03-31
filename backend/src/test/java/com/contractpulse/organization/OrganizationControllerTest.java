package com.contractpulse.organization;

import com.contractpulse.auth.CurrentUserService;
import com.contractpulse.organization.dto.CreateOrganizationRequest;
import com.contractpulse.organization.dto.OrganizationResponse;
import com.contractpulse.organization.dto.UpdateOrganizationRequest;
import com.contractpulse.organization.exception.OrganizationAccessDeniedException;
import com.contractpulse.organization.exception.OrganizationNotFoundException;
import com.contractpulse.organization.model.OrganizationPlan;
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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = OrganizationController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.contractpulse\\.auth\\.JwtSupabaseFilter"
        )
)
@DisplayName("OrganizationController")
class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrganizationService organizationService;

    @MockBean
    private CurrentUserService currentUserService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        when(currentUserService.getCurrentUserId()).thenReturn(userId);
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/organizations — deve criar organização com 201")
    void shouldCreateOrganization() throws Exception {
        var request = new CreateOrganizationRequest("Test Agency");
        var response = new OrganizationResponse(
                UUID.randomUUID(), "Test Agency", userId,
                OrganizationPlan.FREE, ZonedDateTime.now(), ZonedDateTime.now()
        );

        when(organizationService.createOrganization(eq(userId), any(CreateOrganizationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/organizations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Test Agency")))
                .andExpect(jsonPath("$.ownerId", is(userId.toString())))
                .andExpect(jsonPath("$.plan", is("FREE")))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/organizations — deve retornar 400 com nome em branco")
    void shouldReturn400WhenNameIsBlank() throws Exception {
        var request = new CreateOrganizationRequest("");

        mockMvc.perform(post("/api/organizations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/organizations — deve retornar 400 com nome muito curto")
    void shouldReturn400WhenNameTooShort() throws Exception {
        var request = new CreateOrganizationRequest("A");

        mockMvc.perform(post("/api/organizations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/organizations — deve listar organizações do owner")
    void shouldListOrganizations() throws Exception {
        var response1 = new OrganizationResponse(
                UUID.randomUUID(), "Agency 1", userId,
                OrganizationPlan.FREE, ZonedDateTime.now(), ZonedDateTime.now()
        );
        var response2 = new OrganizationResponse(
                UUID.randomUUID(), "Agency 2", userId,
                OrganizationPlan.PRO, ZonedDateTime.now(), ZonedDateTime.now()
        );

        when(organizationService.findAllByOwner(userId)).thenReturn(List.of(response1, response2));

        mockMvc.perform(get("/api/organizations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Agency 1")))
                .andExpect(jsonPath("$[1].name", is("Agency 2")));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/organizations/{id} — deve retornar organização")
    void shouldReturnOrganizationById() throws Exception {
        UUID orgId = UUID.randomUUID();
        var response = new OrganizationResponse(
                orgId, "My Agency", userId,
                OrganizationPlan.FREE, ZonedDateTime.now(), ZonedDateTime.now()
        );

        when(organizationService.findById(orgId, userId)).thenReturn(response);

        mockMvc.perform(get("/api/organizations/" + orgId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(orgId.toString())))
                .andExpect(jsonPath("$.name", is("My Agency")));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/organizations/{id} — deve retornar 404 quando não existe")
    void shouldReturn404WhenNotFound() throws Exception {
        UUID orgId = UUID.randomUUID();

        when(organizationService.findById(orgId, userId))
                .thenThrow(new OrganizationNotFoundException(orgId));

        mockMvc.perform(get("/api/organizations/" + orgId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/organizations/{id} — deve retornar 403 para outro usuário")
    void shouldReturn403ForUnauthorizedUser() throws Exception {
        UUID orgId = UUID.randomUUID();

        when(organizationService.findById(orgId, userId))
                .thenThrow(new OrganizationAccessDeniedException(userId, orgId));

        mockMvc.perform(get("/api/organizations/" + orgId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/organizations/{id} — deve atualizar organização")
    void shouldUpdateOrganization() throws Exception {
        UUID orgId = UUID.randomUUID();
        var request = new UpdateOrganizationRequest("New Name");
        var response = new OrganizationResponse(
                orgId, "New Name", userId,
                OrganizationPlan.FREE, ZonedDateTime.now(), ZonedDateTime.now()
        );

        when(organizationService.updateOrganization(eq(orgId), eq(userId), any(UpdateOrganizationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/organizations/" + orgId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("New Name")));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/organizations/{id} — deve deletar organização com 204")
    void shouldDeleteOrganization() throws Exception {
        UUID orgId = UUID.randomUUID();

        doNothing().when(organizationService).deleteOrganization(orgId, userId);

        mockMvc.perform(delete("/api/organizations/" + orgId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(organizationService).deleteOrganization(orgId, userId);
    }

    @Test
    @DisplayName("Deve retornar 401 sem autenticação")
    void shouldReturn401WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/organizations"))
                .andExpect(status().isUnauthorized());
    }
}
