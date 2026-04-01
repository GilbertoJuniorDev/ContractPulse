package com.contractpulse.timeentry;

import com.contractpulse.timeentry.model.TimeEntry;
import com.contractpulse.timeentry.model.TimeEntryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repositório JPA para a entidade TimeEntry.
 */
@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {

    /**
     * Lista lançamentos de um contrato ordenados por data (mais recente primeiro).
     */
    List<TimeEntry> findByContractIdOrderByEntryDateDesc(UUID contractId);

    /**
     * Lista lançamentos de um contrato filtrados por status.
     */
    List<TimeEntry> findByContractIdAndStatusOrderByEntryDateDesc(UUID contractId, TimeEntryStatus status);

    /**
     * Lista lançamentos do provider ordenados por data (mais recente primeiro).
     */
    List<TimeEntry> findByUserIdOrderByEntryDateDesc(UUID userId);

    /**
     * Lista lançamentos revisáveis (SUBMITTED + PENDING_APPROVAL) de um contrato.
     */
    @Query("SELECT t FROM TimeEntry t " +
           "WHERE t.contractId = :contractId " +
           "AND t.status IN ('SUBMITTED', 'PENDING_APPROVAL') " +
           "ORDER BY t.entryDate DESC")
    List<TimeEntry> findPendingByContractId(@Param("contractId") UUID contractId);

    /**
     * Soma horas não-disputadas de um contrato para validar teto.
     * Inclui todos os status exceto DISPUTED.
     */
    @Query("SELECT COALESCE(SUM(t.hours), 0) FROM TimeEntry t " +
           "WHERE t.contractId = :contractId AND t.status <> 'DISPUTED'")
    BigDecimal sumHoursExcludingDisputed(@Param("contractId") UUID contractId);

    /**
     * Soma total de horas aprovadas de um contrato em um período.
     */
    @Query("SELECT COALESCE(SUM(t.hours), 0) FROM TimeEntry t " +
           "WHERE t.contractId = :contractId " +
           "AND t.status = 'APPROVED' " +
           "AND t.entryDate BETWEEN :startDate AND :endDate")
    BigDecimal sumApprovedHoursByContractAndPeriod(
            @Param("contractId") UUID contractId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Soma total de horas aprovadas de um contrato (all-time).
     */
    @Query("SELECT COALESCE(SUM(t.hours), 0) FROM TimeEntry t " +
           "WHERE t.contractId = :contractId AND t.status = 'APPROVED'")
    BigDecimal sumApprovedHoursByContract(@Param("contractId") UUID contractId);

    /**
     * Busca lançamentos por status global (para jobs agendados).
     */
    List<TimeEntry> findByStatus(TimeEntryStatus status);

    /**
     * Busca lançamentos PENDING_APPROVAL cujo updatedAt é anterior ao cutoff.
     * Usada para auto-aprovação após 48h sem resposta.
     */
    @Query("SELECT t FROM TimeEntry t " +
           "WHERE t.status = 'PENDING_APPROVAL' " +
           "AND t.updatedAt < :cutoff")
    List<TimeEntry> findPendingApprovalOlderThan(@Param("cutoff") ZonedDateTime cutoff);
}
