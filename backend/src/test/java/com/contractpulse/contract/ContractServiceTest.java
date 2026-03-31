package com.contractpulse.contract;

import com.contractpulse.contract.dto.*;
import com.contractpulse.contract.exception.ContractInactiveException;
import com.contractpulse.contract.exception.ContractNotFoundException;
import com.contractpulse.contract.exception.InvalidContractConfigException;
import com.contractpulse.contract.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para o ContractService.
 * Cobertura mínima alvo: 80%.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ContractService")
class ContractServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ContractService contractService;

    private UUID organizationId;
    private UUID clientUserId;
    private UUID contractId;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        clientUserId = UUID.randomUUID();
        contractId = UUID.randomUUID();
    }

    private Contract buildActiveRetainerContract() {
        Map<String, Object> config = new HashMap<>();
        config.put("monthlyHours", 40);
        config.put("hourlyRate", 150.0);
        config.put("rolloverPolicy", "EXPIRE");
        config.put("alertThreshold", 80);
        config.put("overageAllowed", false);

        return Contract.builder()
                .id(contractId)
                .organizationId(organizationId)
                .clientUserId(clientUserId)
                .title("Test Contract")
                .type(ContractType.RETAINER)
                .currency(ContractCurrency.BRL)
                .billingDay(15)
                .startDate(LocalDate.of(2025, 1, 1))
                .status(ContractStatus.ACTIVE)
                .config(config)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();
    }

    private RetainerConfigRequest buildRetainerConfigRequest() {
        return new RetainerConfigRequest(
                40,
                new BigDecimal("150.00"),
                RolloverPolicy.EXPIRE,
                80,
                false,
                null
        );
    }

    private CreateContractRequest buildCreateRequest(RetainerConfigRequest retainerConfig) {
        return new CreateContractRequest(
                organizationId,
                clientUserId,
                "New Retainer Contract",
                ContractType.RETAINER,
                ContractCurrency.BRL,
                15,
                LocalDate.of(2025, 1, 1),
                null,
                retainerConfig
        );
    }

    /**
     * Simula o save do repository retornando a entidade com ID e timestamps preenchidos.
     */
    private void mockRepositorySave() {
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> {
            Contract saved = invocation.getArgument(0);
            return Contract.builder()
                    .id(saved.getId() != null ? saved.getId() : UUID.randomUUID())
                    .organizationId(saved.getOrganizationId())
                    .clientUserId(saved.getClientUserId())
                    .title(saved.getTitle())
                    .type(saved.getType())
                    .currency(saved.getCurrency())
                    .billingDay(saved.getBillingDay())
                    .startDate(saved.getStartDate())
                    .endDate(saved.getEndDate())
                    .status(saved.getStatus())
                    .config(saved.getConfig())
                    .createdAt(saved.getCreatedAt() != null ? saved.getCreatedAt() : ZonedDateTime.now())
                    .updatedAt(ZonedDateTime.now())
                    .build();
        });
    }

    // --- Criação ---

    @Nested
    @DisplayName("createContract")
    class CreateContract {

        @Test
        @DisplayName("deve criar contrato Retainer com configuração válida")
        void shouldCreateRetainerContract() {
            var request = buildCreateRequest(buildRetainerConfigRequest());
            mockRepositorySave();

            ContractResponse response = contractService.createContract(request);

            assertThat(response).isNotNull();
            assertThat(response.id()).isNotNull();
            assertThat(response.title()).isEqualTo("New Retainer Contract");
            assertThat(response.type()).isEqualTo(ContractType.RETAINER);
            assertThat(response.status()).isEqualTo(ContractStatus.ACTIVE);
            assertThat(response.currency()).isEqualTo(ContractCurrency.BRL);
            assertThat(response.billingDay()).isEqualTo(15);
            assertThat(response.startDate()).isEqualTo(LocalDate.of(2025, 1, 1));
            assertThat(response.organizationId()).isEqualTo(organizationId);
            assertThat(response.clientUserId()).isEqualTo(clientUserId);
            assertThat(response.config()).containsKey("monthlyHours");
            assertThat(response.config()).containsKey("hourlyRate");
            assertThat(response.config()).containsKey("rolloverPolicy");

            verify(contractRepository).save(any(Contract.class));
        }

        @Test
        @DisplayName("deve criar contrato Retainer com overage permitido")
        void shouldCreateRetainerWithOverage() {
            var retainerConfig = new RetainerConfigRequest(
                    40, new BigDecimal("150.00"), RolloverPolicy.ACCUMULATE,
                    70, true, new BigDecimal("200.00")
            );
            var request = new CreateContractRequest(
                    organizationId, clientUserId, "Overage Contract",
                    ContractType.RETAINER, ContractCurrency.USD, 1,
                    LocalDate.of(2025, 3, 1), null, retainerConfig
            );
            mockRepositorySave();

            ContractResponse response = contractService.createContract(request);

            assertThat(response.currency()).isEqualTo(ContractCurrency.USD);
            assertThat(response.billingDay()).isEqualTo(1);
            assertThat(response.config()).containsEntry("overageAllowed", true);
            assertThat(response.config()).containsKey("overageRate");
            assertThat(response.config()).containsEntry("monthlyHours", 40);

            verify(contractRepository).save(any(Contract.class));
        }

        @Test
        @DisplayName("deve criar contrato com endDate definido")
        void shouldCreateContractWithEndDate() {
            var retainerConfig = buildRetainerConfigRequest();
            var request = new CreateContractRequest(
                    organizationId, clientUserId, "Fixed Term",
                    ContractType.RETAINER, ContractCurrency.BRL, 10,
                    LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), retainerConfig
            );
            mockRepositorySave();

            ContractResponse response = contractService.createContract(request);

            assertThat(response.startDate()).isEqualTo(LocalDate.of(2025, 1, 1));
            assertThat(response.endDate()).isEqualTo(LocalDate.of(2025, 12, 31));

            verify(contractRepository).save(any(Contract.class));
        }

        @Test
        @DisplayName("deve criar contrato com alertThreshold padrão quando não informado")
        void shouldCreateWithDefaultAlertThreshold() {
            var retainerConfig = new RetainerConfigRequest(
                    40, new BigDecimal("150.00"), RolloverPolicy.EXPIRE,
                    null, false, null
            );
            var request = buildCreateRequest(retainerConfig);
            mockRepositorySave();

            ContractResponse response = contractService.createContract(request);

            assertThat(response.config()).containsEntry("alertThreshold", 80);
        }

        @Test
        @DisplayName("deve falhar ao criar Retainer sem configuração")
        void shouldFailWithoutRetainerConfig() {
            var request = new CreateContractRequest(
                    organizationId, clientUserId, "No Config",
                    ContractType.RETAINER, ContractCurrency.BRL, 15,
                    LocalDate.of(2025, 1, 1), null, null
            );

            assertThatThrownBy(() -> contractService.createContract(request))
                    .isInstanceOf(InvalidContractConfigException.class)
                    .hasMessageContaining("RETAINER");

            verify(contractRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve falhar ao criar Retainer com overage mas sem taxa")
        void shouldFailOverageWithoutRate() {
            var retainerConfig = new RetainerConfigRequest(
                    40, new BigDecimal("150.00"), RolloverPolicy.EXPIRE,
                    80, true, null
            );
            var request = buildCreateRequest(retainerConfig);

            assertThatThrownBy(() -> contractService.createContract(request))
                    .isInstanceOf(InvalidContractConfigException.class)
                    .hasMessageContaining("Overage rate");

            verify(contractRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando request é null — fail fast")
        void shouldThrowWhenRequestIsNull() {
            assertThatThrownBy(() -> contractService.createContract(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("CreateContractRequest must not be null");

            verify(contractRepository, never()).save(any());
        }
    }

    // --- Busca por ID ---

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("deve retornar contrato com mapeamento completo")
        void shouldReturnContractWithFullMapping() {
            Contract existing = buildActiveRetainerContract();
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(existing));

            ContractResponse response = contractService.findById(contractId);

            assertThat(response.id()).isEqualTo(contractId);
            assertThat(response.title()).isEqualTo("Test Contract");
            assertThat(response.type()).isEqualTo(ContractType.RETAINER);
            assertThat(response.currency()).isEqualTo(ContractCurrency.BRL);
            assertThat(response.billingDay()).isEqualTo(15);
            assertThat(response.startDate()).isEqualTo(LocalDate.of(2025, 1, 1));
            assertThat(response.status()).isEqualTo(ContractStatus.ACTIVE);
            assertThat(response.organizationId()).isEqualTo(organizationId);
            assertThat(response.clientUserId()).isEqualTo(clientUserId);
            assertThat(response.config()).isNotNull();
            assertThat(response.createdAt()).isNotNull();
            assertThat(response.updatedAt()).isNotNull();
        }

        @Test
        @DisplayName("deve lançar exceção quando contrato não existe")
        void shouldThrowWhenContractNotFound() {
            UUID missingId = UUID.randomUUID();
            when(contractRepository.findById(missingId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contractService.findById(missingId))
                    .isInstanceOf(ContractNotFoundException.class)
                    .hasMessageContaining(missingId.toString());
        }
    }

    // --- Listagem por organização ---

    @Nested
    @DisplayName("findByOrganization")
    class FindByOrganization {

        @Test
        @DisplayName("deve retornar contratos da organização")
        void shouldReturnOrganizationContracts() {
            Contract existing = buildActiveRetainerContract();
            when(contractRepository.findByOrganizationId(organizationId))
                    .thenReturn(List.of(existing));

            List<ContractResponse> responses = contractService.findByOrganization(organizationId);

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).organizationId()).isEqualTo(organizationId);
            assertThat(responses.get(0).title()).isEqualTo("Test Contract");
        }

        @Test
        @DisplayName("deve retornar múltiplos contratos da organização")
        void shouldReturnMultipleContracts() {
            Contract contract1 = buildActiveRetainerContract();
            Contract contract2 = Contract.builder()
                    .id(UUID.randomUUID())
                    .organizationId(organizationId)
                    .clientUserId(UUID.randomUUID())
                    .title("Second Contract")
                    .type(ContractType.RETAINER)
                    .currency(ContractCurrency.USD)
                    .billingDay(1)
                    .startDate(LocalDate.of(2025, 6, 1))
                    .status(ContractStatus.ACTIVE)
                    .config(Map.of())
                    .createdAt(ZonedDateTime.now())
                    .updatedAt(ZonedDateTime.now())
                    .build();

            when(contractRepository.findByOrganizationId(organizationId))
                    .thenReturn(List.of(contract1, contract2));

            List<ContractResponse> responses = contractService.findByOrganization(organizationId);

            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).title()).isEqualTo("Test Contract");
            assertThat(responses.get(1).title()).isEqualTo("Second Contract");
        }

        @Test
        @DisplayName("deve retornar lista vazia quando organização não tem contratos")
        void shouldReturnEmptyListWhenNoContracts() {
            when(contractRepository.findByOrganizationId(organizationId))
                    .thenReturn(Collections.emptyList());

            List<ContractResponse> responses = contractService.findByOrganization(organizationId);

            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("deve lançar exceção quando organizationId é null")
        void shouldThrowWhenOrganizationIdIsNull() {
            assertThatThrownBy(() -> contractService.findByOrganization(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Organization ID must not be null");
        }
    }

    // --- Listagem ativos ---

    @Nested
    @DisplayName("findActiveByOrganization")
    class FindActiveByOrganization {

        @Test
        @DisplayName("deve retornar apenas contratos ativos")
        void shouldReturnActiveOnly() {
            Contract existing = buildActiveRetainerContract();
            when(contractRepository.findActiveByOrganization(organizationId))
                    .thenReturn(List.of(existing));

            List<ContractResponse> responses = contractService.findActiveByOrganization(organizationId);

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).status()).isEqualTo(ContractStatus.ACTIVE);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há contratos ativos")
        void shouldReturnEmptyListWhenNoActiveContracts() {
            when(contractRepository.findActiveByOrganization(organizationId))
                    .thenReturn(Collections.emptyList());

            List<ContractResponse> responses = contractService.findActiveByOrganization(organizationId);

            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("deve lançar exceção quando organizationId é null")
        void shouldThrowWhenOrganizationIdIsNull() {
            assertThatThrownBy(() -> contractService.findActiveByOrganization(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Organization ID must not be null");
        }
    }

    // --- Listagem por cliente ---

    @Nested
    @DisplayName("findByClient")
    class FindByClient {

        @Test
        @DisplayName("deve retornar contratos do cliente")
        void shouldReturnClientContracts() {
            Contract existing = buildActiveRetainerContract();
            when(contractRepository.findByClientUserId(clientUserId))
                    .thenReturn(List.of(existing));

            List<ContractResponse> responses = contractService.findByClient(clientUserId);

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).clientUserId()).isEqualTo(clientUserId);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando cliente não tem contratos")
        void shouldReturnEmptyListWhenNoClientContracts() {
            when(contractRepository.findByClientUserId(clientUserId))
                    .thenReturn(Collections.emptyList());

            List<ContractResponse> responses = contractService.findByClient(clientUserId);

            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("deve lançar exceção quando clientUserId é null")
        void shouldThrowWhenClientUserIdIsNull() {
            assertThatThrownBy(() -> contractService.findByClient(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Client user ID must not be null");
        }
    }

    // --- Atualização ---

    @Nested
    @DisplayName("updateContract")
    class UpdateContract {

        @Test
        @DisplayName("deve atualizar título do contrato ativo")
        void shouldUpdateTitle() {
            Contract existing = buildActiveRetainerContract();
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(existing));
            when(contractRepository.save(any(Contract.class))).thenAnswer(inv -> inv.getArgument(0));

            var request = new UpdateContractRequest("New Title", null, null, null, null);

            ContractResponse response = contractService.updateContract(contractId, request);

            assertThat(response.title()).isEqualTo("New Title");
            verify(contractRepository).save(existing);
        }

        @Test
        @DisplayName("deve atualizar billing day")
        void shouldUpdateBillingDay() {
            Contract existing = buildActiveRetainerContract();
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(existing));
            when(contractRepository.save(any(Contract.class))).thenAnswer(inv -> inv.getArgument(0));

            var request = new UpdateContractRequest(null, null, 20, null, null);

            ContractResponse response = contractService.updateContract(contractId, request);

            assertThat(response.billingDay()).isEqualTo(20);
            verify(contractRepository).save(existing);
        }

        @Test
        @DisplayName("deve atualizar moeda do contrato")
        void shouldUpdateCurrency() {
            Contract existing = buildActiveRetainerContract();
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(existing));
            when(contractRepository.save(any(Contract.class))).thenAnswer(inv -> inv.getArgument(0));

            var request = new UpdateContractRequest(null, ContractCurrency.EUR, null, null, null);

            ContractResponse response = contractService.updateContract(contractId, request);

            assertThat(response.currency()).isEqualTo(ContractCurrency.EUR);
            verify(contractRepository).save(existing);
        }

        @Test
        @DisplayName("deve atualizar endDate do contrato")
        void shouldUpdateEndDate() {
            Contract existing = buildActiveRetainerContract();
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(existing));
            when(contractRepository.save(any(Contract.class))).thenAnswer(inv -> inv.getArgument(0));

            LocalDate newEndDate = LocalDate.of(2026, 12, 31);
            var request = new UpdateContractRequest(null, null, null, newEndDate, null);

            ContractResponse response = contractService.updateContract(contractId, request);

            assertThat(response.endDate()).isEqualTo(newEndDate);
            verify(contractRepository).save(existing);
        }

        @Test
        @DisplayName("deve atualizar configuração Retainer")
        void shouldUpdateRetainerConfig() {
            Contract existing = buildActiveRetainerContract();
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(existing));
            when(contractRepository.save(any(Contract.class))).thenAnswer(inv -> inv.getArgument(0));

            var newConfig = new RetainerConfigRequest(
                    60, new BigDecimal("180.00"), RolloverPolicy.ACCUMULATE,
                    90, false, null
            );
            var request = new UpdateContractRequest(null, null, null, null, newConfig);

            ContractResponse response = contractService.updateContract(contractId, request);

            assertThat(response.config()).containsEntry("monthlyHours", 60);
            assertThat(response.config()).containsEntry("alertThreshold", 90);
            verify(contractRepository).save(existing);
        }

        @Test
        @DisplayName("deve atualizar múltiplos campos simultaneamente")
        void shouldUpdateMultipleFieldsAtOnce() {
            Contract existing = buildActiveRetainerContract();
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(existing));
            when(contractRepository.save(any(Contract.class))).thenAnswer(inv -> inv.getArgument(0));

            var request = new UpdateContractRequest(
                    "Updated Title", ContractCurrency.USD, 25, null, null
            );

            ContractResponse response = contractService.updateContract(contractId, request);

            assertThat(response.title()).isEqualTo("Updated Title");
            assertThat(response.currency()).isEqualTo(ContractCurrency.USD);
            assertThat(response.billingDay()).isEqualTo(25);
            verify(contractRepository).save(existing);
        }

        @Test
        @DisplayName("deve falhar ao atualizar contrato pausado")
        void shouldFailForPausedContract() {
            Contract existing = buildActiveRetainerContract();
            existing.changeStatus(ContractStatus.PAUSED);
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(existing));

            var request = new UpdateContractRequest("Try", null, null, null, null);

            assertThatThrownBy(() -> contractService.updateContract(contractId, request))
                    .isInstanceOf(ContractInactiveException.class);

            verify(contractRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve falhar ao atualizar contrato encerrado")
        void shouldFailForTerminatedContract() {
            Contract existing = buildActiveRetainerContract();
            existing.changeStatus(ContractStatus.TERMINATED);
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(existing));

            var request = new UpdateContractRequest("Try", null, null, null, null);

            assertThatThrownBy(() -> contractService.updateContract(contractId, request))
                    .isInstanceOf(ContractInactiveException.class);

            verify(contractRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando contrato não existe na atualização")
        void shouldThrowWhenContractNotFoundOnUpdate() {
            UUID missingId = UUID.randomUUID();
            when(contractRepository.findById(missingId)).thenReturn(Optional.empty());

            var request = new UpdateContractRequest("Try", null, null, null, null);

            assertThatThrownBy(() -> contractService.updateContract(missingId, request))
                    .isInstanceOf(ContractNotFoundException.class);

            verify(contractRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando contractId é null na atualização")
        void shouldThrowWhenContractIdIsNull() {
            var request = new UpdateContractRequest("Try", null, null, null, null);

            assertThatThrownBy(() -> contractService.updateContract(null, request))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Contract ID must not be null");

            verify(contractRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando request é null na atualização")
        void shouldThrowWhenRequestIsNull() {
            assertThatThrownBy(() -> contractService.updateContract(contractId, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("UpdateContractRequest must not be null");

            verify(contractRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve falhar ao atualizar retainer config com overage sem taxa")
        void shouldFailOnRetainerConfigUpdateWithOverageWithoutRate() {
            Contract existing = buildActiveRetainerContract();
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(existing));

            var invalidConfig = new RetainerConfigRequest(
                    60, new BigDecimal("180.00"), RolloverPolicy.EXPIRE,
                    80, true, null
            );
            var request = new UpdateContractRequest(null, null, null, null, invalidConfig);

            assertThatThrownBy(() -> contractService.updateContract(contractId, request))
                    .isInstanceOf(InvalidContractConfigException.class)
                    .hasMessageContaining("Overage rate");

            verify(contractRepository, never()).save(any());
        }
    }

    // --- Pausar contrato ---

    @Nested
    @DisplayName("pauseContract")
    class PauseContract {

        @Test
        @DisplayName("deve pausar contrato ativo")
        void shouldPauseActiveContract() {
            Contract existing = buildActiveRetainerContract();
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(existing));
            when(contractRepository.save(any(Contract.class))).thenAnswer(inv -> inv.getArgument(0));

            ContractResponse response = contractService.pauseContract(contractId);

            assertThat(response.status()).isEqualTo(ContractStatus.PAUSED);
            verify(contractRepository).save(existing);
        }

        @Test
        @DisplayName("deve falhar ao pausar contrato já pausado")
        void shouldFailForAlreadyPaused() {
            Contract existing = buildActiveRetainerContract();
            existing.changeStatus(ContractStatus.PAUSED);
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> contractService.pauseContract(contractId))
                    .isInstanceOf(ContractInactiveException.class);

            verify(contractRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve falhar ao pausar contrato encerrado")
        void shouldFailForTerminatedContract() {
            Contract existing = buildActiveRetainerContract();
            existing.changeStatus(ContractStatus.TERMINATED);
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> contractService.pauseContract(contractId))
                    .isInstanceOf(ContractInactiveException.class);

            verify(contractRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando contrato não existe ao pausar")
        void shouldThrowWhenContractNotFoundOnPause() {
            UUID missingId = UUID.randomUUID();
            when(contractRepository.findById(missingId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contractService.pauseContract(missingId))
                    .isInstanceOf(ContractNotFoundException.class);

            verify(contractRepository, never()).save(any());
        }
    }

    // --- Reativar contrato ---

    @Nested
    @DisplayName("resumeContract")
    class ResumeContract {

        @Test
        @DisplayName("deve reativar contrato pausado")
        void shouldResumeContract() {
            Contract existing = buildActiveRetainerContract();
            existing.changeStatus(ContractStatus.PAUSED);
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(existing));
            when(contractRepository.save(any(Contract.class))).thenAnswer(inv -> inv.getArgument(0));

            ContractResponse response = contractService.resumeContract(contractId);

            assertThat(response.status()).isEqualTo(ContractStatus.ACTIVE);
            verify(contractRepository).save(existing);
        }

        @Test
        @DisplayName("deve falhar ao reativar contrato já ativo")
        void shouldFailForAlreadyActive() {
            Contract existing = buildActiveRetainerContract();
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> contractService.resumeContract(contractId))
                    .isInstanceOf(ContractInactiveException.class);

            verify(contractRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve falhar ao reativar contrato encerrado")
        void shouldFailForTerminatedContract() {
            Contract existing = buildActiveRetainerContract();
            existing.changeStatus(ContractStatus.TERMINATED);
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> contractService.resumeContract(contractId))
                    .isInstanceOf(ContractInactiveException.class);

            verify(contractRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando contrato não existe ao reativar")
        void shouldThrowWhenContractNotFoundOnResume() {
            UUID missingId = UUID.randomUUID();
            when(contractRepository.findById(missingId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contractService.resumeContract(missingId))
                    .isInstanceOf(ContractNotFoundException.class);

            verify(contractRepository, never()).save(any());
        }
    }

    // --- Encerrar contrato ---

    @Nested
    @DisplayName("terminateContract")
    class TerminateContract {

        @Test
        @DisplayName("deve encerrar contrato ativo")
        void shouldTerminateActiveContract() {
            Contract existing = buildActiveRetainerContract();
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(existing));
            when(contractRepository.save(any(Contract.class))).thenAnswer(inv -> inv.getArgument(0));

            ContractResponse response = contractService.terminateContract(contractId);

            assertThat(response.status()).isEqualTo(ContractStatus.TERMINATED);
            verify(contractRepository).save(existing);
        }

        @Test
        @DisplayName("deve encerrar contrato pausado")
        void shouldTerminatePausedContract() {
            Contract existing = buildActiveRetainerContract();
            existing.changeStatus(ContractStatus.PAUSED);
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(existing));
            when(contractRepository.save(any(Contract.class))).thenAnswer(inv -> inv.getArgument(0));

            ContractResponse response = contractService.terminateContract(contractId);

            assertThat(response.status()).isEqualTo(ContractStatus.TERMINATED);
            verify(contractRepository).save(existing);
        }

        @Test
        @DisplayName("deve falhar ao encerrar contrato já encerrado")
        void shouldFailForAlreadyTerminated() {
            Contract existing = buildActiveRetainerContract();
            existing.changeStatus(ContractStatus.TERMINATED);
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> contractService.terminateContract(contractId))
                    .isInstanceOf(ContractInactiveException.class);

            verify(contractRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando contrato não existe ao encerrar")
        void shouldThrowWhenContractNotFoundOnTerminate() {
            UUID missingId = UUID.randomUUID();
            when(contractRepository.findById(missingId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contractService.terminateContract(missingId))
                    .isInstanceOf(ContractNotFoundException.class);

            verify(contractRepository, never()).save(any());
        }
    }
}
