package com.contractpulse.organization.exception;

import java.util.UUID;

/**
 * Exceção lançada quando um usuário tenta acessar uma organização sem permissão.
 */
public class OrganizationAccessDeniedException extends RuntimeException {

    public OrganizationAccessDeniedException(UUID userId, UUID organizationId) {
        super("User " + userId + " does not have access to organization " + organizationId);
    }
}
