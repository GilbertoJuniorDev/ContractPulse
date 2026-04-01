-- ContractPulse — Migration: Altera role padrão de PROVIDER para CLIENT.
-- Novos usuários criados via registro comum serão CLIENT por padrão.

ALTER TABLE users ALTER COLUMN role SET DEFAULT 'CLIENT';
