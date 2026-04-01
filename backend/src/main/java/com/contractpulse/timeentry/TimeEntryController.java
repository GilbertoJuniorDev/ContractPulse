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
 * <p>
 * Ciclo completo: DRAFT → SUBMITTED → PENDING_APPROVAL → APPROVED | DISPUTED → INVOICED
 * </p>
 */
@RestController
@RequestMapping("/api/time-entries")
@Tag(name = "Time Entries", description = "Lançamento e aprovação de horas")
public class TimeEntryController {

    private final TimeEntryService timeEntryService;
    private final CurrentUserService currentUserService;
    private final ApprovalService approvalService;

    public TimeEntryController(TimeEntryService timeEntryService,
                               CurrentUserService currentUserService,
                               ApprovalService approvalService) {
        this.timeEntryService = timeEntryService;
        this.currentUserService = currentUserService;
        this.approvalService = approvalService;
    }

    /**
     * Cria um novo lançamento de horas como DRAFT (provider).
     */
    @PostMapping
    @Operation(summary = "Lançar horas em um contrato (cria como DRAFT)")
    public ResponseEntity<TimeEntryResponse> createTimeEntry(
            @Valid @RequestBody CreateTimeEntryRequest request) {

        UUID userId = currentUserService.getCurrentUserId();
        TimeEntryResponse response = timeEntryService.createTimeEntry(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Submete um lançamento de horas para aprovação (DRAFT → SUBMITTED).
     */
    @PatchMapping("/{id}/submit")
    @Operation(summary = "Submeter lançamento de horas para aprovação")
    public ResponseEntity<TimeEntryResponse> submitTimeEntry(@PathVariable UUID id) {

        UUID userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(timeEntryService.submitTimeEntry(userId, id));
    }

    /**
     * Lista lançamentos do provider autenticado.
     */
    @GetMapping("/my")
    @Operation(summary = "Listar meus lançamentos de horas")
    public ResponseEntity<List<TimeEntryResponse>> findMyTimeEntries() {

        UUID userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(timeEntryService.findByProvider(userId));
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
     * Lista lançamentos pendentes de aprovação de um contrato.
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

    /**
     * Aprova em lote todos os lançamentos PENDING_APPROVAL de um contrato (client).
     */
    @PatchMapping("/contract/{contractId}/approve-all")
    @Operation(summary = "Aprovar em lote todos os lançamentos pendentes de um contrato")
    public ResponseEntity<List<TimeEntryResponse>> batchApprove(
            @PathVariable UUID contractId) {

        UUID reviewerId = currentUserService.getCurrentUserId();
        List<TimeEntryResponse> approved = approvalService.batchApprove(reviewerId, contractId);
        return ResponseEntity.ok(approved);
    }

    /**
     * Remove um lançamento de horas em DRAFT (provider).
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Remover lançamento de horas (apenas DRAFT)")
    public ResponseEntity<Void> deleteTimeEntry(@PathVariable UUID id) {

        UUID userId = currentUserService.getCurrentUserId();
        timeEntryService.deleteTimeEntry(userId, id);
        return ResponseEntity.noContent().build();
    }
}
