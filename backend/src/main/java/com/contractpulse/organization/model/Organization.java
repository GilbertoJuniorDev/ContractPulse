package com.contractpulse.organization.model;

import com.contractpulse.user.model.User;
import jakarta.persistence.*;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade que representa uma organização (agência ou freelancer como entidade de negócio).
 */
@Entity
@Table(name = "organizations")
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrganizationPlan plan = OrganizationPlan.FREE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    protected Organization() {
        // Construtor padrão exigido pelo JPA
    }

    private Organization(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.owner = builder.owner;
        this.plan = builder.plan;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public User getOwner() {
        return owner;
    }

    public OrganizationPlan getPlan() {
        return plan;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Atualiza o nome da organização.
     */
    public void updateName(String newName) {
        Objects.requireNonNull(newName, "Organization name must not be null");
        if (newName.isBlank()) {
            throw new IllegalArgumentException("Organization name must not be blank");
        }
        this.name = newName;
        this.updatedAt = ZonedDateTime.now();
    }

    /**
     * Altera o plano de assinatura da organização.
     */
    public void changePlan(OrganizationPlan newPlan) {
        Objects.requireNonNull(newPlan, "Organization plan must not be null");
        this.plan = newPlan;
        this.updatedAt = ZonedDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = ZonedDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = ZonedDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private UUID id;
        private String name;
        private User owner;
        private OrganizationPlan plan = OrganizationPlan.FREE;
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder owner(User owner) {
            this.owner = owner;
            return this;
        }

        public Builder plan(OrganizationPlan plan) {
            this.plan = plan;
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

        public Organization build() {
            Objects.requireNonNull(name, "Organization name is required");
            Objects.requireNonNull(owner, "Organization owner is required");
            return new Organization(this);
        }
    }
}
