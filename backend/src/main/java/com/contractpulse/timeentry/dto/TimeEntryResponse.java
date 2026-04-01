package com.contractpulse.timeentry.dto;

import com.contractpulse.timeentry.model.TimeEntry;
import com.contractpulse.timeentry.model.TimeEntryStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * DTO de resposta para lançamento de horas.
 */
public record TimeEntryResponse(
        UUID id,
        UUID contractId,
        UUID userId,
        String description,
        String aiSummary,
        BigDecimal hours,
        LocalDate entryDate,
        TimeEntryStatus status,
        UUID reviewerId,
        ZonedDateTime reviewedAt,
        String disputeReason,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {

    /**
     * Converte a entidade para o DTO de resposta.
     */
    public static TimeEntryResponse fromEntity(TimeEntry entity) {
        return new TimeEntryResponse(
                entity.getId(),
                entity.getContractId(),
                entity.getUserId(),
                entity.getDescription(),
                entity.getAiSummary(),
                entity.getHours(),
                entity.getEntryDate(),
                entity.getStatus(),
                entity.getReviewerId(),
                entity.getReviewedAt(),
                entity.getDisputeReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
