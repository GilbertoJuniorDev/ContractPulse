package com.contractpulse.contract;

import com.contractpulse.contract.config.RetainerConfig;
import com.contractpulse.contract.dto.*;
import com.contractpulse.contract.exception.ContractInactiveException;
import com.contractpulse.contract.exception.ContractNotFoundException;
import com.contractpulse.contract.exception.InvalidContractConfigException;
import com.contractpulse.contract.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Serviço com regras de negócio de contrato.
 * Orquestra criação, atualização, busca e mudança de status.
 */
@Service
public class ContractService {

    private static final Logger log = LoggerFactory.getLogger(ContractService.class);

    private final ContractRepository contractRepository;
    private final ObjectMapper objectMapper;

    public ContractService(ContractRepository contractRepository, ObjectMapper objectMapper) {
        this.contractRepository = contractRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Cria um novo contrato validando a configuração de acordo com o tipo.
     *
     * @param request dados do contrato a ser criado
     * @return resposta com dados do contrato criado
     */
    @Transactional
    public ContractResponse createContract(CreateContractRequest request) {
        Objects.requireNonNull(request, "CreateContractRequest must not be null");

        validateConfigForType(request);

        Map<String, Object> config = buildConfigMap(request);

        Contract contract = Contract.builder()
                .organizationId(request.organizationId())
                .clientUserId(request.clientUserId())
                .title(request.title())
                .type(request.type())
                .currency(request.currency())
                .billingDay(request.billingDay())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .status(ContractStatus.ACTIVE)
                .config(config)
                .build();

        Contract saved = contractRepository.save(contract);
        log.info("Contract created: id={}, type={}, org={}",
                saved.getId(), saved.getType(), saved.getOrganizationId());

        return ContractResponse.fromEntity(saved);
    }

    /**
     * Busca um contrato pelo ID ou lança exceção.
     *
     * @param contractId ID do contrato
     * @return resposta com dados do contrato
     */
    @Transactional(readOnly = true)
    public ContractResponse findById(UUID contractId) {
        Contract contract = findContractOrThrow(contractId);
        return ContractResponse.fromEntity(contract);
    }

    /**
     * Lista todos os contratos de uma organização.
     *
     * @param organizationId ID da organização
     * @return lista de contratos
     */
    @Transactional(readOnly = true)
    public List<ContractResponse> findByOrganization(UUID organizationId) {
        Objects.requireNonNull(organizationId, "Organization ID must not be null");
        return contractRepository.findByOrganizationId(organizationId).stream()
                .map(ContractResponse::fromEntity)
                .toList();
    }

    /**
     * Lista contratos ativos de uma organização.
     *
     * @param organizationId ID da organização
     * @return lista de contratos ativos
     */
    @Transactional(readOnly = true)
    public List<ContractResponse> findActiveByOrganization(UUID organizationId) {
        Objects.requireNonNull(organizationId, "Organization ID must not be null");
        return contractRepository.findActiveByOrganization(organizationId).stream()
                .map(ContractResponse::fromEntity)
                .toList();
    }

    /**
     * Lista contratos vinculados a um cliente.
     *
     * @param clientUserId ID do cliente
     * @return lista de contratos do cliente
     */
    @Transactional(readOnly = true)
    public List<ContractResponse> findByClient(UUID clientUserId) {
        Objects.requireNonNull(clientUserId, "Client user ID must not be null");
        return contractRepository.findByClientUserId(clientUserId).stream()
                .map(ContractResponse::fromEntity)
                .toList();
    }

    /**
     * Atualiza campos editáveis de um contrato ativo.
     * Campos nulos no request não são atualizados (atualização parcial).
     *
     * @param contractId ID do contrato
     * @param request    dados a atualizar
     * @return resposta com dados atualizados
     */
    @Transactional
    public ContractResponse updateContract(UUID contractId, UpdateContractRequest request) {
        Objects.requireNonNull(contractId, "Contract ID must not be null");
        Objects.requireNonNull(request, "UpdateContractRequest must not be null");

        Contract contract = findContractOrThrow(contractId);

        if (!contract.isActive()) {
            throw new ContractInactiveException(contractId);
        }

        if (request.title() != null) {
            contract.updateTitle(request.title());
        }
        if (request.currency() != null) {
            contract.updateCurrency(request.currency());
        }
        if (request.billingDay() != null) {
            contract.updateBillingDay(request.billingDay());
        }
        if (request.endDate() != null) {
            contract.updateEndDate(request.endDate());
        }
        if (request.retainerConfig() != null && contract.getType() == ContractType.RETAINER) {
            validateRetainerConfig(request.retainerConfig());
            contract.updateConfig(convertToMap(buildRetainerConfig(request.retainerConfig())));
        }

        Contract updated = contractRepository.save(contract);
        log.info("Contract updated: id={}", updated.getId());

        return ContractResponse.fromEntity(updated);
    }

    /**
     * Pausa um contrato ativo.
     *
     * @param contractId ID do contrato
     * @return resposta com contrato pausado
     */
    @Transactional
    public ContractResponse pauseContract(UUID contractId) {
        Contract contract = findContractOrThrow(contractId);

        if (!contract.isActive()) {
            throw new ContractInactiveException(contractId);
        }

        contract.changeStatus(ContractStatus.PAUSED);
        Contract saved = contractRepository.save(contract);
        log.info("Contract paused: id={}", saved.getId());

        return ContractResponse.fromEntity(saved);
    }

    /**
     * Reativa um contrato pausado.
     *
     * @param contractId ID do contrato
     * @return resposta com contrato reativado
     */
    @Transactional
    public ContractResponse resumeContract(UUID contractId) {
        Contract contract = findContractOrThrow(contractId);

        if (contract.getStatus() != ContractStatus.PAUSED) {
            throw new ContractInactiveException(contractId);
        }

        contract.changeStatus(ContractStatus.ACTIVE);
        Contract saved = contractRepository.save(contract);
        log.info("Contract resumed: id={}", saved.getId());

        return ContractResponse.fromEntity(saved);
    }

    /**
     * Encerra um contrato (pode estar ativo ou pausado).
     *
     * @param contractId ID do contrato
     * @return resposta com contrato encerrado
     */
    @Transactional
    public ContractResponse terminateContract(UUID contractId) {
        Contract contract = findContractOrThrow(contractId);

        if (contract.getStatus() == ContractStatus.TERMINATED) {
            throw new ContractInactiveException(contractId);
        }

        contract.changeStatus(ContractStatus.TERMINATED);
        Contract saved = contractRepository.save(contract);
        log.info("Contract terminated: id={}", saved.getId());

        return ContractResponse.fromEntity(saved);
    }

    // --- Métodos internos ---

    private Contract findContractOrThrow(UUID contractId) {
        return contractRepository.findById(contractId)
                .orElseThrow(() -> new ContractNotFoundException(contractId));
    }

    private void validateConfigForType(CreateContractRequest request) {
        if (request.type() == ContractType.RETAINER) {
            if (request.retainerConfig() == null) {
                throw new InvalidContractConfigException(
                        "Retainer config is required for RETAINER contract type");
            }
            validateRetainerConfig(request.retainerConfig());
        }
    }

    private void validateRetainerConfig(RetainerConfigRequest config) {
        if (Boolean.TRUE.equals(config.overageAllowed()) && config.overageRate() == null) {
            throw new InvalidContractConfigException(
                    "Overage rate is required when overage is allowed");
        }
    }

    private Map<String, Object> buildConfigMap(CreateContractRequest request) {
        if (request.type() == ContractType.RETAINER) {
            RetainerConfig retainerConfig = buildRetainerConfig(request.retainerConfig());
            return convertToMap(retainerConfig);
        }
        return Map.of();
    }

    private RetainerConfig buildRetainerConfig(RetainerConfigRequest request) {
        return RetainerConfig.builder()
                .monthlyHours(request.monthlyHours())
                .hourlyRate(request.hourlyRate())
                .rolloverPolicy(request.rolloverPolicy())
                .alertThreshold(request.alertThreshold() != null ? request.alertThreshold() : 80)
                .overageAllowed(request.overageAllowed())
                .overageRate(request.overageRate())
                .build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToMap(Object config) {
        return objectMapper.convertValue(config, Map.class);
    }
}
