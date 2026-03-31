package com.contractpulse.timeentry;

import com.contractpulse.timeentry.model.TimeEntry;
import com.contractpulse.timeentry.model.TimeEntryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
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
     * Lista lançamentos pendentes de um contrato (pipeline de aprovação do client).
     */
    default List<TimeEntry> findPendingByContractId(UUID contractId) {
        return findByContractIdAndStatusOrderByEntryDateDesc(contractId, TimeEntryStatus.PENDING);
    }

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
}
