package com.contractpulse.contract;

import com.contractpulse.auth.CurrentUserService;
import com.contractpulse.contract.dto.ClientDashboardResponse;
import com.contractpulse.contract.dto.ContractResponse;
import com.contractpulse.contract.dto.CreateContractRequest;
import com.contractpulse.contract.dto.UpdateContractRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST para operações de contrato.
 */
@RestController
@RequestMapping("/api/contracts")
@Tag(name = "Contracts", description = "Operações de gestão de contratos")
public class ContractController {

    private final ContractService contractService;
    private final ClientDashboardService clientDashboardService;
    private final CurrentUserService currentUserService;

    public ContractController(ContractService contractService,
                              ClientDashboardService clientDashboardService,
                              CurrentUserService currentUserService) {
        this.contractService = contractService;
        this.clientDashboardService = clientDashboardService;
        this.currentUserService = currentUserService;
    }

    /**
     * Cria um novo contrato.
     */
    @PostMapping
    @Operation(summary = "Criar novo contrato")
    public ResponseEntity<ContractResponse> createContract(
            @Valid @RequestBody CreateContractRequest request) {

        ContractResponse response = contractService.createContract(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Busca um contrato por ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar contrato por ID")
    public ResponseEntity<ContractResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(contractService.findById(id));
    }

    /**
     * Lista todos os contratos de uma organização.
     */
    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "Listar contratos de uma organização")
    public ResponseEntity<List<ContractResponse>> findByOrganization(
            @PathVariable UUID organizationId) {

        return ResponseEntity.ok(contractService.findByOrganization(organizationId));
    }

    /**
     * Lista contratos ativos de uma organização.
     */
    @GetMapping("/organization/{organizationId}/active")
    @Operation(summary = "Listar contratos ativos de uma organização")
    public ResponseEntity<List<ContractResponse>> findActiveByOrganization(
            @PathVariable UUID organizationId) {

        return ResponseEntity.ok(contractService.findActiveByOrganization(organizationId));
    }

    /**
     * Lista contratos vinculados a um cliente.
     */
    @GetMapping("/client/{clientUserId}")
    @Operation(summary = "Listar contratos de um cliente")
    public ResponseEntity<List<ContractResponse>> findByClient(
            @PathVariable UUID clientUserId) {

        return ResponseEntity.ok(contractService.findByClient(clientUserId));
    }

    /**
     * Atualiza um contrato existente.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar contrato")
    public ResponseEntity<ContractResponse> updateContract(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateContractRequest request) {

        return ResponseEntity.ok(contractService.updateContract(id, request));
    }

    /**
     * Pausa um contrato ativo.
     */
    @PatchMapping("/{id}/pause")
    @Operation(summary = "Pausar contrato")
    public ResponseEntity<ContractResponse> pauseContract(@PathVariable UUID id) {
        return ResponseEntity.ok(contractService.pauseContract(id));
    }

    /**
     * Reativa um contrato pausado.
     */
    @PatchMapping("/{id}/resume")
    @Operation(summary = "Reativar contrato pausado")
    public ResponseEntity<ContractResponse> resumeContract(@PathVariable UUID id) {
        return ResponseEntity.ok(contractService.resumeContract(id));
    }

    /**
     * Encerra um contrato.
     */
    @PatchMapping("/{id}/terminate")
    @Operation(summary = "Encerrar contrato")
    public ResponseEntity<ContractResponse> terminateContract(@PathVariable UUID id) {
        return ResponseEntity.ok(contractService.terminateContract(id));
    }

    /**
     * Dashboard do cliente para um contrato específico.
     * Retorna métricas de burn rate, saldo de horas e totais de aprovação.
     */
    @GetMapping("/{id}/client-dashboard")
    @Operation(summary = "Dashboard do cliente para um contrato")
    public ResponseEntity<ClientDashboardResponse> getClientDashboard(@PathVariable UUID id) {
        UUID clientUserId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(clientDashboardService.getDashboard(clientUserId, id));
    }
}
