package com.contractpulse.timeentry;

import com.contractpulse.contract.ContractRepository;
import com.contractpulse.contract.exception.ContractNotFoundException;
import com.contractpulse.contract.model.Contract;
import com.contractpulse.contract.model.ContractCurrency;
import com.contractpulse.contract.model.ContractStatus;
import com.contractpulse.contract.model.ContractType;
import com.contractpulse.timeentry.dto.TimeEntryResponse;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para o ApprovalService.
 * Cobertura mínima alvo: 80%.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ApprovalService")
class ApprovalServiceTest {

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private ContractRepository contractRepository;

    @InjectMocks
    private ApprovalService approvalService;

    private UUID clientUserId;
    private UUID contractId;
    private UUID systemUserId;
    private Contract activeContract;

    @BeforeEach
    void setUp() {
        clientUserId = UUID.randomUUID();
        contractId = UUID.randomUUID();
        systemUserId = UUID.fromString("00000000-0000-0000-0000-000000000000");

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
     * Helper: constrói um lançamento em PENDING_APPROVAL.
     */
    private TimeEntry buildPendingEntry() {
        return TimeEntry.builder()
                .id(UUID.randomUUID())
                .contractId(contractId)
                .userId(UUID.randomUUID())
                .description("Desenvolvimento feature X")
                .hours(new BigDecimal("4.5"))
                .entryDate(LocalDate.now())
                .status(TimeEntryStatus.PENDING_APPROVAL)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now().minusHours(72))
                .build();
    }

    /**
     * Helper: constrói um lançamento em SUBMITTED.
     */
    private TimeEntry buildSubmittedEntry() {
        return TimeEntry.builder()
                .id(UUID.randomUUID())
                .contractId(contractId)
                .userId(UUID.randomUUID())
                .description("Code review PR #42")
                .hours(new BigDecimal("2.0"))
                .entryDate(LocalDate.now())
                .status(TimeEntryStatus.SUBMITTED)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("batchApprove")
    class BatchApprove {

        @Test
        @DisplayName("deve aprovar todos os lançamentos pendentes de um contrato")
        void shouldApproveAllPendingEntries() {
            // Arrange
            List<TimeEntry> pending = List.of(buildPendingEntry(), buildPendingEntry());

            when(contractRepository.findById(contractId)).thenReturn(Optional.of(activeContract));
            when(timeEntryRepository.findPendingByContractId(contractId)).thenReturn(pending);
            when(timeEntryRepository.saveAll(anyList())).thenReturn(pending);

            // Act
            List<TimeEntryResponse> result = approvalService.batchApprove(clientUserId, contractId);

            // Assert
            assertThat(result).hasSize(2);
            verify(timeEntryRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("deve aprovar em lote inclui SUBMITTED e PENDING_APPROVAL")
        void shouldBatchApproveMixedStatuses() {
            // Arrange
            List<TimeEntry> mixed = List.of(buildSubmittedEntry(), buildPendingEntry());

            when(contractRepository.findById(contractId)).thenReturn(Optional.of(activeContract));
            when(timeEntryRepository.findPendingByContractId(contractId)).thenReturn(mixed);
            when(timeEntryRepository.saveAll(anyList())).thenReturn(mixed);

            // Act
            List<TimeEntryResponse> result = approvalService.batchApprove(clientUserId, contractId);

            // Assert
            assertThat(result).hasSize(2);
            verify(timeEntryRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há lançamentos pendentes")
        void shouldReturnEmptyWhenNoPendingEntries() {
            // Arrange
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(activeContract));
            when(timeEntryRepository.findPendingByContractId(contractId)).thenReturn(List.of());

            // Act
            List<TimeEntryResponse> result = approvalService.batchApprove(clientUserId, contractId);

            // Assert
            assertThat(result).isEmpty();
            verify(timeEntryRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("deve rejeitar aprovação em lote por não-client")
        void shouldRejectBatchApproveByNonClient() {
            // Arrange
            UUID otherUserId = UUID.randomUUID();
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(activeContract));

            // Act + Assert
            assertThatThrownBy(() -> approvalService.batchApprove(otherUserId, contractId))
                    .isInstanceOf(UnauthorizedReviewException.class);
        }

        @Test
        @DisplayName("deve lançar exceção para contrato não encontrado")
        void shouldThrowForContractNotFound() {
            // Arrange
            when(contractRepository.findById(contractId)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> approvalService.batchApprove(clientUserId, contractId))
                    .isInstanceOf(ContractNotFoundException.class);
        }

        @Test
        @DisplayName("deve rejeitar reviewerId nulo")
        void shouldRejectNullReviewerId() {
            // Act + Assert
            assertThatThrownBy(() -> approvalService.batchApprove(null, contractId))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("deve rejeitar contractId nulo")
        void shouldRejectNullContractId() {
            // Act + Assert
            assertThatThrownBy(() -> approvalService.batchApprove(clientUserId, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("promoteSubmittedToPendingApproval")
    class PromoteSubmitted {

        @Test
        @DisplayName("deve promover lançamentos SUBMITTED para PENDING_APPROVAL")
        void shouldPromoteSubmittedEntries() {
            // Arrange
            List<TimeEntry> submitted = List.of(buildSubmittedEntry(), buildSubmittedEntry());

            when(timeEntryRepository.findByStatus(TimeEntryStatus.SUBMITTED)).thenReturn(submitted);
            when(timeEntryRepository.saveAll(anyList())).thenReturn(submitted);

            // Act
            int count = approvalService.promoteSubmittedToPendingApproval();

            // Assert
            assertThat(count).isEqualTo(2);
            verify(timeEntryRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("deve retornar zero quando não há SUBMITTED")
        void shouldReturnZeroWhenNoSubmitted() {
            // Arrange
            when(timeEntryRepository.findByStatus(TimeEntryStatus.SUBMITTED)).thenReturn(List.of());

            // Act
            int count = approvalService.promoteSubmittedToPendingApproval();

            // Assert
            assertThat(count).isZero();
            verify(timeEntryRepository, never()).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("autoApproveExpiredEntries")
    class AutoApprove {

        @Test
        @DisplayName("deve auto-aprovar lançamentos pendentes há mais de 48h")
        void shouldAutoApproveExpiredEntries() {
            // Arrange
            List<TimeEntry> expired = List.of(buildPendingEntry());

            when(timeEntryRepository.findPendingApprovalOlderThan(any(ZonedDateTime.class)))
                    .thenReturn(expired);
            when(timeEntryRepository.saveAll(anyList())).thenReturn(expired);

            // Act
            int count = approvalService.autoApproveExpiredEntries(systemUserId);

            // Assert
            assertThat(count).isEqualTo(1);
            verify(timeEntryRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("deve retornar zero quando não há expirados")
        void shouldReturnZeroWhenNoExpired() {
            // Arrange
            when(timeEntryRepository.findPendingApprovalOlderThan(any(ZonedDateTime.class)))
                    .thenReturn(List.of());

            // Act
            int count = approvalService.autoApproveExpiredEntries(systemUserId);

            // Assert
            assertThat(count).isZero();
            verify(timeEntryRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("deve rejeitar systemUserId nulo")
        void shouldRejectNullSystemUserId() {
            // Act + Assert
            assertThatThrownBy(() -> approvalService.autoApproveExpiredEntries(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
