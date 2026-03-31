package com.contractpulse.contract.exception;

import java.util.UUID;

/**
 * Exceção lançada ao tentar operar sobre um contrato inativo.
 * Mapeia para HTTP 409 (Conflict).
 */
public class ContractInactiveException extends RuntimeException {

    public ContractInactiveException(UUID contractId) {
        super("Contract is not active: " + contractId);
    }
}
