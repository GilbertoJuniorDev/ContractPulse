package com.contractpulse.contract.model;

/**
 * Política de rollover para horas não utilizadas em contratos Retainer.
 * <ul>
 *   <li>{@link #EXPIRE} — Horas não usadas expiram ao final do mês</li>
 *   <li>{@link #ACCUMULATE} — Horas não usadas acumulam para o próximo mês</li>
 *   <li>{@link #PARTIAL} — Percentual parcial das horas acumula (configurável)</li>
 * </ul>
 */
public enum RolloverPolicy {
    EXPIRE,
    ACCUMULATE,
    PARTIAL
}
