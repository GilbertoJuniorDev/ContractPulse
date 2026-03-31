package com.contractpulse.user.exception;

import java.util.UUID;

/**
 * Exceção lançada quando um usuário não é encontrado pelo ID.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID userId) {
        super("User not found with id: " + userId);
    }
}
