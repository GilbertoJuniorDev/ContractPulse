package com.contractpulse.contract.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade de domínio que representa um contrato.
 * O campo {@code config} armazena configurações específicas do tipo como JSONB.
 */
@Entity
@Table(name = "contracts")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "client_user_id", nullable = false)
    private UUID clientUserId;

    @Column(nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ContractType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private ContractCurrency currency;

    @Column(name = "billing_day", nullable = false)
    private Integer billingDay;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ContractStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> config;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    protected Contract() {
        // Construtor padrão exigido pelo JPA
    }

    private Contract(Builder builder) {
        this.id = builder.id;
        this.organizationId = builder.organizationId;
        this.clientUserId = builder.clientUserId;
        this.title = builder.title;
        this.type = builder.type;
        this.currency = builder.currency;
        this.billingDay = builder.billingDay;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.status = builder.status;
        this.config = builder.config;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    // --- Getters ---

    public UUID getId() {
        return id;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public UUID getClientUserId() {
        return clientUserId;
    }

    public String getTitle() {
        return title;
    }

    public ContractType getType() {
        return type;
    }

    public ContractCurrency getCurrency() {
        return currency;
    }

    public Integer getBillingDay() {
        return billingDay;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    // --- Métodos de domínio ---

    /**
     * Verifica se o contrato está ativo.
     */
    public boolean isActive() {
        return this.status == ContractStatus.ACTIVE;
    }

    /**
     * Atualiza o título do contrato.
     */
    public void updateTitle(String newTitle) {
        Objects.requireNonNull(newTitle, "Contract title must not be null");
        this.title = newTitle;
        this.updatedAt = ZonedDateTime.now();
    }

    /**
     * Atualiza a moeda do contrato.
     */
    public void updateCurrency(ContractCurrency newCurrency) {
        Objects.requireNonNull(newCurrency, "Currency must not be null");
        this.currency = newCurrency;
        this.updatedAt = ZonedDateTime.now();
    }

    /**
     * Atualiza o dia de faturamento (billing day).
     */
    public void updateBillingDay(Integer newBillingDay) {
        Objects.requireNonNull(newBillingDay, "Billing day must not be null");
        this.billingDay = newBillingDay;
        this.updatedAt = ZonedDateTime.now();
    }

    /**
     * Atualiza a data de término do contrato.
     */
    public void updateEndDate(LocalDate newEndDate) {
        this.endDate = newEndDate;
        this.updatedAt = ZonedDateTime.now();
    }

    /**
     * Atualiza o JSONB config do contrato.
     */
    public void updateConfig(Map<String, Object> newConfig) {
        Objects.requireNonNull(newConfig, "Config must not be null");
        this.config = newConfig;
        this.updatedAt = ZonedDateTime.now();
    }

    /**
     * Altera o status do contrato.
     */
    public void changeStatus(ContractStatus newStatus) {
        Objects.requireNonNull(newStatus, "Contract status must not be null");
        this.status = newStatus;
        this.updatedAt = ZonedDateTime.now();
    }

    // --- Lifecycle callbacks ---

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = ZonedDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = ZonedDateTime.now();
        }
        if (status == null) {
            status = ContractStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }

    // --- Builder ---

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private UUID id;
        private UUID organizationId;
        private UUID clientUserId;
        private String title;
        private ContractType type;
        private ContractCurrency currency = ContractCurrency.BRL;
        private Integer billingDay;
        private LocalDate startDate;
        private LocalDate endDate;
        private ContractStatus status = ContractStatus.ACTIVE;
        private Map<String, Object> config = Map.of();
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder organizationId(UUID organizationId) {
            this.organizationId = organizationId;
            return this;
        }

        public Builder clientUserId(UUID clientUserId) {
            this.clientUserId = clientUserId;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder type(ContractType type) {
            this.type = type;
            return this;
        }

        public Builder currency(ContractCurrency currency) {
            this.currency = currency;
            return this;
        }

        public Builder billingDay(Integer billingDay) {
            this.billingDay = billingDay;
            return this;
        }

        public Builder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder status(ContractStatus status) {
            this.status = status;
            return this;
        }

        public Builder config(Map<String, Object> config) {
            this.config = config;
            return this;
        }

        public Builder createdAt(ZonedDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(ZonedDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Contract build() {
            Objects.requireNonNull(organizationId, "Organization ID is required");
            Objects.requireNonNull(clientUserId, "Client user ID is required");
            Objects.requireNonNull(title, "Contract title is required");
            Objects.requireNonNull(type, "Contract type is required");
            Objects.requireNonNull(billingDay, "Billing day is required");
            Objects.requireNonNull(startDate, "Start date is required");
            return new Contract(this);
        }
    }
}
