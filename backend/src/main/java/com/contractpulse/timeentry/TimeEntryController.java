package com.contractpulse.timeentry;

import com.contractpulse.auth.CurrentUserService;
import com.contractpulse.timeentry.dto.CreateTimeEntryRequest;
import com.contractpulse.timeentry.dto.ReviewTimeEntryRequest;
import com.contractpulse.timeentry.dto.TimeEntryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST para operações de lançamento de horas.
 */
@RestController
@RequestMapping("/api/time-entries")
@Tag(name = "Time Entries", description = "Lançamento e aprovação de horas")
public class TimeEntryController {

    private final TimeEntryService timeEntryService;
    private final CurrentUserService currentUserService;

    public TimeEntryController(TimeEntryService timeEntryService,
                               CurrentUserService currentUserService) {
        this.timeEntryService = timeEntryService;
        this.currentUserService = currentUserService;
    }

    /**
     * Cria um novo lançamento de horas (provider).
     */
    @PostMapping
    @Operation(summary = "Lançar horas em um contrato")
    public ResponseEntity<TimeEntryResponse> createTimeEntry(
            @Valid @RequestBody CreateTimeEntryRequest request) {

        UUID userId = currentUserService.getCurrentUserId();
        TimeEntryResponse response = timeEntryService.createTimeEntry(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lista todos os lançamentos de um contrato.
     */
    @GetMapping("/contract/{contractId}")
    @Operation(summary = "Listar lançamentos de um contrato")
    public ResponseEntity<List<TimeEntryResponse>> findByContract(
            @PathVariable UUID contractId) {

        return ResponseEntity.ok(timeEntryService.findByContract(contractId));
    }

    /**
     * Lista lançamentos pendentes de um contrato (pipeline de aprovação do client).
     */
    @GetMapping("/contract/{contractId}/pending")
    @Operation(summary = "Listar lançamentos pendentes de aprovação")
    public ResponseEntity<List<TimeEntryResponse>> findPendingByContract(
            @PathVariable UUID contractId) {

        return ResponseEntity.ok(timeEntryService.findPendingByContract(contractId));
    }

    /**
     * Aprova um lançamento de horas (client).
     */
    @PatchMapping("/{id}/approve")
    @Operation(summary = "Aprovar lançamento de horas")
    public ResponseEntity<TimeEntryResponse> approveTimeEntry(@PathVariable UUID id) {

        UUID reviewerId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(timeEntryService.approveTimeEntry(reviewerId, id));
    }

    /**
     * Disputa um lançamento de horas (client).
     */
    @PatchMapping("/{id}/dispute")
    @Operation(summary = "Disputar lançamento de horas")
    public ResponseEntity<TimeEntryResponse> disputeTimeEntry(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewTimeEntryRequest request) {

        UUID reviewerId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(timeEntryService.disputeTimeEntry(reviewerId, id, request));
    }
}
