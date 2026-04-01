package com.contractpulse.scheduler;

import com.contractpulse.timeentry.ApprovalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Job agendado que gerencia o ciclo semanal de aprovação de lançamentos.
 *
 * <ul>
 *   <li>Domingo 23:59 — promove SUBMITTED → PENDING_APPROVAL</li>
 *   <li>Diariamente 08:00 — auto-aprova PENDING_APPROVAL com mais de 48h</li>
 * </ul>
 */
@Component
public class WeeklyApprovalJob {

    private static final Logger log = LoggerFactory.getLogger(WeeklyApprovalJob.class);

    /**
     * UUID fixo representando o "sistema" em auto-aprovações.
     * Permite rastreabilidade no reviewer_id.
     */
    static final UUID SYSTEM_USER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final ApprovalService approvalService;

    public WeeklyApprovalJob(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    /**
     * Executa todo domingo às 23:59 — promove lançamentos SUBMITTED para PENDING_APPROVAL.
     * Cron: segundo minuto hora dia-do-mês mês dia-da-semana
     */
    @Scheduled(cron = "0 59 23 * * SUN", zone = "America/Sao_Paulo")
    public void promoteSubmittedEntries() {
        log.info("WeeklyApprovalJob: starting SUBMITTED → PENDING_APPROVAL promotion");
        try {
            int count = approvalService.promoteSubmittedToPendingApproval();
            log.info("WeeklyApprovalJob: promoted {} entries", count);
        } catch (Exception e) {
            log.error("WeeklyApprovalJob: error promoting entries", e);
        }
    }

    /**
     * Executa diariamente às 08:00 — auto-aprova lançamentos pendentes há mais de 48h.
     */
    @Scheduled(cron = "0 0 8 * * *", zone = "America/Sao_Paulo")
    public void autoApproveExpiredEntries() {
        log.info("WeeklyApprovalJob: starting auto-approve of expired entries");
        try {
            int count = approvalService.autoApproveExpiredEntries(SYSTEM_USER_ID);
            log.info("WeeklyApprovalJob: auto-approved {} entries", count);
        } catch (Exception e) {
            log.error("WeeklyApprovalJob: error auto-approving entries", e);
        }
    }
}
