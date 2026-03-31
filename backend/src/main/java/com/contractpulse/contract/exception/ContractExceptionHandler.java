package com.contractpulse.contract.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

/**
 * Handler centralizado de exceções do módulo de contratos.
 * Retorna respostas no formato ProblemDetail (RFC 7807).
 */
@RestControllerAdvice(basePackages = "com.contractpulse.contract")
public class ContractExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ContractExceptionHandler.class);

    @ExceptionHandler(ContractNotFoundException.class)
    public ProblemDetail handleNotFound(ContractNotFoundException ex) {
        log.warn(ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Contract not found");
        problem.setType(URI.create("https://contractpulse.com/errors/contract-not-found"));
        return problem;
    }

    @ExceptionHandler(ContractInactiveException.class)
    public ProblemDetail handleInactive(ContractInactiveException ex) {
        log.warn(ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Contract inactive");
        problem.setType(URI.create("https://contractpulse.com/errors/contract-inactive"));
        return problem;
    }

    @ExceptionHandler(InvalidContractConfigException.class)
    public ProblemDetail handleInvalidConfig(InvalidContractConfigException ex) {
        log.warn(ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid contract configuration");
        problem.setType(URI.create("https://contractpulse.com/errors/invalid-contract-config"));
        return problem;
    }
}
