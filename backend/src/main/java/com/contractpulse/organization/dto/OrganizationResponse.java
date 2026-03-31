package com.contractpulse.organization.dto;

import com.contractpulse.organization.model.Organization;
import com.contractpulse.organization.model.OrganizationPlan;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * DTO de resposta com dados da organização.
 */
public record OrganizationResponse(
        UUID id,
        String name,
        UUID ownerId,
        OrganizationPlan plan,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {

    /**
     * Converte a entidade Organization em DTO de resposta.
     */
    public static OrganizationResponse fromEntity(Organization organization) {
        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getOwner().getId(),
                organization.getPlan(),
                organization.getCreatedAt(),
                organization.getUpdatedAt()
        );
    }
}
