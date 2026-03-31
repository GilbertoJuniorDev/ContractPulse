package com.contractpulse.contract;

import com.contractpulse.contract.dto.ClientDashboardResponse;
import com.contractpulse.contract.exception.ContractNotFoundException;
import com.contractpulse.contract.model.Contract;
import com.contractpulse.timeentry.TimeEntryRepository;
import com.contractpulse.timeentry.exception.UnauthorizedReviewException;
import com.contractpulse.timeentry.model.TimeEntryStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Serviço que calcula métricas do dashboard do cliente.
 * Burn rate, saldo de horas, totais de aprovação.
 */
@Service
public class ClientDashboardService {

    private static final Logger log = LoggerFactory.getLogger(ClientDashboardService.class);

    private final ContractRepository contractRepository;
    private final TimeEntryRepository timeEntryRepository;

    public ClientDashboardService(ContractRepository contractRepository,
                                   TimeEntryRepository timeEntryRepository) {
        this.contractRepository = contractRepository;
        this.timeEntryRepository = timeEntryRepository;
    }

    /**
     * Calcula as métricas do dashboard do cliente para um contrato.
     *
     * @param clientUserId ID do cliente autenticado
     * @param contractId   ID do contrato
     * @return métricas do dashboard
     */
    @Transactional(readOnly = true)
    public ClientDashboardResponse getDashboard(UUID clientUserId, UUID contractId) {
        Objects.requireNonNull(clientUserId, "Client user ID must not be null");
        Objects.requireNonNull(contractId, "Contract ID must not be null");

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ContractNotFoundException(contractId));

        if (!contract.getClientUserId().equals(clientUserId)) {
            throw new UnauthorizedReviewException(clientUserId, contractId);
        }

        BigDecimal monthlyHours = extractMonthlyHoursFromConfig(contract.getConfig());

        // Período atual baseado no billing day
        LocalDate now = LocalDate.now();
        int billingDay = contract.getBillingDay();
        LocalDate periodStart = calculatePeriodStart(now, billingDay);
        LocalDate periodEnd = calculatePeriodEnd(now, billingDay);

        BigDecimal approvedHours = timeEntryRepository.sumApprovedHoursByContractAndPeriod(
                contractId, periodStart, periodEnd);

        BigDecimal remainingHours = monthlyHours.subtract(approvedHours).max(BigDecimal.ZERO);

        BigDecimal burnRate = BigDecimal.ZERO;
        if (monthlyHours.compareTo(BigDecimal.ZERO) > 0) {
            burnRate = approvedHours.divide(monthlyHours, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        var allEntries = timeEntryRepository.findByContractIdOrderByEntryDateDesc(contractId);
        long pendingCount = allEntries.stream()
                .filter(e -> e.getStatus() == TimeEntryStatus.PENDING).count();
        long approvedCount = allEntries.stream()
                .filter(e -> e.getStatus() == TimeEntryStatus.APPROVED).count();
        long disputedCount = allEntries.stream()
                .filter(e -> e.getStatus() == TimeEntryStatus.DISPUTED).count();

        log.info("Client dashboard calculated: contract={}, burnRate={}%", contractId, burnRate);

        return new ClientDashboardResponse(
                contractId,
                contract.getTitle(),
                monthlyHours,
                approvedHours,
                remainingHours,
                burnRate,
                pendingCount,
                approvedCount,
                disputedCount
        );
    }

    // --- Métodos internos ---

    private BigDecimal extractMonthlyHoursFromConfig(Map<String, Object> config) {
        if (config == null || !config.containsKey("monthlyHours")) {
            return BigDecimal.ZERO;
        }
        Object value = config.get("monthlyHours");
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return BigDecimal.ZERO;
    }

    private LocalDate calculatePeriodStart(LocalDate now, int billingDay) {
        int safeDay = Math.min(billingDay, now.lengthOfMonth());
        if (now.getDayOfMonth() >= safeDay) {
            return now.withDayOfMonth(safeDay);
        }
        LocalDate lastMonth = now.minusMonths(1);
        return lastMonth.withDayOfMonth(Math.min(billingDay, lastMonth.lengthOfMonth()));
    }

    private LocalDate calculatePeriodEnd(LocalDate now, int billingDay) {
        int safeDay = Math.min(billingDay, now.lengthOfMonth());
        if (now.getDayOfMonth() >= safeDay) {
            LocalDate nextMonth = now.plusMonths(1);
            return nextMonth.withDayOfMonth(Math.min(billingDay, nextMonth.lengthOfMonth())).minusDays(1);
        }
        return now.withDayOfMonth(safeDay).minusDays(1);
    }
}
