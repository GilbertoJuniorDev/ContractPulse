package com.contractpulse.timeentry.exception;

import java.util.UUID;

/**
 * Exceção lançada quando um usuário tenta aprovar/disputar
 * um lançamento sem ser o cliente do contrato.
 * Mapeia para HTTP 403.
 */
public class UnauthorizedReviewException extends RuntimeException {

    public UnauthorizedReviewException(UUID userId, UUID contractId) {
        super("User " + userId + " is not authorized to review entries for contract " + contractId);
    }
}
