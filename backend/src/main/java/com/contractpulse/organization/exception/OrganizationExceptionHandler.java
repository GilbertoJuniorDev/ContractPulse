package com.contractpulse.organization.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handler de exceções do módulo de organização.
 */
@RestControllerAdvice(basePackages = "com.contractpulse.organization")
public class OrganizationExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(OrganizationExceptionHandler.class);

    @ExceptionHandler(OrganizationNotFoundException.class)
    public ProblemDetail handleNotFound(OrganizationNotFoundException ex) {
        log.warn(ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DuplicateOrganizationNameException.class)
    public ProblemDetail handleDuplicateName(DuplicateOrganizationNameException ex) {
        log.warn(ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(OrganizationAccessDeniedException.class)
    public ProblemDetail handleAccessDenied(OrganizationAccessDeniedException ex) {
        log.warn(ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }
}
