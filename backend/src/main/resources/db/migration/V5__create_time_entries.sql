-- ContractPulse — Migration: Tabela de lançamentos de horas
-- Registra horas lançadas pelo provider e aprovadas/disputadas pelo client.

CREATE TABLE time_entries (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contract_id     UUID NOT NULL REFERENCES contracts(id) ON DELETE CASCADE,
    user_id         UUID NOT NULL REFERENCES users(id),
    description     VARCHAR(500) NOT NULL,
    hours           NUMERIC(6, 2) NOT NULL CHECK (hours > 0),
    entry_date      DATE NOT NULL,
    status          VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    reviewer_id     UUID REFERENCES users(id),
    reviewed_at     TIMESTAMP WITH TIME ZONE,
    dispute_reason  VARCHAR(500),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_time_entries_contract_id ON time_entries(contract_id);
CREATE INDEX idx_time_entries_user_id     ON time_entries(user_id);
CREATE INDEX idx_time_entries_status      ON time_entries(status);
CREATE INDEX idx_time_entries_entry_date  ON time_entries(entry_date);
