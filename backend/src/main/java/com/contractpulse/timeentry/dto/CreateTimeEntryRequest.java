package com.contractpulse.timeentry.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO para criação de lançamento de horas pelo provider.
 */
public record CreateTimeEntryRequest(

        @NotNull(message = "ID do contrato é obrigatório")
        UUID contractId,

        @NotBlank(message = "Descrição é obrigatória")
        @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
        String description,

        @NotNull(message = "Horas são obrigatórias")
        @DecimalMin(value = "0.01", message = "Horas devem ser maiores que zero")
        @Digits(integer = 4, fraction = 2, message = "Horas devem ter no máximo 4 dígitos inteiros e 2 decimais")
        BigDecimal hours,

        @NotNull(message = "Data do lançamento é obrigatória")
        LocalDate entryDate
) {
}
