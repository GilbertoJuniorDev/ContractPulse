package com.contractpulse.contract.dto;

import com.contractpulse.contract.model.ContractCurrency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * DTO de entrada para atualização de contrato.
 * Campos nulos não serão atualizados (atualização parcial).
 */
public record UpdateContractRequest(

        @Size(max = 255, message = "Title must have at most 255 characters")
        String title,

        ContractCurrency currency,

        @Min(value = 1, message = "Billing day must be between 1 and 28")
        @Max(value = 28, message = "Billing day must be between 1 and 28")
        Integer billingDay,

        LocalDate endDate,

        /** Configuração atualizada para contratos do tipo RETAINER */
        @Valid
        RetainerConfigRequest retainerConfig
) {
}
