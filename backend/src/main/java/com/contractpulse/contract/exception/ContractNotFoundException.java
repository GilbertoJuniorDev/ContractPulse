package com.contractpulse.contract.exception;

import java.util.UUID;

/**
 * Exceção lançada quando um contrato não é encontrado.
 * Mapeia para HTTP 404.
 */
public class ContractNotFoundException extends RuntimeException {

    public ContractNotFoundException(UUID contractId) {
        super("Contract not found with id: " + contractId);
    }
}
