package com.contractpulse.organization;

import com.contractpulse.organization.dto.CreateOrganizationRequest;
import com.contractpulse.organization.dto.OrganizationResponse;
import com.contractpulse.organization.dto.UpdateOrganizationRequest;
import com.contractpulse.organization.exception.DuplicateOrganizationNameException;
import com.contractpulse.organization.exception.OrganizationAccessDeniedException;
import com.contractpulse.organization.exception.OrganizationNotFoundException;
import com.contractpulse.organization.model.Organization;
import com.contractpulse.organization.model.OrganizationPlan;
import com.contractpulse.user.UserRepository;
import com.contractpulse.user.model.User;
import com.contractpulse.user.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrganizationService")
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrganizationService organizationService;

    private UUID ownerId;
    private User owner;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        owner = User.builder()
                .id(ownerId)
                .fullName("John Doe")
                .email("john@example.com")
                .role(UserRole.PROVIDER)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();
    }

    private Organization buildOrganization(UUID orgId, String name) {
        return Organization.builder()
                .id(orgId)
                .name(name)
                .owner(owner)
                .plan(OrganizationPlan.FREE)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("createOrganization")
    class CreateOrganization {

        @Test
        @DisplayName("deve criar organização com sucesso")
        void shouldCreateOrganizationSuccessfully() {
            var request = new CreateOrganizationRequest("My Agency");

            when(organizationRepository.existsByNameAndOwnerId("My Agency", ownerId)).thenReturn(false);
            when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
            when(organizationRepository.save(any(Organization.class))).thenAnswer(invocation -> {
                Organization org = invocation.getArgument(0);
                return Organization.builder()
                        .id(UUID.randomUUID())
                        .name(org.getName())
                        .owner(org.getOwner())
                        .plan(OrganizationPlan.FREE)
                        .createdAt(ZonedDateTime.now())
                        .updatedAt(ZonedDateTime.now())
                        .build();
            });

            OrganizationResponse response = organizationService.createOrganization(ownerId, request);

            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo("My Agency");
            assertThat(response.ownerId()).isEqualTo(ownerId);
            assertThat(response.plan()).isEqualTo(OrganizationPlan.FREE);

            verify(organizationRepository).save(any(Organization.class));
        }

        @Test
        @DisplayName("deve lançar exceção quando nome duplicado para o mesmo owner")
        void shouldThrowWhenDuplicateNameForSameOwner() {
            var request = new CreateOrganizationRequest("My Agency");

            when(organizationRepository.existsByNameAndOwnerId("My Agency", ownerId)).thenReturn(true);

            assertThatThrownBy(() -> organizationService.createOrganization(ownerId, request))
                    .isInstanceOf(DuplicateOrganizationNameException.class);

            verify(organizationRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando owner ID é null")
        void shouldThrowWhenOwnerIdIsNull() {
            var request = new CreateOrganizationRequest("My Agency");

            assertThatThrownBy(() -> organizationService.createOrganization(null, request))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Owner ID must not be null");
        }

        @Test
        @DisplayName("deve lançar exceção quando request é null")
        void shouldThrowWhenRequestIsNull() {
            assertThatThrownBy(() -> organizationService.createOrganization(ownerId, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("CreateOrganizationRequest must not be null");
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("deve retornar organização quando owner busca a própria")
        void shouldReturnOrganizationForOwner() {
            UUID orgId = UUID.randomUUID();
            Organization organization = buildOrganization(orgId, "My Agency");

            when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));

            OrganizationResponse response = organizationService.findById(orgId, ownerId);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(orgId);
            assertThat(response.name()).isEqualTo("My Agency");
        }

        @Test
        @DisplayName("deve lançar exceção quando organização não existe")
        void shouldThrowWhenOrganizationNotFound() {
            UUID orgId = UUID.randomUUID();
            when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> organizationService.findById(orgId, ownerId))
                    .isInstanceOf(OrganizationNotFoundException.class);
        }

        @Test
        @DisplayName("deve lançar exceção quando usuário não é owner")
        void shouldThrowWhenUserIsNotOwner() {
            UUID orgId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();
            Organization organization = buildOrganization(orgId, "My Agency");

            when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));

            assertThatThrownBy(() -> organizationService.findById(orgId, otherUserId))
                    .isInstanceOf(OrganizationAccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("findAllByOwner")
    class FindAllByOwner {

        @Test
        @DisplayName("deve retornar lista de organizações do owner")
        void shouldReturnOrganizationsForOwner() {
            Organization org1 = buildOrganization(UUID.randomUUID(), "Agency 1");
            Organization org2 = buildOrganization(UUID.randomUUID(), "Agency 2");

            when(organizationRepository.findByOwnerId(ownerId)).thenReturn(List.of(org1, org2));

            List<OrganizationResponse> responses = organizationService.findAllByOwner(ownerId);

            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).name()).isEqualTo("Agency 1");
            assertThat(responses.get(1).name()).isEqualTo("Agency 2");
        }

        @Test
        @DisplayName("deve retornar lista vazia quando owner não tem organizações")
        void shouldReturnEmptyListWhenNoOrganizations() {
            when(organizationRepository.findByOwnerId(ownerId)).thenReturn(List.of());

            List<OrganizationResponse> responses = organizationService.findAllByOwner(ownerId);

            assertThat(responses).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateOrganization")
    class UpdateOrganization {

        @Test
        @DisplayName("deve atualizar nome da organização com sucesso")
        void shouldUpdateOrganizationSuccessfully() {
            UUID orgId = UUID.randomUUID();
            Organization organization = buildOrganization(orgId, "Old Name");

            var request = new UpdateOrganizationRequest("New Name");

            when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
            when(organizationRepository.existsByNameAndOwnerId("New Name", ownerId)).thenReturn(false);
            when(organizationRepository.save(any(Organization.class))).thenAnswer(invocation -> invocation.getArgument(0));

            OrganizationResponse response = organizationService.updateOrganization(orgId, ownerId, request);

            assertThat(response.name()).isEqualTo("New Name");
            verify(organizationRepository).save(organization);
        }

        @Test
        @DisplayName("deve permitir atualizar com o mesmo nome (sem duplicidade)")
        void shouldAllowUpdatingWithSameName() {
            UUID orgId = UUID.randomUUID();
            Organization organization = buildOrganization(orgId, "Same Name");

            var request = new UpdateOrganizationRequest("Same Name");

            when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
            when(organizationRepository.save(any(Organization.class))).thenAnswer(invocation -> invocation.getArgument(0));

            OrganizationResponse response = organizationService.updateOrganization(orgId, ownerId, request);

            assertThat(response.name()).isEqualTo("Same Name");
            // Não deve verificar duplicidade quando o nome é o mesmo
            verify(organizationRepository, never()).existsByNameAndOwnerId(anyString(), any());
        }

        @Test
        @DisplayName("deve lançar exceção quando nome duplicado na atualização")
        void shouldThrowWhenDuplicateNameOnUpdate() {
            UUID orgId = UUID.randomUUID();
            Organization organization = buildOrganization(orgId, "Old Name");

            var request = new UpdateOrganizationRequest("Existing Name");

            when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
            when(organizationRepository.existsByNameAndOwnerId("Existing Name", ownerId)).thenReturn(true);

            assertThatThrownBy(() -> organizationService.updateOrganization(orgId, ownerId, request))
                    .isInstanceOf(DuplicateOrganizationNameException.class);

            verify(organizationRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando usuário não é owner ao atualizar")
        void shouldThrowWhenNotOwnerOnUpdate() {
            UUID orgId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();
            Organization organization = buildOrganization(orgId, "My Agency");

            var request = new UpdateOrganizationRequest("New Name");

            when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));

            assertThatThrownBy(() -> organizationService.updateOrganization(orgId, otherUserId, request))
                    .isInstanceOf(OrganizationAccessDeniedException.class);

            verify(organizationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteOrganization")
    class DeleteOrganization {

        @Test
        @DisplayName("deve deletar organização com sucesso")
        void shouldDeleteOrganizationSuccessfully() {
            UUID orgId = UUID.randomUUID();
            Organization organization = buildOrganization(orgId, "My Agency");

            when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));

            organizationService.deleteOrganization(orgId, ownerId);

            verify(organizationRepository).delete(organization);
        }

        @Test
        @DisplayName("deve lançar exceção quando organização não existe ao deletar")
        void shouldThrowWhenOrganizationNotFoundOnDelete() {
            UUID orgId = UUID.randomUUID();
            when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> organizationService.deleteOrganization(orgId, ownerId))
                    .isInstanceOf(OrganizationNotFoundException.class);

            verify(organizationRepository, never()).delete(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando usuário não é owner ao deletar")
        void shouldThrowWhenNotOwnerOnDelete() {
            UUID orgId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();
            Organization organization = buildOrganization(orgId, "My Agency");

            when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));

            assertThatThrownBy(() -> organizationService.deleteOrganization(orgId, otherUserId))
                    .isInstanceOf(OrganizationAccessDeniedException.class);

            verify(organizationRepository, never()).delete(any());
        }
    }
}
