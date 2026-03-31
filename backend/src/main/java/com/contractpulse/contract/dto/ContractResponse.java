package com.contractpulse.contract.dto;

import com.contractpulse.contract.model.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO de resposta para contrato.
 */
public record ContractResponse(
        UUID id,
        UUID organizationId,
        UUID clientUserId,
        String title,
        ContractType type,
        ContractCurrency currency,
        Integer billingDay,
        LocalDate startDate,
        LocalDate endDate,
        ContractStatus status,
        Map<String, Object> config,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {

    /**
     * Converte a entidade Contract em DTO de resposta.
     */
    public static ContractResponse fromEntity(Contract contract) {
        return new ContractResponse(
                contract.getId(),
                contract.getOrganizationId(),
                contract.getClientUserId(),
                contract.getTitle(),
                contract.getType(),
                contract.getCurrency(),
                contract.getBillingDay(),
                contract.getStartDate(),
                contract.getEndDate(),
                contract.getStatus(),
                contract.getConfig(),
                contract.getCreatedAt(),
                contract.getUpdatedAt()
        );
    }
}
