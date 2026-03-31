package com.contractpulse.organization;

import com.contractpulse.auth.CurrentUserService;
import com.contractpulse.organization.dto.CreateOrganizationRequest;
import com.contractpulse.organization.dto.OrganizationResponse;
import com.contractpulse.organization.dto.UpdateOrganizationRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST para operações de organização.
 */
@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;
    private final CurrentUserService currentUserService;

    public OrganizationController(OrganizationService organizationService,
                                  CurrentUserService currentUserService) {
        this.organizationService = organizationService;
        this.currentUserService = currentUserService;
    }

    /**
     * Cria uma nova organização para o usuário autenticado.
     */
    @PostMapping
    public ResponseEntity<OrganizationResponse> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request) {

        UUID ownerId = currentUserService.getCurrentUserId();
        OrganizationResponse response = organizationService.createOrganization(ownerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Busca uma organização por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> findById(@PathVariable UUID id) {

        UUID requesterId = currentUserService.getCurrentUserId();
        OrganizationResponse response = organizationService.findById(id, requesterId);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista todas as organizações do usuário autenticado.
     */
    @GetMapping
    public ResponseEntity<List<OrganizationResponse>> findAllByOwner() {

        UUID ownerId = currentUserService.getCurrentUserId();
        List<OrganizationResponse> response = organizationService.findAllByOwner(ownerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Atualiza uma organização existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<OrganizationResponse> updateOrganization(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrganizationRequest request) {

        UUID requesterId = currentUserService.getCurrentUserId();
        OrganizationResponse response = organizationService.updateOrganization(id, requesterId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Remove uma organização.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganization(@PathVariable UUID id) {

        UUID requesterId = currentUserService.getCurrentUserId();
        organizationService.deleteOrganization(id, requesterId);
        return ResponseEntity.noContent().build();
    }
}
