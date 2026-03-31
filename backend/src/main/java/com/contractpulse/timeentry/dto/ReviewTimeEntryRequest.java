package com.contractpulse.timeentry.dto;

import jakarta.validation.constraints.Size;

/**
 * DTO para disputa de um lançamento de horas pelo cliente.
 */
public record ReviewTimeEntryRequest(

        @Size(max = 500, message = "Motivo da disputa deve ter no máximo 500 caracteres")
        String disputeReason
) {
}
