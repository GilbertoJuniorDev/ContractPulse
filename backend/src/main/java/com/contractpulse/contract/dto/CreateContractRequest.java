package com.contractpulse.contract.dto;

import com.contractpulse.contract.model.ContractCurrency;
import com.contractpulse.contract.model.ContractType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de entrada para criação de contrato.
 */
public record CreateContractRequest(

        @NotNull(message = "Organization ID is required")
        UUID organizationId,

        @NotNull(message = "Client user ID is required")
        UUID clientUserId,

        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must have at most 255 characters")
        String title,

        @NotNull(message = "Contract type is required")
        ContractType type,

        @NotNull(message = "Currency is required")
        ContractCurrency currency,

        @NotNull(message = "Billing day is required")
        @Min(value = 1, message = "Billing day must be between 1 and 28")
        @Max(value = 28, message = "Billing day must be between 1 and 28")
        Integer billingDay,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        LocalDate endDate,

        /** Configuração específica para contratos do tipo RETAINER */
        @Valid
        RetainerConfigRequest retainerConfig
) {
}
