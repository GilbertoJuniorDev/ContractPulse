package com.contractpulse.organization.exception;

import java.util.UUID;

/**
 * Exceção lançada quando uma organização não é encontrada.
 */
public class OrganizationNotFoundException extends RuntimeException {

    public OrganizationNotFoundException(UUID organizationId) {
        super("Organization not found with id: " + organizationId);
    }
}
