package com.contractpulse;

import com.contractpulse.auth.JwtSupabaseFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: verifica que o contexto Spring carrega corretamente.
 * O JwtSupabaseFilter é mockado porque depende de conexão externa
 * ao JWKS endpoint do Supabase, indisponível no CI.
 */
@SpringBootTest
@ActiveProfiles("test")
class ContractPulseApplicationTests {

    @MockBean
    private JwtSupabaseFilter jwtSupabaseFilter;

    @Test
    void contextLoads() {
    }
}
