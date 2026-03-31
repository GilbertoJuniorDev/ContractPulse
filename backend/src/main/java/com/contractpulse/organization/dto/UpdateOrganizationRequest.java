package com.contractpulse.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de requisição para atualização de uma organização.
 */
public record UpdateOrganizationRequest(

        @NotBlank(message = "Organization name is required")
        @Size(min = 2, max = 255, message = "Organization name must be between 2 and 255 characters")
        String name
) {
}
