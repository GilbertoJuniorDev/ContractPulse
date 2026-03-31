package com.contractpulse.organization;

import com.contractpulse.organization.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositório JPA para a entidade Organization.
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    /**
     * Busca todas as organizações de um determinado owner.
     */
    List<Organization> findByOwnerId(UUID ownerId);

    /**
     * Verifica se já existe uma organização com o mesmo nome para o owner.
     */
    boolean existsByNameAndOwnerId(String name, UUID ownerId);
}
