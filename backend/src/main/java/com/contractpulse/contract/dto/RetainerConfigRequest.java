package com.contractpulse.contract.dto;

import com.contractpulse.contract.model.RolloverPolicy;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * DTO de entrada para configuração de contrato Retainer.
 */
public record RetainerConfigRequest(

        @NotNull(message = "Monthly hours is required")
        @Positive(message = "Monthly hours must be positive")
        Integer monthlyHours,

        @NotNull(message = "Hourly rate is required")
        @Positive(message = "Hourly rate must be positive")
        BigDecimal hourlyRate,

        @NotNull(message = "Rollover policy is required")
        RolloverPolicy rolloverPolicy,

        @Min(value = 1, message = "Alert threshold must be between 1 and 100")
        @Max(value = 100, message = "Alert threshold must be between 1 and 100")
        Integer alertThreshold,

        @NotNull(message = "Overage allowed flag is required")
        Boolean overageAllowed,

        @PositiveOrZero(message = "Overage rate must be positive or zero")
        BigDecimal overageRate
) {
}
