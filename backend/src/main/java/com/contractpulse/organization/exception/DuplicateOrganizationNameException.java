package com.contractpulse.organization.exception;

/**
 * Exceção lançada quando o owner já possui uma organização com o mesmo nome.
 */
public class DuplicateOrganizationNameException extends RuntimeException {

    public DuplicateOrganizationNameException(String name) {
        super("Organization with name '" + name + "' already exists for this owner");
    }
}
