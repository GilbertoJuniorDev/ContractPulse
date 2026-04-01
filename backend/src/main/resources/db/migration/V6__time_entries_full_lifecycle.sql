-- ContractPulse — Migration: Evolução do ciclo de vida de lançamentos de horas
-- Adiciona suporte ao fluxo completo: DRAFT → SUBMITTED → PENDING_APPROVAL → APPROVED | DISPUTED → INVOICED
-- Adiciona coluna ai_summary para resumo gerado por IA.

-- Coluna para resumo gerado por IA (linguagem não-técnica)
ALTER TABLE time_entries ADD COLUMN IF NOT EXISTS ai_summary TEXT;

-- Atualiza default de status de PENDING para DRAFT (novos lançamentos nascem como rascunho)
ALTER TABLE time_entries ALTER COLUMN status SET DEFAULT 'DRAFT';

-- Migra registros PENDING existentes para PENDING_APPROVAL (compatibilidade)
UPDATE time_entries SET status = 'PENDING_APPROVAL' WHERE status = 'PENDING';
