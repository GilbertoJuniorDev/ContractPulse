package com.contractpulse.timeentry.exception;

import java.util.UUID;

/**
 * Exceção lançada quando um lançamento de horas não é encontrado.
 * Mapeia para HTTP 404.
 */
public class TimeEntryNotFoundException extends RuntimeException {

    public TimeEntryNotFoundException(UUID timeEntryId) {
        super("Time entry not found with id: " + timeEntryId);
    }
}
