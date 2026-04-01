package com.contractpulse.timeentry;

import com.contractpulse.contract.ContractRepository;
import com.contractpulse.contract.exception.ContractNotFoundException;
import com.contractpulse.contract.model.Contract;
import com.contractpulse.contract.model.ContractStatus;
import com.contractpulse.timeentry.dto.CreateTimeEntryRequest;
import com.contractpulse.timeentry.dto.ReviewTimeEntryRequest;
import com.contractpulse.timeentry.dto.TimeEntryResponse;
import com.contractpulse.timeentry.exception.TimeEntryNotFoundException;
import com.contractpulse.timeentry.exception.UnauthorizedReviewException;
import com.contractpulse.timeentry.model.TimeEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Serviço com regras de negócio de lançamentos de horas.
 * <p>
 * Ciclo de vida completo:
 * DRAFT → SUBMITTED → PENDING_APPROVAL → APPROVED | DISPUTED → INVOICED
 * </p>
 */
@Service
public class TimeEntryService {

    private static final Logger log = LoggerFactory.getLogger(TimeEntryService.class);

    private final TimeEntryRepository timeEntryRepository;
    private final ContractRepository contractRepository;

    public TimeEntryService(TimeEntryRepository timeEntryRepository,
                            ContractRepository contractRepository) {
        this.timeEntryRepository = timeEntryRepository;
        this.contractRepository = contractRepository;
    }

    /**
     * Cria um lançamento de horas como DRAFT para um contrato ativo.
     *
     * @param userId  ID do provider que está lançando
     * @param request dados do lançamento
     * @return resposta com dados do lançamento criado (status DRAFT)
     */
    @Transactional
    public TimeEntryResponse createTimeEntry(UUID userId, CreateTimeEntryRequest request) {
        Objects.requireNonNull(userId, "User ID must not be null");
        Objects.requireNonNull(request, "CreateTimeEntryRequest must not be null");

        Contract contract = findContractOrThrow(request.contractId());
        validateContractIsActive(contract);

        TimeEntry entry = TimeEntry.builder()
                .contractId(request.contractId())
                .userId(userId)
                .description(request.description())
                .hours(request.hours())
                .entryDate(request.entryDate())
                .build();

        TimeEntry saved = timeEntryRepository.save(entry);
        log.info("Time entry created (DRAFT): id={}, contract={}, hours={}",
                saved.getId(), saved.getContractId(), saved.getHours());

        return TimeEntryResponse.fromEntity(saved);
    }

    /**
     * Submete um lançamento de horas (DRAFT → SUBMITTED).
     * Apenas o provider dono do lançamento pode submeter.
     *
     * @param userId      ID do provider que está submetendo
     * @param timeEntryId ID do lançamento
     * @return resposta com dados atualizados (status SUBMITTED)
     */
    @Transactional
    public TimeEntryResponse submitTimeEntry(UUID userId, UUID timeEntryId) {
        Objects.requireNonNull(userId, "User ID must not be null");
        Objects.requireNonNull(timeEntryId, "Time entry ID must not be null");

        TimeEntry entry = findTimeEntryOrThrow(timeEntryId);
        validateProviderAccess(userId, entry);

        entry.submit();
        TimeEntry saved = timeEntryRepository.save(entry);

        log.info("Time entry submitted: id={}, user={}", saved.getId(), userId);
        return TimeEntryResponse.fromEntity(saved);
    }

    /**
     * Lista todos os lançamentos de um contrato.
     *
     * @param contractId ID do contrato
     * @return lista de lançamentos ordenados por data desc
     */
    @Transactional(readOnly = true)
    public List<TimeEntryResponse> findByContract(UUID contractId) {
        Objects.requireNonNull(contractId, "Contract ID must not be null");
        return timeEntryRepository.findByContractIdOrderByEntryDateDesc(contractId).stream()
                .map(TimeEntryResponse::fromEntity)
                .toList();
    }

    /**
     * Lista lançamentos pendentes de aprovação de um contrato.
     *
     * @param contractId ID do contrato
     * @return lista de lançamentos com status PENDING_APPROVAL
     */
    @Transactional(readOnly = true)
    public List<TimeEntryResponse> findPendingByContract(UUID contractId) {
        Objects.requireNonNull(contractId, "Contract ID must not be null");
        return timeEntryRepository.findPendingByContractId(contractId).stream()
                .map(TimeEntryResponse::fromEntity)
                .toList();
    }

    /**
     * Lista todos os lançamentos do provider autenticado.
     *
     * @param userId ID do provider
     * @return lista de lançamentos do provider ordenados por data desc
     */
    @Transactional(readOnly = true)
    public List<TimeEntryResponse> findByProvider(UUID userId) {
        Objects.requireNonNull(userId, "User ID must not be null");
        return timeEntryRepository.findByUserIdOrderByEntryDateDesc(userId).stream()
                .map(TimeEntryResponse::fromEntity)
                .toList();
    }

    /**
     * Aprova um lançamento de horas (PENDING_APPROVAL → APPROVED).
     * Apenas o client do contrato pode aprovar.
     *
     * @param reviewerId  ID do cliente que está aprovando
     * @param timeEntryId ID do lançamento
     * @return resposta com dados atualizados
     */
    @Transactional
    public TimeEntryResponse approveTimeEntry(UUID reviewerId, UUID timeEntryId) {
        Objects.requireNonNull(reviewerId, "Reviewer ID must not be null");
        Objects.requireNonNull(timeEntryId, "Time entry ID must not be null");

        TimeEntry entry = findTimeEntryOrThrow(timeEntryId);
        Contract contract = findContractOrThrow(entry.getContractId());
        validateClientAccess(reviewerId, contract);

        entry.approve(reviewerId);
        TimeEntry saved = timeEntryRepository.save(entry);

        log.info("Time entry approved: id={}, reviewer={}", saved.getId(), reviewerId);
        return TimeEntryResponse.fromEntity(saved);
    }

    /**
     * Disputa um lançamento de horas (PENDING_APPROVAL → DISPUTED).
     * Apenas o client do contrato pode disputar.
     *
     * @param reviewerId  ID do cliente que está disputando
     * @param timeEntryId ID do lançamento
     * @param request     motivo da disputa
     * @return resposta com dados atualizados
     */
    @Transactional
    public TimeEntryResponse disputeTimeEntry(UUID reviewerId, UUID timeEntryId,
                                               ReviewTimeEntryRequest request) {
        Objects.requireNonNull(reviewerId, "Reviewer ID must not be null");
        Objects.requireNonNull(timeEntryId, "Time entry ID must not be null");
        Objects.requireNonNull(request, "ReviewTimeEntryRequest must not be null");

        TimeEntry entry = findTimeEntryOrThrow(timeEntryId);
        Contract contract = findContractOrThrow(entry.getContractId());
        validateClientAccess(reviewerId, contract);

        entry.dispute(reviewerId, request.disputeReason());
        TimeEntry saved = timeEntryRepository.save(entry);

        log.info("Time entry disputed: id={}, reviewer={}", saved.getId(), reviewerId);
        return TimeEntryResponse.fromEntity(saved);
    }

    /**
     * Retorna o total de horas aprovadas de um contrato no período.
     */
    @Transactional(readOnly = true)
    public BigDecimal sumApprovedHoursForPeriod(UUID contractId, LocalDate startDate, LocalDate endDate) {
        return timeEntryRepository.sumApprovedHoursByContractAndPeriod(contractId, startDate, endDate);
    }

    /**
     * Retorna o total de horas aprovadas de um contrato (all-time).
     */
    @Transactional(readOnly = true)
    public BigDecimal sumApprovedHoursTotal(UUID contractId) {
        return timeEntryRepository.sumApprovedHoursByContract(contractId);
    }

    /**
     * Remove um lançamento de horas em DRAFT.
     * Apenas o provider dono pode remover, e somente enquanto estiver em DRAFT.
     *
     * @param userId      ID do provider autenticado
     * @param timeEntryId ID do lançamento a remover
     */
    @Transactional
    public void deleteTimeEntry(UUID userId, UUID timeEntryId) {
        Objects.requireNonNull(userId, "User ID must not be null");
        Objects.requireNonNull(timeEntryId, "Time entry ID must not be null");

        TimeEntry entry = findTimeEntryOrThrow(timeEntryId);
        validateProviderAccess(userId, entry);

        if (!entry.isEditable()) {
            throw new IllegalStateException(
                    "Only DRAFT entries can be deleted. Current status: " + entry.getStatus());
        }

        timeEntryRepository.delete(entry);
        log.info("Time entry deleted: id={}, user={}", timeEntryId, userId);
    }

    // --- Métodos internos ---

    private TimeEntry findTimeEntryOrThrow(UUID timeEntryId) {
        return timeEntryRepository.findById(timeEntryId)
                .orElseThrow(() -> new TimeEntryNotFoundException(timeEntryId));
    }

    private Contract findContractOrThrow(UUID contractId) {
        return contractRepository.findById(contractId)
                .orElseThrow(() -> new ContractNotFoundException(contractId));
    }

    private void validateContractIsActive(Contract contract) {
        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Cannot create time entry for contract with status: " + contract.getStatus());
        }
    }

    private void validateProviderAccess(UUID userId, TimeEntry entry) {
        if (!entry.getUserId().equals(userId)) {
            throw new IllegalStateException(
                    "User " + userId + " is not the owner of time entry " + entry.getId());
        }
    }

    private void validateClientAccess(UUID userId, Contract contract) {
        if (!contract.getClientUserId().equals(userId)) {
            throw new UnauthorizedReviewException(userId, contract.getId());
        }
    }
}
