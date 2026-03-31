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

    private TimeEntry buildPendingEntry() {
        return TimeEntry.builder()
                .id(timeEntryId)
                .contractId(contractId)
                .userId(userId)
                .description("Desenvolvimento feature X")
                .hours(new BigDecimal("4.5"))
                .entryDate(LocalDate.now())
                .status(TimeEntryStatus.PENDING)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();
    }

    // --- Criação ---

    @Nested
    @DisplayName("createTimeEntry")
    class CreateTimeEntry {

        @Test
        @DisplayName("deve criar lançamento com sucesso para contrato ativo")
        void shouldCreateTimeEntryForActiveContract() {
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
                        .status(TimeEntryStatus.PENDING)
                        .createdAt(ZonedDateTime.now())
                        .updatedAt(ZonedDateTime.now())
                        .build();
            });

            TimeEntryResponse response = timeEntryService.createTimeEntry(userId, request);

            assertThat(response.id()).isEqualTo(timeEntryId);
            assertThat(response.contractId()).isEqualTo(contractId);
            assertThat(response.userId()).isEqualTo(userId);
            assertThat(response.hours()).isEqualByComparingTo(new BigDecimal("4.5"));
            assertThat(response.status()).isEqualTo(TimeEntryStatus.PENDING);
            verify(timeEntryRepository).save(any(TimeEntry.class));
        }

        @Test
        @DisplayName("deve lançar exceção para contrato pausado")
        void shouldThrowForPausedContract() {
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

            assertThatThrownBy(() -> timeEntryService.createTimeEntry(userId, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("PAUSED");

            verify(timeEntryRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção para contrato encerrado")
        void shouldThrowForTerminatedContract() {
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

            assertThatThrownBy(() -> timeEntryService.createTimeEntry(userId, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("TERMINATED");

            verify(timeEntryRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando contrato não existe")
        void shouldThrowWhenContractNotFound() {
            var request = new CreateTimeEntryRequest(
                    contractId, "Dev", new BigDecimal("3"), LocalDate.now());

            when(contractRepository.findById(contractId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> timeEntryService.createTimeEntry(userId, request))
                    .isInstanceOf(ContractNotFoundException.class);

            verify(timeEntryRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando userId é null — fail fast")
        void shouldThrowWhenUserIdIsNull() {
            var request = new CreateTimeEntryRequest(
                    contractId, "Dev", new BigDecimal("3"), LocalDate.now());

            assertThatThrownBy(() -> timeEntryService.createTimeEntry(null, request))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("User ID must not be null");

            verify(timeEntryRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando request é null — fail fast")
        void shouldThrowWhenRequestIsNull() {
            assertThatThrownBy(() -> timeEntryService.createTimeEntry(userId, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("CreateTimeEntryRequest must not be null");

            verify(timeEntryRepository, never()).save(any());
        }
    }

    // --- Listagem ---

    @Nested
    @DisplayName("findByContract")
    class FindByContract {

        @Test
        @DisplayName("deve retornar lançamentos do contrato")
        void shouldReturnContractEntries() {
            TimeEntry entry = buildPendingEntry();
            when(timeEntryRepository.findByContractIdOrderByEntryDateDesc(contractId))
                    .thenReturn(List.of(entry));

            List<TimeEntryResponse> responses = timeEntryService.findByContract(contractId);

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).contractId()).isEqualTo(contractId);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há lançamentos")
        void shouldReturnEmptyList() {
            when(timeEntryRepository.findByContractIdOrderByEntryDateDesc(contractId))
                    .thenReturn(List.of());

            List<TimeEntryResponse> responses = timeEntryService.findByContract(contractId);

            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("deve lançar exceção quando contractId é null")
        void shouldThrowWhenContractIdIsNull() {
            assertThatThrownBy(() -> timeEntryService.findByContract(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Contract ID must not be null");
        }
    }

    @Nested
    @DisplayName("findPendingByContract")
    class FindPendingByContract {

        @Test
        @DisplayName("deve retornar apenas lançamentos pendentes")
        void shouldReturnPendingOnly() {
            TimeEntry entry = buildPendingEntry();
            when(timeEntryRepository.findPendingByContractId(contractId))
                    .thenReturn(List.of(entry));

            List<TimeEntryResponse> responses = timeEntryService.findPendingByContract(contractId);

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).status()).isEqualTo(TimeEntryStatus.PENDING);
        }
    }

    // --- Aprovação ---

    @Nested
    @DisplayName("approveTimeEntry")
    class ApproveTimeEntry {

        @Test
        @DisplayName("deve aprovar lançamento pendente pelo client")
        void shouldApproveByClient() {
            TimeEntry pendingEntry = buildPendingEntry();

            when(timeEntryRepository.findById(timeEntryId)).thenReturn(Optional.of(pendingEntry));
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(activeContract));
            when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));

            TimeEntryResponse response = timeEntryService.approveTimeEntry(clientUserId, timeEntryId);

            assertThat(response.status()).isEqualTo(TimeEntryStatus.APPROVED);
            assertThat(response.reviewerId()).isEqualTo(clientUserId);
            assertThat(response.reviewedAt()).isNotNull();
            verify(timeEntryRepository).save(any(TimeEntry.class));
        }

        @Test
        @DisplayName("deve rejeitar aprovação por usuário não-client")
        void shouldRejectApprovalByNonClient() {
            UUID nonClientId = UUID.randomUUID();
            TimeEntry pendingEntry = buildPendingEntry();

            when(timeEntryRepository.findById(timeEntryId)).thenReturn(Optional.of(pendingEntry));
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(activeContract));

            assertThatThrownBy(() -> timeEntryService.approveTimeEntry(nonClientId, timeEntryId))
                    .isInstanceOf(UnauthorizedReviewException.class);

            verify(timeEntryRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção para lançamento não encontrado")
        void shouldThrowForNotFound() {
            when(timeEntryRepository.findById(timeEntryId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> timeEntryService.approveTimeEntry(clientUserId, timeEntryId))
                    .isInstanceOf(TimeEntryNotFoundException.class);

            verify(timeEntryRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando reviewerId é null")
        void shouldThrowWhenReviewerIdIsNull() {
            assertThatThrownBy(() -> timeEntryService.approveTimeEntry(null, timeEntryId))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Reviewer ID must not be null");
        }

        @Test
        @DisplayName("deve lançar exceção quando timeEntryId é null")
        void shouldThrowWhenTimeEntryIdIsNull() {
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
        @DisplayName("deve disputar lançamento pendente pelo client")
        void shouldDisputeByClient() {
            TimeEntry pendingEntry = buildPendingEntry();
            var request = new ReviewTimeEntryRequest("Horas não condizem com o escopo");

            when(timeEntryRepository.findById(timeEntryId)).thenReturn(Optional.of(pendingEntry));
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(activeContract));
            when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));

            TimeEntryResponse response = timeEntryService.disputeTimeEntry(clientUserId, timeEntryId, request);

            assertThat(response.status()).isEqualTo(TimeEntryStatus.DISPUTED);
            assertThat(response.disputeReason()).isEqualTo("Horas não condizem com o escopo");
            assertThat(response.reviewerId()).isEqualTo(clientUserId);
            verify(timeEntryRepository).save(any(TimeEntry.class));
        }

        @Test
        @DisplayName("deve rejeitar disputa por usuário não-client")
        void shouldRejectDisputeByNonClient() {
            UUID nonClientId = UUID.randomUUID();
            TimeEntry pendingEntry = buildPendingEntry();
            var request = new ReviewTimeEntryRequest("Motivo");

            when(timeEntryRepository.findById(timeEntryId)).thenReturn(Optional.of(pendingEntry));
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(activeContract));

            assertThatThrownBy(() -> timeEntryService.disputeTimeEntry(nonClientId, timeEntryId, request))
                    .isInstanceOf(UnauthorizedReviewException.class);

            verify(timeEntryRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando request é null")
        void shouldThrowWhenRequestIsNull() {
            assertThatThrownBy(() -> timeEntryService.disputeTimeEntry(clientUserId, timeEntryId, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("ReviewTimeEntryRequest must not be null");
        }
    }
}
