package com.contractpulse.contract;

import com.contractpulse.contract.model.Contract;
import com.contractpulse.contract.model.ContractStatus;
import com.contractpulse.contract.model.ContractType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositório JPA para a entidade Contract.
 */
@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {

    /**
     * Busca contratos de uma organização filtrados por status.
     */
    List<Contract> findByOrganizationIdAndStatus(UUID organizationId, ContractStatus status);

    /**
     * Busca todos os contratos de uma organização.
     */
    List<Contract> findByOrganizationId(UUID organizationId);

    /**
     * Busca contratos vinculados a um cliente.
     */
    List<Contract> findByClientUserId(UUID clientUserId);

    /**
     * Busca contratos por organização, status e tipo.
     */
    List<Contract> findByOrganizationIdAndStatusAndType(
            UUID organizationId, ContractStatus status, ContractType type);

    /**
     * Busca contratos ativos de uma organização — método nomeado (DRY).
     */
    default List<Contract> findActiveByOrganization(UUID organizationId) {
        return findByOrganizationIdAndStatus(organizationId, ContractStatus.ACTIVE);
    }
}
