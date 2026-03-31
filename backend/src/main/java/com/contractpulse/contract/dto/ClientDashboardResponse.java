package com.contractpulse.contract.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO com métricas do dashboard do cliente para um contrato específico.
 * Inclui burn rate, saldo de horas e totais de aprovação.
 */
public record ClientDashboardResponse(
        UUID contractId,
        String contractTitle,
        BigDecimal monthlyHours,
        BigDecimal approvedHoursThisPeriod,
        BigDecimal remainingHours,
        BigDecimal burnRatePercentage,
        long pendingEntries,
        long approvedEntries,
        long disputedEntries
) {
}
