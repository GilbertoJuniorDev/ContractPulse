package com.contractpulse.user.model;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Entidade que representa um usuário do sistema.
 * O ID é o mesmo UUID gerado pelo Supabase Auth.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.PROVIDER;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    protected User() {
        // Construtor padrão exigido pelo JPA
    }

    private User(Builder builder) {
        this.id = builder.id;
        this.fullName = builder.fullName;
        this.email = builder.email;
        this.avatarUrl = builder.avatarUrl;
        this.role = builder.role;
        this.createdAt = builder.createdAt != null ? builder.createdAt : ZonedDateTime.now();
        this.updatedAt = builder.updatedAt != null ? builder.updatedAt : ZonedDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public UserRole getRole() {
        return role;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Atualiza o perfil do usuário (nome e avatar).
     */
    public void updateProfile(String fullName, String avatarUrl) {
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
        this.updatedAt = ZonedDateTime.now();
    }

    /**
     * Atualiza apenas o nome do usuário.
     * Usado pelo sync para não sobrescrever avatar customizado.
     */
    public void updateFullName(String fullName) {
        this.fullName = fullName;
        this.updatedAt = ZonedDateTime.now();
    }

    /**
     * Altera o papel (role) do usuário no sistema.
     */
    public void changeRole(UserRole newRole) {
        this.role = newRole;
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
        private String fullName;
        private String email;
        private String avatarUrl;
        private UserRole role = UserRole.PROVIDER;
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public Builder role(UserRole role) {
            this.role = role;
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

        public User build() {
            return new User(this);
        }
    }
}
