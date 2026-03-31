package com.contractpulse.contract.exception;

/**
 * Exceção lançada quando a configuração do contrato é inválida.
 * Mapeia para HTTP 400 (Bad Request).
 */
public class InvalidContractConfigException extends RuntimeException {

    public InvalidContractConfigException(String message) {
        super(message);
    }
}
