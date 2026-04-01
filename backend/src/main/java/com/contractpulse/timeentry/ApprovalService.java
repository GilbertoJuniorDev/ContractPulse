package com.contractpulse.timeentry;

import com.contractpulse.contract.ContractRepository;
import com.contractpulse.contract.exception.ContractNotFoundException;
import com.contractpulse.contract.model.Contract;
import com.contractpulse.timeentry.dto.TimeEntryResponse;
import com.contractpulse.timeentry.exception.UnauthorizedReviewException;
import com.contractpulse.timeentry.model.TimeEntry;
import com.contractpulse.timeentry.model.TimeEntryStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Serviço dedicado à lógica de aprovação e disputa de lançamentos.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Aprovação em lote (batch approve) por contrato</li>
 *   <li>Transição SUBMITTED → PENDING_APPROVAL (ciclo semanal)</li>
 *   <li>Auto-aprovação após 48h sem resposta do cliente</li>
 * </ul>
 */
@Service
public class ApprovalService {

    private static final Logger log = LoggerFactory.getLogger(ApprovalService.class);

    /** Horas padrão para auto-aprovação sem resposta do cliente. */
    private static final long AUTO_APPROVE_HOURS = 48;

    private final TimeEntryRepository timeEntryRepository;
    private final ContractRepository contractRepository;

    public ApprovalService(TimeEntryRepository timeEntryRepository,
                           ContractRepository contractRepository) {
        this.timeEntryRepository = timeEntryRepository;
        this.contractRepository = contractRepository;
    }

    /**
     * Aprova em lote todos os lançamentos PENDING_APPROVAL de um contrato.
     * Apenas o client do contrato pode executar esta ação.
     *
     * @param reviewerId ID do cliente autenticado
     * @param contractId ID do contrato
     * @return lista de lançamentos aprovados
     */
    @Transactional
    public List<TimeEntryResponse> batchApprove(UUID reviewerId, UUID contractId) {
        Objects.requireNonNull(reviewerId, "Reviewer ID must not be null");
        Objects.requireNonNull(contractId, "Contract ID must not be null");

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ContractNotFoundException(contractId));
        validateClientAccess(reviewerId, contract);

        List<TimeEntry> pendingEntries = timeEntryRepository.findPendingByContractId(contractId);

        if (pendingEntries.isEmpty()) {
            log.info("No pending entries to batch approve: contract={}", contractId);
            return List.of();
        }

        pendingEntries.forEach(entry -> entry.approve(reviewerId));
        List<TimeEntry> saved = timeEntryRepository.saveAll(pendingEntries);

        log.info("Batch approved {} entries: contract={}, reviewer={}",
                saved.size(), contractId, reviewerId);

        return saved.stream()
                .map(TimeEntryResponse::fromEntity)
                .toList();
    }

    /**
     * Transiciona todos os lançamentos SUBMITTED para PENDING_APPROVAL.
     * Chamado pelo job semanal ({@link com.contractpulse.scheduler.WeeklyApprovalJob})
     * todo domingo às 23:59.
     *
     * @return quantidade de lançamentos transicionados
     */
    @Transactional
    public int promoteSubmittedToPendingApproval() {
        List<TimeEntry> submittedEntries = timeEntryRepository.findByStatus(TimeEntryStatus.SUBMITTED);

        if (submittedEntries.isEmpty()) {
            log.info("No SUBMITTED entries to promote to PENDING_APPROVAL");
            return 0;
        }

        submittedEntries.forEach(TimeEntry::markPendingApproval);
        timeEntryRepository.saveAll(submittedEntries);

        log.info("Promoted {} entries from SUBMITTED to PENDING_APPROVAL", submittedEntries.size());
        return submittedEntries.size();
    }

    /**
     * Auto-aprova lançamentos PENDING_APPROVAL que ultrapassaram 48h sem resposta.
     * Utiliza um UUID do sistema como reviewerId para rastreabilidade.
     *
     * @param systemUserId UUID representando o sistema (auto-approve)
     * @return quantidade de lançamentos auto-aprovados
     */
    @Transactional
    public int autoApproveExpiredEntries(UUID systemUserId) {
        Objects.requireNonNull(systemUserId, "System user ID must not be null");

        ZonedDateTime cutoff = ZonedDateTime.now().minusHours(AUTO_APPROVE_HOURS);

        List<TimeEntry> expiredEntries = timeEntryRepository
                .findPendingApprovalOlderThan(cutoff);

        if (expiredEntries.isEmpty()) {
            log.info("No expired PENDING_APPROVAL entries to auto-approve");
            return 0;
        }

        expiredEntries.forEach(entry -> entry.approve(systemUserId));
        timeEntryRepository.saveAll(expiredEntries);

        log.info("Auto-approved {} expired entries (cutoff={})", expiredEntries.size(), cutoff);
        return expiredEntries.size();
    }

    private void validateClientAccess(UUID userId, Contract contract) {
        if (!contract.getClientUserId().equals(userId)) {
            throw new UnauthorizedReviewException(userId, contract.getId());
        }
    }
}
