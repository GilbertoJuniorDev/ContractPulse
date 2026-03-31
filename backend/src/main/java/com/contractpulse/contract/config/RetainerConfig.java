package com.contractpulse.contract.config;

import com.contractpulse.contract.model.RolloverPolicy;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * POJO de configuração para contratos do tipo RETAINER (Banco de Horas).
 * Serializado como JSONB no campo config do contrato.
 */
public class RetainerConfig {

    /** Horas contratadas por mês */
    private Integer monthlyHours;

    /** Valor por hora */
    private BigDecimal hourlyRate;

    /** Política de rollover de horas não usadas */
    private RolloverPolicy rolloverPolicy;

    /** Percentual de consumo para disparar alerta (padrão: 80) */
    private Integer alertThreshold;

    /** Permite ultrapassar horas contratadas? */
    private Boolean overageAllowed;

    /** Valor da hora excedente (obrigatório se overageAllowed = true) */
    private BigDecimal overageRate;

    public RetainerConfig() {
        this.alertThreshold = 80;
    }

    private RetainerConfig(Builder builder) {
        this.monthlyHours = builder.monthlyHours;
        this.hourlyRate = builder.hourlyRate;
        this.rolloverPolicy = builder.rolloverPolicy;
        this.alertThreshold = builder.alertThreshold;
        this.overageAllowed = builder.overageAllowed;
        this.overageRate = builder.overageRate;
    }

    // --- Getters ---

    public Integer getMonthlyHours() {
        return monthlyHours;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public RolloverPolicy getRolloverPolicy() {
        return rolloverPolicy;
    }

    public Integer getAlertThreshold() {
        return alertThreshold;
    }

    public Boolean getOverageAllowed() {
        return overageAllowed;
    }

    public BigDecimal getOverageRate() {
        return overageRate;
    }

    // --- Builder ---

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Integer monthlyHours;
        private BigDecimal hourlyRate;
        private RolloverPolicy rolloverPolicy;
        private Integer alertThreshold = 80;
        private Boolean overageAllowed;
        private BigDecimal overageRate;

        public Builder monthlyHours(Integer monthlyHours) {
            this.monthlyHours = monthlyHours;
            return this;
        }

        public Builder hourlyRate(BigDecimal hourlyRate) {
            this.hourlyRate = hourlyRate;
            return this;
        }

        public Builder rolloverPolicy(RolloverPolicy rolloverPolicy) {
            this.rolloverPolicy = rolloverPolicy;
            return this;
        }

        public Builder alertThreshold(Integer alertThreshold) {
            this.alertThreshold = alertThreshold;
            return this;
        }

        public Builder overageAllowed(Boolean overageAllowed) {
            this.overageAllowed = overageAllowed;
            return this;
        }

        public Builder overageRate(BigDecimal overageRate) {
            this.overageRate = overageRate;
            return this;
        }

        public RetainerConfig build() {
            Objects.requireNonNull(monthlyHours, "Monthly hours is required");
            Objects.requireNonNull(hourlyRate, "Hourly rate is required");
            Objects.requireNonNull(rolloverPolicy, "Rollover policy is required");
            Objects.requireNonNull(overageAllowed, "Overage allowed flag is required");
            return new RetainerConfig(this);
        }
    }
}
