package com.contractpulse.user.model;

/**
 * Papéis de um usuário no sistema ContractPulse.
 */
public enum UserRole {

    /** Freelancer ou agência que presta serviços */
    PROVIDER,

    /** Cliente que contrata serviços */
    CLIENT,

    /** Visualizador read-only no dashboard do cliente */
    CLIENT_VIEWER
}
