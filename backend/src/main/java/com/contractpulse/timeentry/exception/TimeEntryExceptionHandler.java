package com.contractpulse.timeentry.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

/**
 * Handler centralizado de exceções do módulo de time entries.
 * Retorna respostas no formato ProblemDetail (RFC 7807).
 */
@RestControllerAdvice(basePackages = "com.contractpulse.timeentry")
public class TimeEntryExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(TimeEntryExceptionHandler.class);

    @ExceptionHandler(TimeEntryNotFoundException.class)
    public ProblemDetail handleNotFound(TimeEntryNotFoundException ex) {
        log.warn(ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Time entry not found");
        problem.setType(URI.create("https://contractpulse.com/errors/time-entry-not-found"));
        return problem;
    }

    @ExceptionHandler(UnauthorizedReviewException.class)
    public ProblemDetail handleUnauthorizedReview(UnauthorizedReviewException ex) {
        log.warn(ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN, ex.getMessage());
        problem.setTitle("Unauthorized review");
        problem.setType(URI.create("https://contractpulse.com/errors/unauthorized-review"));
        return problem;
    }
}
