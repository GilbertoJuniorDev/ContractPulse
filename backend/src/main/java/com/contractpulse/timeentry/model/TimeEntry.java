package com.contractpulse.timeentry.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade que representa um lançamento de horas em um contrato.
 * O provider lança; o client aprova ou disputa.
 */
@Entity
@Table(name = "time_entries")
public class TimeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "contract_id", nullable = false)
    private UUID contractId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal hours;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TimeEntryStatus status;

    @Column(name = "reviewer_id")
    private UUID reviewerId;

    @Column(name = "reviewed_at")
    private ZonedDateTime reviewedAt;

    @Column(name = "dispute_reason", length = 500)
    private String disputeReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    protected TimeEntry() {
        // Construtor padrão exigido pelo JPA
    }

    private TimeEntry(Builder builder) {
        this.id = builder.id;
        this.contractId = builder.contractId;
        this.userId = builder.userId;
        this.description = builder.description;
        this.hours = builder.hours;
        this.entryDate = builder.entryDate;
        this.status = builder.status;
        this.reviewerId = builder.reviewerId;
        this.reviewedAt = builder.reviewedAt;
        this.disputeReason = builder.disputeReason;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    // --- Getters ---

    public UUID getId() {
        return id;
    }

    public UUID getContractId() {
        return contractId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getHours() {
        return hours;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public TimeEntryStatus getStatus() {
        return status;
    }

    public UUID getReviewerId() {
        return reviewerId;
    }

    public ZonedDateTime getReviewedAt() {
        return reviewedAt;
    }

    public String getDisputeReason() {
        return disputeReason;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    // --- Métodos de domínio ---

    /**
     * Aprova o lançamento de horas.
     *
     * @param reviewerId ID do cliente que está aprovando
     */
    public void approve(UUID reviewerId) {
        Objects.requireNonNull(reviewerId, "Reviewer ID must not be null");
        if (this.status != TimeEntryStatus.PENDING) {
            throw new IllegalStateException("Only PENDING entries can be approved");
        }
        this.status = TimeEntryStatus.APPROVED;
        this.reviewerId = reviewerId;
        this.reviewedAt = ZonedDateTime.now();
        this.disputeReason = null;
    }

    /**
     * Disputa o lançamento de horas com uma justificativa.
     *
     * @param reviewerId    ID do cliente que está disputando
     * @param disputeReason motivo da disputa
     */
    public void dispute(UUID reviewerId, String disputeReason) {
        Objects.requireNonNull(reviewerId, "Reviewer ID must not be null");
        Objects.requireNonNull(disputeReason, "Dispute reason must not be null");
        if (this.status != TimeEntryStatus.PENDING) {
            throw new IllegalStateException("Only PENDING entries can be disputed");
        }
        this.status = TimeEntryStatus.DISPUTED;
        this.reviewerId = reviewerId;
        this.reviewedAt = ZonedDateTime.now();
        this.disputeReason = disputeReason;
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
            status = TimeEntryStatus.PENDING;
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
        private UUID contractId;
        private UUID userId;
        private String description;
        private BigDecimal hours;
        private LocalDate entryDate;
        private TimeEntryStatus status = TimeEntryStatus.PENDING;
        private UUID reviewerId;
        private ZonedDateTime reviewedAt;
        private String disputeReason;
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder contractId(UUID contractId) {
            this.contractId = contractId;
            return this;
        }

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder hours(BigDecimal hours) {
            this.hours = hours;
            return this;
        }

        public Builder entryDate(LocalDate entryDate) {
            this.entryDate = entryDate;
            return this;
        }

        public Builder status(TimeEntryStatus status) {
            this.status = status;
            return this;
        }

        public Builder reviewerId(UUID reviewerId) {
            this.reviewerId = reviewerId;
            return this;
        }

        public Builder reviewedAt(ZonedDateTime reviewedAt) {
            this.reviewedAt = reviewedAt;
            return this;
        }

        public Builder disputeReason(String disputeReason) {
            this.disputeReason = disputeReason;
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

        public TimeEntry build() {
            Objects.requireNonNull(contractId, "Contract ID is required");
            Objects.requireNonNull(userId, "User ID is required");
            Objects.requireNonNull(description, "Description is required");
            Objects.requireNonNull(hours, "Hours is required");
            Objects.requireNonNull(entryDate, "Entry date is required");
            return new TimeEntry(this);
        }
    }
}
