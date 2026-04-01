package com.contractpulse.timeentry;

import com.contractpulse.contract.ContractRepository;
import com.contractpulse.contract.exception.ContractNotFoundException;
import com.contractpulse.contract.model.Contract;
import com.contractpulse.contract.model.ContractCurrency;
import com.contractpulse.contract.model.ContractStatus;
import com.contractpulse.contract.model.ContractType;
import com.contractpulse.timeentry.dto.CreateTimeEntryRequest;
import com.contractpulse.timeentry.dto.ReviewTimeEntryRequest;
import com.contractpulse.timeentry.dto.TimeEntryResponse;
import com.contractpulse.timeentry.exception.TimeEntryNotFoundException;
import com.contractpulse.timeentry.exception.UnauthorizedReviewException;
import com.contractpulse.timeentry.model.TimeEntry;
import com.contractpulse.timeentry.model.TimeEntryStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para o TimeEntryService.
 * Ciclo de vida: DRAFT → SUBMITTED → PENDING_APPROVAL → APPROVED | DISPUTED → INVOICED
 * Cobertura mínima alvo: 80%.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TimeEntryService")
class TimeEntryServiceTest {

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private ContractRepository contractRepository;

    @InjectMocks
    private TimeEntryService timeEntryService;

    private UUID userId;
    private UUID clientUserId;
    private UUID contractId;
    private UUID timeEntryId;
    private Contract activeContract;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        clientUserId = UUID.randomUUID();
        contractId = UUID.randomUUID();
        timeEntryId = UUID.randomUUID();

        activeContract = Contract.builder()
                .id(contractId)
                .organizationId(UUID.randomUUID())
                .clientUserId(clientUserId)
                .title("Suporte Mensal")
                .type(ContractType.RETAINER)
                .currency(ContractCurrency.BRL)
                .billingDay(15)
                .startDate(LocalDate.of(2024, 1, 1))
                .status(ContractStatus.ACTIVE)
                .config(Map.of("monthlyHours", 40))
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();
    }

    /**
     * Helper: constrói um lançamento em DRAFT (recém-criado).
     */
    private TimeEntry buildDraftEntry() {
        return TimeEntry.builder()
                .id(timeEntryId)
                .contractId(contractId)
                .userId(userId)
                .description("Desenvolvimento feature X")
                .hours(new BigDecimal("4.5"))
                .entryDate(LocalDate.now())
                .status(TimeEntryStatus.DRAFT)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();
    }

    /**
     * Helper: constrói um lançamento em PENDING_APPROVAL.
     */
    private TimeEntry buildPendingApprovalEntry() {
        return TimeEntry.builder()
                .id(timeEntryId)
                .contractId(contractId)
                .userId(userId)
                .description("Desenvolvimento feature X")
                .hours(new BigDecimal("4.5"))
                .entryDate(LocalDate.now())
                .status(TimeEntryStatus.PENDING_APPROVAL)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();
    }

    /**
     * Helper: constrói um lançamento em SUBMITTED.
     */
    private TimeEntry buildSubmittedEntry() {
        return TimeEntry.builder()
                .id(timeEntryId)
                .contractId(contractId)
                .userId(userId)
                .description("Desenvolvimento feature X")
                .hours(new BigDecimal("4.5"))
                .entryDate(LocalDate.now())
                .status(TimeEntryStatus.SUBMITTED)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();
    }

    // --- Criação ---

    @Nested
    @DisplayName("createTimeEntry")
    class CreateTimeEntry {

        @Test
        @DisplayName("deve criar lançamento como DRAFT para contrato ativo")
        void shouldCreateTimeEntryAsDraftForActiveContract() {
            // Arrange
            var request = new CreateTimeEntryRequest(
                    contractId, "Desenvolvimento feature X", new BigDecimal("4.5"), LocalDate.now());

            when(contractRepository.findById(contractId)).thenReturn(Optional.of(activeContract));
            when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(inv -> {
                TimeEntry entry = inv.getArgument(0);
                return TimeEntry.builder()
                        .id(timeEntryId)
                        .contractId(entry.getContractId())
                        .userId(entry.getUserId())
                        .description(entry.getDescription())
                        .hours(entry.getHours())
                        .entryDate(entry.getEntryDate())
                        .status(TimeEntryStatus.DRAFT)
                        .createdAt(ZonedDateTime.now())
                        .updatedAt(ZonedDateTime.now())
                        .build();
            });

            // Act
            TimeEntryResponse response = timeEntryService.createTimeEntry(userId, request);

            // Assert
            assertThat(response.id()).isEqualTo(timeEntryId);
            assertThat(response.contractId()).isEqualTo(contractId);
            assertThat(response.userId()).isEqualTo(userId);
            assertThat(response.hours()).isEqualByComparingTo(new BigDecimal("4.5"));
            assertThat(response.status()).isEqualTo(TimeEntryStatus.DRAFT);
            verify(timeEntryRepository).save(any(TimeEntry.class));
        }

        @Test
        @DisplayName("deve lançar exceção para contrato pausado")
        void shouldThrowForPausedContract() {
            // Arrange
            Contract pausedContract = Contract.builder()
                    .id(contractId)
                    .organizationId(UUID.randomUUID())
                    .clientUserId(clientUserId)
                    .title("Contrato Pausado")
                    .type(ContractType.RETAINER)
                    .billingDay(15)
                    .startDate(LocalDate.of(2024, 1, 1))
                    .status(ContractStatus.PAUSED)
                    .config(Map.of())
                    .createdAt(ZonedDateTime.now())
                    .updatedAt(ZonedDateTime.now())
                    .build();

            var request = new CreateTimeEntryRequest(
                    contractId, "Tentativa", new BigDecimal("2"), LocalDate.now());

            when(contractRepository.findById(contractId)).thenReturn(Optional.of(pausedContract));

            // Act + Assert
            assertThatThrownBy(() -> timeEntryService.createTimeEntry(userId, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("PAUSED");

            verify(timeEntryRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção para contrato encerrado")
        void shouldThrowForTerminatedContract() {
            // Arrange
            Contract terminated = Contract.builder()
                    .id(contractId)
                    .organizationId(UUID.randomUUID())
                    .clientUserId(clientUserId)
                    .title("Encerrado")
                    .type(ContractType.RETAINER)
                    .billingDay(15)
                    .startDate(LocalDate.of(2024, 1, 1))
                    .status(ContractStatus.TERMINATED)
                    .config(Map.of())
                    .createdAt(ZonedDateTime.now())
                    .updatedAt(ZonedDateTime.now())
                    .build();

            var request = new CreateTimeEntryRequest(
                    contractId, "Tentativa", new BigDecimal("2"), LocalDate.now());

            when(contractRepository.findById(contractId)).thenReturn(Optional.of(terminated));

            // Act + Assert
            assertThatThrownBy(() -> timeEntryService.createTimeEntry(userId, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("TERMINATED");

            verify(timeEntryRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando contrato não existe")
        void shouldThrowWhenContractNotFound() {
            // Arrange
            var request = new CreateTimeEntryRequest(
                    contractId, "Dev", new BigDecimal("3"), LocalDate.now());

            when(contractRepository.findById(contractId)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> timeEntryService.createTimeEntry(userId, request))
                    .isInstanceOf(ContractNotFoundException.class);

            verify(timeEntryRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando userId é null — fail fast")
        void shouldThrowWhenUserIdIsNull() {
            // Arrange
            var request = new CreateTimeEntryRequest(
                    contractId, "Dev", new BigDecimal("3"), LocalDate.now());

            // Act + Assert
            assertThatThrownBy(() -> timeEntryService.createTimeEntry(null, request))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("User ID must not be null");

            verify(timeEntryRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando request é null — fail fast")
        void shouldThrowWhenRequestIsNull() {
            // Act + Assert
            assertThatThrownBy(() -> timeEntryService.createTimeEntry(userId, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("CreateTimeEntryRequest must not be null");

            verify(timeEntryRepository, never()).save(any());
        }
    }

    // --- Submissão ---

    @Nested
    @DisplayName("submitTimeEntry")
    class SubmitTimeEntry {

        @Test
        @DisplayName("deve submeter lançamento DRAFT com sucesso")
        void shouldSubmitDraftEntry() {
            // Arrange
            TimeEntry draftEntry = buildDraftEntry();

            when(timeEntryRepository.findById(timeEntryId)).thenReturn(Optional.of(draftEntry));
            when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            TimeEntryResponse response = timeEntryService.submitTimeEntry(userId, timeEntryId);

            // Assert
            assertThat(response.status()).isEqualTo(TimeEntryStatus.SUBMITTED);
            verify(timeEntryRepository).save(any(TimeEntry.class));
        }

        @Test
        @DisplayName("deve rejeitar submissão por usuário não-dono")
        void shouldRejectSubmitByNonOwner() {
            // Arrange
            UUID nonOwnerId = UUID.randomUUID();
            TimeEntry draftEntry = buildDraftEntry();

            when(timeEntryRepository.findById(timeEntryId)).thenReturn(Optional.of(draftEntry));

            // Act + Assert
            assertThatThrownBy(() -> timeEntryService.submitTimeEntry(nonOwnerId, timeEntryId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not the owner");

            verify(timeEntryRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção ao submeter lançamento já aprovado")
        void shouldThrowWhenSubmittingApprovedEntry() {
            // Arrange
            TimeEntry approvedEntry = TimeEntry.builder()
                    .id(timeEntryId)
                    .contractId(contractId)
                    .userId(userId)
                    .description("Dev")
                    .hours(new BigDecimal("4.0"))
                    .entryDate(LocalDate.now())
                    .status(TimeEntryStatus.APPROVED)
                    .createdAt(ZonedDateTime.now())
                    .updatedAt(ZonedDateTime.now())
                    .build();

            when(timeEntryRepository.findById(timeEntryId)).thenReturn(Optional.of(approvedEntry));

            // Act + Assert
            assertThatThrownBy(() -> timeEntryService.submitTimeEntry(userId, timeEntryId))
                    .isInstanceOf(IllegalStateException.class);

            verify(timeEntryRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando lançamento não encontrado")
        void shouldThrowWhenNotFound() {
            // Arrange
            when(timeEntryRepository.findById(timeEntryId)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> timeEntryService.submitTimeEntry(userId, timeEntryId))
                    .isInstanceOf(TimeEntryNotFoundException.class);

            verify(timeEntryRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando userId é null — fail fast")
        void shouldThrowWhenUserIdIsNull() {
            // Act + Assert
            assertThatThrownBy(() -> timeEntryService.submitTimeEntry(null, timeEntryId))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("User ID must not be null");
        }

        @Test
        @DisplayName("deve lançar exceção quando timeEntryId é null — fail fast")
        void shouldThrowWhenTimeEntryIdIsNull() {
            // Act + Assert
            assertThatThrownBy(() -> timeEntryService.submitTimeEntry(userId, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Time entry ID must not be null");
        }
    }

    // --- Listagem ---

    @Nested
    @DisplayName("findByContract")
    class FindByContract {

        @Test
        @DisplayName("deve retornar lançamentos do contrato")
        void shouldReturnContractEntries() {
            // Arrange
            TimeEntry entry = buildDraftEntry();
            when(timeEntryRepository.findByContractIdOrderByEntryDateDesc(contractId))
                    .thenReturn(List.of(entry));

            // Act
            List<TimeEntryResponse> responses = timeEntryService.findByContract(contractId);

            // Assert
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).contractId()).isEqualTo(contractId);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há lançamentos")
        void shouldReturnEmptyList() {
            // Arrange
            when(timeEntryRepository.findByContractIdOrderByEntryDateDesc(contractId))
                    .thenReturn(List.of());

            // Act
            List<TimeEntryResponse> responses = timeEntryService.findByContract(contractId);

            // Assert
            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("deve lançar exceção quando contractId é null")
        void shouldThrowWhenContractIdIsNull() {
            // Act + Assert
            assertThatThrownBy(() -> timeEntryService.findByContract(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Contract ID must not be null");
        }
    }

    @Nested
    @DisplayName("findPendingByContract")
    class FindPendingByContract {

        @Test
        @DisplayName("deve retornar apenas lançamentos PENDING_APPROVAL")
        void shouldReturnPendingApprovalOnly() {
            // Arrange
            TimeEntry entry = buildPendingApprovalEntry();
            when(timeEntryRepository.findPendingByContractId(contractId))
                    .thenReturn(List.of(entry));

            // Act
            List<TimeEntryResponse> responses = timeEntryService.findPendingByContract(contractId);

            // Assert
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).status()).isEqualTo(TimeEntryStatus.PENDING_APPROVAL);
        }
    }

    @Nested
    @DisplayName("findByProvider")
    class FindByProvider {

        @Test
        @DisplayName("deve retornar lançamentos do provider")
        void shouldReturnProviderEntries() {
            // Arrange
            TimeEntry entry = buildDraftEntry();
            when(timeEntryRepository.findByUserIdOrderByEntryDateDesc(userId))
                    .thenReturn(List.of(entry));

            // Act
            List<TimeEntryResponse> responses = timeEntryService.findByProvider(userId);

            // Assert
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).userId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("deve lançar exceção quando userId é null")
        void shouldThrowWhenUserIdIsNull() {
            // Act + Assert
            assertThatThrownBy(() -> timeEntryService.findByProvider(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("User ID must not be null");
        }
    }

    // --- Aprovação ---

    @Nested
    @DisplayName("approveTimeEntry")
    class ApproveTimeEntry {

        @Test
        @DisplayName("deve aprovar lançamento PENDING_APPROVAL pelo client")
        void shouldApproveByClient() {
            // Arrange
            TimeEntry pendingEntry = buildPendingApprovalEntry();

            when(timeEntryRepository.findById(timeEntryId)).thenReturn(Optional.of(pendingEntry));
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(activeContract));
            when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            TimeEntryResponse response = timeEntryService.approveTimeEntry(clientUserId, timeEntryId);

            // Assert
            assertThat(response.status()).isEqualTo(TimeEntryStatus.APPROVED);
            assertThat(response.reviewerId()).isEqualTo(clientUserId);
            assertThat(response.reviewedAt()).isNotNull();
            verify(timeEntryRepository).save(any(TimeEntry.class));
        }

        @Test
        @DisplayName("deve aprovar lançamento SUBMITTED diretamente pelo client")
        void shouldApproveSubmittedByClient() {
            // Arrange
            TimeEntry submittedEntry = buildSubmittedEntry();

            when(timeEntryRepository.findById(timeEntryId)).thenReturn(Optional.of(submittedEntry));
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(activeContract));
            when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            TimeEntryResponse response = timeEntryService.approveTimeEntry(clientUserId, timeEntryId);

            // Assert
            assertThat(response.status()).isEqualTo(TimeEntryStatus.APPROVED);
            assertThat(response.reviewerId()).isEqualTo(clientUserId);
            verify(timeEntryRepository).save(any(TimeEntry.class));
        }

        @Test
        @DisplayName("deve rejeitar aprovação por usuário não-client")
        void shouldRejectApprovalByNonClient() {
            // Arrange
            UUID nonClientId = UUID.randomUUID();
            TimeEntry pendingEntry = buildPendingApprovalEntry();

            when(timeEntryRepository.findById(timeEntryId)).thenReturn(Optional.of(pendingEntry));
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(activeContract));

            // Act + Assert
            assertThatThrownBy(() -> timeEntryService.approveTimeEntry(nonClientId, timeEntryId))
                    .isInstanceOf(UnauthorizedReviewException.class);

            verify(timeEntryRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção ao aprovar lançamento em DRAFT")
        void shouldThrowWhenApprovingDraft() {
            // Arrange
            TimeEntry draftEntry = buildDraftEntry();

            when(timeEntryRepository.findById(timeEntryId)).thenReturn(Optional.of(draftEntry));
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(activeContract));

            // Act + Assert
            assertThatThrownBy(() -> timeEntryService.approveTimeEntry(clientUserId, timeEntryId))
                    .isInstanceOf(IllegalStateException.class);

            verify(timeEntryRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção para lançamento não encontrado")
        void shouldThrowForNotFound() {
            // Arrange
            when(timeEntryRepository.findById(timeEntryId)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> timeEntryService.approveTimeEntry(clientUserId, timeEntryId))
                    .isInstanceOf(TimeEntryNotFoundException.class);

            verify(timeEntryRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando reviewerId é null")
        void shouldThrowWhenReviewerIdIsNull() {
            // Act + Assert
            assertThatThrownBy(() -> timeEntryService.approveTimeEntry(null, timeEntryId))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Reviewer ID must not be null");
        }

        @Test
        @DisplayName("deve lançar exceção quando timeEntryId é null")
        void shouldThrowWhenTimeEntryIdIsNull() {
            // Act + Assert
            assertThatThrownBy(() -> timeEntryService.approveTimeEntry(clientUserId, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Time entry ID must not be null");
        }
    }

    // --- Disputa ---

    @Nested
    @DisplayName("disputeTimeEntry")
    class DisputeTimeEntry {

        @Test
        @DisplayName("deve disputar lançamento PENDING_APPROVAL pelo client")
        void shouldDisputeByClient() {
            // Arrange
            TimeEntry pendingEntry = buildPendingApprovalEntry();
            var request = new ReviewTimeEntryRequest("Horas não condizem com o escopo");

            when(timeEntryRepository.findById(timeEntryId)).thenReturn(Optional.of(pendingEntry));
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(activeContract));
            when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            TimeEntryResponse response = timeEntryService.disputeTimeEntry(clientUserId, timeEntryId, request);

            // Assert
            assertThat(response.status()).isEqualTo(TimeEntryStatus.DISPUTED);
            assertThat(response.disputeReason()).isEqualTo("Horas não condizem com o escopo");
            assertThat(response.reviewerId()).isEqualTo(clientUserId);
            verify(timeEntryRepository).save(any(TimeEntry.class));
        }

        @Test
        @DisplayName("deve disputar lançamento SUBMITTED diretamente pelo client")
        void shouldDisputeSubmittedByClient() {
            // Arrange
            TimeEntry submittedEntry = buildSubmittedEntry();
            var request = new ReviewTimeEntryRequest("Descrição não corresponde");

            when(timeEntryRepository.findById(timeEntryId)).thenReturn(Optional.of(submittedEntry));
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(activeContract));
            when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            TimeEntryResponse response = timeEntryService.disputeTimeEntry(clientUserId, timeEntryId, request);

            // Assert
            assertThat(response.status()).isEqualTo(TimeEntryStatus.DISPUTED);
            assertThat(response.disputeReason()).isEqualTo("Descrição não corresponde");
            assertThat(response.reviewerId()).isEqualTo(clientUserId);
            verify(timeEntryRepository).save(any(TimeEntry.class));
        }

        @Test
        @DisplayName("deve rejeitar disputa por usuário não-client")
        void shouldRejectDisputeByNonClient() {
            // Arrange
            UUID nonClientId = UUID.randomUUID();
            TimeEntry pendingEntry = buildPendingApprovalEntry();
            var request = new ReviewTimeEntryRequest("Motivo");

            when(timeEntryRepository.findById(timeEntryId)).thenReturn(Optional.of(pendingEntry));
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(activeContract));

            // Act + Assert
            assertThatThrownBy(() -> timeEntryService.disputeTimeEntry(nonClientId, timeEntryId, request))
                    .isInstanceOf(UnauthorizedReviewException.class);

            verify(timeEntryRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção ao disputar lançamento em DRAFT")
        void shouldThrowWhenDisputingDraft() {
            // Arrange
            TimeEntry draftEntry = buildDraftEntry();
            var request = new ReviewTimeEntryRequest("Motivo");

            when(timeEntryRepository.findById(timeEntryId)).thenReturn(Optional.of(draftEntry));
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(activeContract));

            // Act + Assert
            assertThatThrownBy(() -> timeEntryService.disputeTimeEntry(clientUserId, timeEntryId, request))
                    .isInstanceOf(IllegalStateException.class);

            verify(timeEntryRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando request é null")
        void shouldThrowWhenRequestIsNull() {
            // Act + Assert
            assertThatThrownBy(() -> timeEntryService.disputeTimeEntry(clientUserId, timeEntryId, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("ReviewTimeEntryRequest must not be null");
        }
    }

    // --- Exclusão ---

    @Nested
    @DisplayName("deleteTimeEntry")
    class DeleteTimeEntry {

        @Test
        @DisplayName("deve remover lançamento em DRAFT")
        void shouldDeleteDraftEntry() {
            // Arrange
            TimeEntry draftEntry = buildDraftEntry();
            when(timeEntryRepository.findById(timeEntryId)).thenReturn(Optional.of(draftEntry));

            // Act
            timeEntryService.deleteTimeEntry(userId, timeEntryId);

            // Assert
            verify(timeEntryRepository).delete(draftEntry);
        }

        @Test
        @DisplayName("deve rejeitar exclusão por usuário não-proprietário")
        void shouldRejectDeleteByNonOwner() {
            // Arrange
            UUID otherUserId = UUID.randomUUID();
            TimeEntry draftEntry = buildDraftEntry();
            when(timeEntryRepository.findById(timeEntryId)).thenReturn(Optional.of(draftEntry));

            // Act + Assert
            assertThatThrownBy(() -> timeEntryService.deleteTimeEntry(otherUserId, timeEntryId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("is not the owner");

            verify(timeEntryRepository, never()).delete(any());
        }

        @Test
        @DisplayName("deve rejeitar exclusão de lançamento já submetido")
        void shouldRejectDeleteOfSubmittedEntry() {
            // Arrange
            TimeEntry submittedEntry = TimeEntry.builder()
                    .id(timeEntryId)
                    .contractId(contractId)
                    .userId(userId)
                    .description("Desenvolvimento feature X")
                    .hours(new BigDecimal("4.5"))
                    .entryDate(LocalDate.now())
                    .status(TimeEntryStatus.SUBMITTED)
                    .createdAt(ZonedDateTime.now())
                    .updatedAt(ZonedDateTime.now())
                    .build();
            when(timeEntryRepository.findById(timeEntryId)).thenReturn(Optional.of(submittedEntry));

            // Act + Assert
            assertThatThrownBy(() -> timeEntryService.deleteTimeEntry(userId, timeEntryId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only DRAFT entries can be deleted");

            verify(timeEntryRepository, never()).delete(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando lançamento não existe")
        void shouldThrowWhenTimeEntryNotFound() {
            // Arrange
            when(timeEntryRepository.findById(timeEntryId)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> timeEntryService.deleteTimeEntry(userId, timeEntryId))
                    .isInstanceOf(TimeEntryNotFoundException.class);

            verify(timeEntryRepository, never()).delete(any());
        }
    }
}
