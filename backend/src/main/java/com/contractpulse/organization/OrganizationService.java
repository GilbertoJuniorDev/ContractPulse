package com.contractpulse.organization;

import com.contractpulse.organization.dto.CreateOrganizationRequest;
import com.contractpulse.organization.dto.OrganizationResponse;
import com.contractpulse.organization.dto.UpdateOrganizationRequest;
import com.contractpulse.organization.exception.DuplicateOrganizationNameException;
import com.contractpulse.organization.exception.OrganizationAccessDeniedException;
import com.contractpulse.organization.exception.OrganizationNotFoundException;
import com.contractpulse.organization.model.Organization;
import com.contractpulse.user.UserRepository;
import com.contractpulse.user.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Serviço com regras de negócio de organização.
 */
@Service
public class OrganizationService {

    private static final Logger log = LoggerFactory.getLogger(OrganizationService.class);

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    public OrganizationService(OrganizationRepository organizationRepository,
                               UserRepository userRepository) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Cria uma nova organização para o usuário autenticado.
     *
     * @param ownerId ID do usuário owner (extraído do JWT)
     * @param request dados da organização
     * @return resposta com dados da organização criada
     */
    @Transactional
    public OrganizationResponse createOrganization(UUID ownerId, CreateOrganizationRequest request) {
        Objects.requireNonNull(ownerId, "Owner ID must not be null");
        Objects.requireNonNull(request, "CreateOrganizationRequest must not be null");

        if (organizationRepository.existsByNameAndOwnerId(request.name(), ownerId)) {
            throw new DuplicateOrganizationNameException(request.name());
        }

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + ownerId));

        Organization organization = Organization.builder()
                .name(request.name())
                .owner(owner)
                .build();

        Organization saved = organizationRepository.save(organization);
        log.info("Organization created: id={}, name={}, owner={}", saved.getId(), saved.getName(), ownerId);

        return OrganizationResponse.fromEntity(saved);
    }

    /**
     * Busca uma organização pelo ID, validando acesso do usuário.
     *
     * @param organizationId ID da organização
     * @param requesterId    ID do usuário que está fazendo a requisição
     * @return resposta com dados da organização
     */
    @Transactional(readOnly = true)
    public OrganizationResponse findById(UUID organizationId, UUID requesterId) {
        Objects.requireNonNull(organizationId, "Organization ID must not be null");
        Objects.requireNonNull(requesterId, "Requester ID must not be null");

        Organization organization = findOrganizationOrThrow(organizationId);
        validateOwnership(organization, requesterId);

        return OrganizationResponse.fromEntity(organization);
    }

    /**
     * Lista todas as organizações do usuário autenticado.
     *
     * @param ownerId ID do usuário owner
     * @return lista de organizações do owner
     */
    @Transactional(readOnly = true)
    public List<OrganizationResponse> findAllByOwner(UUID ownerId) {
        Objects.requireNonNull(ownerId, "Owner ID must not be null");

        return organizationRepository.findByOwnerId(ownerId)
                .stream()
                .map(OrganizationResponse::fromEntity)
                .toList();
    }

    /**
     * Atualiza os dados de uma organização existente.
     *
     * @param organizationId ID da organização
     * @param requesterId    ID do usuário que está fazendo a requisição
     * @param request        dados atualizados
     * @return resposta com dados atualizados
     */
    @Transactional
    public OrganizationResponse updateOrganization(UUID organizationId, UUID requesterId,
                                                    UpdateOrganizationRequest request) {
        Objects.requireNonNull(organizationId, "Organization ID must not be null");
        Objects.requireNonNull(requesterId, "Requester ID must not be null");
        Objects.requireNonNull(request, "UpdateOrganizationRequest must not be null");

        Organization organization = findOrganizationOrThrow(organizationId);
        validateOwnership(organization, requesterId);

        // Verifica nome duplicado apenas se o nome está sendo alterado
        if (!organization.getName().equals(request.name())
                && organizationRepository.existsByNameAndOwnerId(request.name(), requesterId)) {
            throw new DuplicateOrganizationNameException(request.name());
        }

        organization.updateName(request.name());
        Organization updated = organizationRepository.save(organization);
        log.info("Organization updated: id={}, newName={}", updated.getId(), updated.getName());

        return OrganizationResponse.fromEntity(updated);
    }

    /**
     * Remove uma organização pelo ID.
     *
     * @param organizationId ID da organização
     * @param requesterId    ID do usuário que está fazendo a requisição
     */
    @Transactional
    public void deleteOrganization(UUID organizationId, UUID requesterId) {
        Objects.requireNonNull(organizationId, "Organization ID must not be null");
        Objects.requireNonNull(requesterId, "Requester ID must not be null");

        Organization organization = findOrganizationOrThrow(organizationId);
        validateOwnership(organization, requesterId);

        organizationRepository.delete(organization);
        log.info("Organization deleted: id={}, owner={}", organizationId, requesterId);
    }

    private Organization findOrganizationOrThrow(UUID organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException(organizationId));
    }

    private void validateOwnership(Organization organization, UUID requesterId) {
        if (!organization.getOwner().getId().equals(requesterId)) {
            throw new OrganizationAccessDeniedException(requesterId, organization.getId());
        }
    }
}
