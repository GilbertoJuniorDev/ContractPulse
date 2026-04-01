# ContractPulse — SaaS de Gestão de Contratos Transparentes

> Plataforma que cria confiança entre freelancers/agências e clientes através de visibilidade total sobre horas, entregas, métricas e pagamentos — em qualquer modelo de contrato variável.

## Prioridades
1. Sempre seguir as regras do indice 15 de Padrões de Engenharia de Software, garantindo código limpo, modular e testável
2. Sempre analisar o código para garantir que não há vazamento de abstração ou acoplamento indevido
3. Sempre implementar os testes unitários e de integração necessários para garantir a robustez do sistema
4. Lembre se que não estou usando docker, estou usando o supabase.
---

## Índice

1. [Visão Geral](#1-visão-geral)
2. [Stack Tecnológica](#2-stack-tecnológica)
3. [Arquitetura do Sistema](#3-arquitetura-do-sistema)
4. [Modelos de Contrato](#4-modelos-de-contrato)
5. [Regras de Negócio](#5-regras-de-negócio)
6. [Entidades do Domínio](#6-entidades-do-domínio)
7. [Módulos do Backend (Java)](#7-módulos-do-backend-java)
8. [Estrutura do Frontend (Next.js)](#8-estrutura-do-frontend-nextjs)
9. [Autenticação & Autorização](#9-autenticação--autorização)
10. [Banco de Dados (Supabase/PostgreSQL)](#10-banco-de-dados-supabasepostgresql)
11. [Integrações Externas](#11-integrações-externas)
12. [Funcionalidades por Perfil](#12-funcionalidades-por-perfil)
13. [Health Score — Métrica Proprietária](#13-health-score--métrica-proprietária)
14. [Roadmap MVP → V1 → V2](#14-roadmap-mvp--v1--v2)
15. [Convenções e Boas Práticas](#15-convenções-e-boas-práticas)

---

## 1. Visão Geral

### Problema
Contratos variáveis (banco de horas, hora excedente, profit sharing) geram desconfiança porque o cliente não tem visibilidade do consumo e o prestador não tem proteção formal sobre seu esforço.

### Solução
Um SaaS que atua como "camada de transparência" entre as duas partes: o prestador lança, o cliente aprova, ambos têm dashboards dedicados, e o sistema cuida das regras financeiras automaticamente.

### Personas

| Persona | Papel no sistema | Dor principal |
|---|---|---|
| Freelancer / Dev | `PROVIDER` | Não receber por horas extras, sem histórico de confiabilidade |
| Agência | `PROVIDER` | Gerenciar múltiplos clientes com modelos diferentes |
| Cliente (PJ/PF) | `CLIENT` | Surpresas na fatura, sem visibilidade do burn rate |
| Sócio / Gestor | `CLIENT_VIEWER` | Quer ver progresso sem acesso operacional |

---

## 2. Stack Tecnológica

### Backend
- **Linguagem:** Java 21 (LTS)
- **Framework:** Spring Boot 3.x
- **ORM:** Spring Data JPA + Hibernate
- **Migrations:** Flyway
- **Build:** Maven ou Gradle (Gradle preferido)
- **Testes:** JUnit 5 + Mockito + Testcontainers
- **Documentação API:** SpringDoc OpenAPI (Swagger UI)
- **Autenticação:** Spring Security + validação de JWT do Supabase

### Frontend
- **Framework:** Next.js 14+ (App Router)
- **Linguagem:** TypeScript
- **Estilização:** Tailwind CSS + Shadcn/UI
- **Estado global:** Zustand ou React Query (TanStack Query)
- **Formulários:** React Hook Form + Zod
- **Gráficos:** Recharts

### Banco de Dados & Auth
- **Banco:** Supabase (PostgreSQL gerenciado)
- **Auth:** Supabase Auth
- **Storage:** Supabase Storage (para snapshots/evidências)
- **Realtime:** Supabase Realtime (notificações ao vivo no dashboard)

### Infraestrutura / DevOps
- **Containerização:** Docker + Docker Compose (local)
- **Deploy Backend:** Railway, Render ou Fly.io
- **Deploy Frontend:** Vercel
- **CI/CD:** GitHub Actions
- **Monitoramento:** Sentry (erros) + LogRocket (frontend)

---

## 3. Arquitetura do Sistema

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENTE (Browser)                    │
│                     Next.js 14 (Vercel)                     │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTPS / REST + Supabase Realtime
          ┌──────────────┴──────────────┐
          │                             │
          ▼                             ▼
┌─────────────────┐           ┌──────────────────┐
│   Supabase Auth │           │  Spring Boot API  │
│  (JWT/Google)   │           │   (Railway/Fly)   │
└────────┬────────┘           └────────┬─────────┘
         │                            │
         │                   ┌────────▼─────────┐
         │                   │   Supabase DB     │
         └──────────────────►│  (PostgreSQL)     │
                             └────────┬─────────┘
                                      │
                             ┌────────▼─────────┐
                             │ Supabase Storage  │
                             │  (Evidências,     │
                             │   Snapshots)      │
                             └──────────────────┘
```

### Fluxo de Autenticação
1. Usuário faz login via Supabase Auth (Google OAuth ou e-mail/senha)
2. Supabase retorna um JWT assinado
3. O frontend envia esse JWT no header `Authorization: Bearer <token>` para o backend Java
4. O backend valida o JWT usando a chave pública do Supabase (JWKS endpoint)
5. O `userId` extraído do JWT é usado para todas as operações autorizadas

---

## 4. Modelos de Contrato

### 4.1 Banco de Horas (Retainer)

```
ContractType: RETAINER
Campos:
  - monthly_hours: Integer          // horas contratadas por mês
  - hourly_rate: BigDecimal         // valor por hora
  - rollover_policy: Enum           // EXPIRE | ACCUMULATE | PARTIAL(%)
  - alert_threshold: Integer        // % de consumo para alerta (padrão: 80)
  - overage_allowed: Boolean        // permite ultrapassar?
  - overage_rate: BigDecimal?       // valor da hora excedente (se permitido)
```

**Regras:**
- Ao virar o mês, aplicar `rollover_policy`
- Ao atingir `alert_threshold`%, notificar ambas as partes
- Se `overage_allowed = false`, bloquear novos lançamentos ao atingir 100%

### 4.2 Hora Excedente (Fixed + Overage)

```
ContractType: FIXED_OVERAGE
Campos:
  - base_fee: BigDecimal            // valor fixo mensal
  - included_hours: Integer         // horas incluídas no fixo
  - overage_rate: BigDecimal        // valor por hora além do teto
  - overage_ceiling: Integer?       // teto máximo de horas extras (opcional)
```

**Regras:**
- Fatura base sempre gerada no dia X do mês
- Ao ultrapassar `included_hours`, cada hora adicional é faturada separadamente
- Se `overage_ceiling` definido e atingido, notificar e bloquear

### 4.3 Profit Sharing (Participação nos Resultados)

```
ContractType: PROFIT_SHARING
Campos:
  - base_fee: BigDecimal            // valor base reduzido
  - kpi_type: Enum                  // REVENUE | PROFIT | CUSTOM
  - kpi_target: BigDecimal          // meta de KPI
  - bonus_percentage: BigDecimal    // % de participação acima da meta
  - bonus_cap: BigDecimal?          // teto do bônus (opcional)
  - kpi_input_day: Integer          // dia do mês para input do KPI
```

**Regras:**
- KPI é inserido pelo cliente (ou via integração futura)
- O sistema calcula o bônus: `max(0, (kpi_actual - kpi_target) * bonus_percentage)`
- Evidências (snapshots) são obrigatórias antes de liberar o bônus
- Bônus pendente de aprovação do cliente após cálculo

---

## 5. Regras de Negócio

### 5.1 Ciclo de Vida de um Lançamento de Horas

```
DRAFT → SUBMITTED → PENDING_APPROVAL → APPROVED | DISPUTED → INVOICED
```

| Status | Quem age | Descrição |
|---|---|---|
| `DRAFT` | Provider | Rascunho, não visível ao cliente |
| `SUBMITTED` | Provider | Enviado para revisão |
| `PENDING_APPROVAL` | Client | Aguardando aprovação (após ciclo semanal) |
| `APPROVED` | Client | Horas confirmadas, entram no cálculo de fatura |
| `DISPUTED` | Client | Cliente contestou — abre thread de resolução |
| `INVOICED` | Sistema | Horas incluídas em fatura emitida |

### 5.2 Ciclo de Aprovação Semanal
- Todo domingo às 23:59, o sistema agrupa os lançamentos `SUBMITTED` da semana
- Gera um "Resumo Semanal" e envia para o cliente (e-mail / WhatsApp)
- O cliente tem até 48h para aprovar ou contestar
- Sem resposta em 48h → aprovação automática (configurável por contrato)

### 5.3 Geração de Faturas
- Fatura gerada automaticamente no `billing_day` do contrato
- Composta por: horas aprovadas + base fee + overages + bônus profit sharing
- Status da fatura: `DRAFT → SENT → PAID | OVERDUE`
- Integração com Asaas/Stripe para cobrança automatizada (V2)

### 5.4 Conversão de Moeda
- Contratos podem ser indexados a: `BRL | USD | EUR`
- Se o contrato é em moeda estrangeira, na data de fechamento o sistema consulta a taxa do dia (API do Banco Central ou AwesomeAPI)
- A cotação usada é registrada no histórico da fatura (imutável)

### 5.5 Snapshots de Progresso (Profit Sharing)
- Provider faz upload de evidências (PDF, PNG, link) vinculadas ao período
- Cada snapshot tem: `title`, `period`, `file_url`, `kpi_value_demonstrated`
- Cliente visualiza e pode comentar antes de aprovar o bônus

---

## 6. Entidades do Domínio

```
User
  ├── id (UUID — mesmo do Supabase Auth)
  ├── full_name
  ├── email
  ├── avatar_url
  ├── role: PROVIDER | CLIENT | CLIENT_VIEWER
  └── created_at

Organization (Agência ou Freelancer como entidade)
  ├── id
  ├── name
  ├── owner_id (→ User)
  └── plan: FREE | PRO | ENTERPRISE

Contract
  ├── id
  ├── organization_id (→ Organization)
  ├── client_user_id (→ User)
  ├── title
  ├── type: RETAINER | FIXED_OVERAGE | PROFIT_SHARING
  ├── currency: BRL | USD | EUR
  ├── billing_day: Integer
  ├── start_date
  ├── end_date?
  ├── status: ACTIVE | PAUSED | TERMINATED
  └── config: JSONB  // campos específicos do tipo de contrato

TimeEntry
  ├── id
  ├── contract_id (→ Contract)
  ├── provider_id (→ User)
  ├── date
  ├── hours: Decimal
  ├── description
  ├── ai_summary?   // resumo gerado por IA para linguagem não-técnica
  ├── status: DRAFT | SUBMITTED | PENDING_APPROVAL | APPROVED | DISPUTED | INVOICED
  └── created_at

Milestone
  ├── id
  ├── contract_id (→ Contract)
  ├── title
  ├── description
  ├── trigger_event: String    // "deploy_production", "custom", etc.
  ├── payment_release_pct: Decimal
  ├── status: PENDING | TRIGGERED | PAID
  └── triggered_at?

Invoice
  ├── id
  ├── contract_id (→ Contract)
  ├── period_start
  ├── period_end
  ├── base_amount
  ├── hours_amount
  ├── overage_amount
  ├── bonus_amount
  ├── total_amount
  ├── currency
  ├── exchange_rate?
  ├── exchange_rate_date?
  ├── status: DRAFT | SENT | PAID | OVERDUE
  └── issued_at

KpiEntry (Profit Sharing)
  ├── id
  ├── contract_id (→ Contract)
  ├── period
  ├── kpi_value: BigDecimal
  ├── bonus_calculated: BigDecimal
  ├── submitted_by (→ User)
  └── approved_at?

Snapshot (Evidência)
  ├── id
  ├── contract_id (→ Contract)
  ├── kpi_entry_id? (→ KpiEntry)
  ├── title
  ├── file_url
  ├── period
  └── uploaded_at

ContractHealthScore
  ├── contract_id (→ Contract)
  ├── score: Integer (0–100)
  ├── on_time_delivery_rate: Decimal
  ├── approval_speed_avg_hours: Decimal
  ├── rework_rate: Decimal
  ├── last_calculated_at
  └── history: JSONB[]

Notification
  ├── id
  ├── user_id (→ User)
  ├── contract_id? (→ Contract)
  ├── type: HOURS_ALERT | APPROVAL_NEEDED | INVOICE_SENT | MILESTONE_TRIGGERED | ...
  ├── channel: IN_APP | EMAIL | WHATSAPP
  ├── payload: JSONB
  ├── read_at?
  └── sent_at
```

---

## 7. Módulos do Backend (Java)

```
src/main/java/com/contractpulse/
│
├── auth/
│   ├── JwtSupabaseFilter.java        // valida JWT do Supabase
│   ├── SecurityConfig.java
│   └── CurrentUserService.java       // extrai userId do contexto
│
├── user/
│   ├── UserController.java
│   ├── UserService.java
│   └── UserRepository.java
│
├── organization/
│   ├── OrganizationController.java
│   ├── OrganizationService.java
│   └── OrganizationRepository.java
│
├── contract/
│   ├── ContractController.java
│   ├── ContractService.java
│   ├── ContractRepository.java
│   ├── model/
│   │   ├── Contract.java
│   │   ├── ContractType.java
│   │   └── ContractStatus.java
│   └── config/                       // POJOs serializados em JSONB
│       ├── RetainerConfig.java
│       ├── FixedOverageConfig.java
│       └── ProfitSharingConfig.java
│
├── timeentry/
│   ├── TimeEntryController.java
│   ├── TimeEntryService.java
│   ├── TimeEntryRepository.java
│   ├── ApprovalService.java          // lógica de aprovação/disputa
│   └── AiSummaryService.java         // chama LLM para resumo não-técnico
│
├── milestone/
│   ├── MilestoneController.java
│   ├── MilestoneService.java
│   └── MilestoneRepository.java
│
├── invoice/
│   ├── InvoiceController.java
│   ├── InvoiceService.java           // orquestra cálculo de fatura
│   ├── InvoiceRepository.java
│   └── calculator/
│       ├── RetainerCalculator.java
│       ├── FixedOverageCalculator.java
│       └── ProfitSharingCalculator.java
│
├── kpi/
│   ├── KpiController.java
│   ├── KpiService.java
│   ├── KpiRepository.java
│   └── SnapshotService.java
│
├── healthscore/
│   ├── HealthScoreService.java       // calcula e persiste o score
│   └── HealthScoreRepository.java
│
├── notification/
│   ├── NotificationService.java      // orquestra envios
│   ├── EmailNotificationAdapter.java // Resend ou SendGrid
│   └── WhatsappNotificationAdapter.java // Z-API ou Twilio
│
├── currency/
│   └── ExchangeRateService.java      // AwesomeAPI / BCB
│
└── scheduler/
    ├── WeeklyApprovalJob.java        // gera resumos semanais
    ├── MonthlyInvoiceJob.java        // dispara geração de faturas
    └── HealthScoreJob.java           // recalcula scores
```

---

## 8. Estrutura do Frontend (Next.js)

```
src/
├── app/
│   ├── (auth)/
│   │   ├── login/page.tsx
│   │   └── callback/page.tsx         // OAuth callback do Supabase
│   │
│   ├── (dashboard)/
│   │   ├── layout.tsx                // sidebar + header autenticados
│   │   │
│   │   ├── overview/page.tsx         // visão geral (provider)
│   │   │
│   │   ├── contracts/
│   │   │   ├── page.tsx              // lista de contratos
│   │   │   ├── new/page.tsx          // criar contrato (wizard por tipo)
│   │   │   └── [id]/
│   │   │       ├── page.tsx          // detalhe do contrato
│   │   │       ├── time-entries/     // lançamentos de horas
│   │   │       ├── milestones/       // milestones
│   │   │       ├── invoices/         // faturas
│   │   │       ├── snapshots/        // evidências (profit sharing)
│   │   │       └── health-score/     // score do contrato
│   │   │
│   │   └── settings/
│   │       ├── page.tsx
│   │       ├── profile/
│   │       └── organization/
│   │
│   └── client-view/[contractId]/
│       └── page.tsx                  // dashboard view-only do cliente
│
├── components/
│   ├── ui/                           // shadcn/ui re-exports
│   ├── charts/
│   │   ├── BurnRateChart.tsx
│   │   ├── HoursBreakdownChart.tsx
│   │   └── HealthScoreGauge.tsx
│   ├── contract/
│   │   ├── ContractCard.tsx
│   │   ├── ContractWizard.tsx
│   │   └── ContractTypeConfig/
│   ├── time-entry/
│   │   ├── TimeEntryForm.tsx
│   │   ├── TimeEntryList.tsx
│   │   └── ApprovalBadge.tsx
│   └── notifications/
│       └── NotificationBell.tsx
│
├── lib/
│   ├── supabase/
│   │   ├── client.ts
│   │   └── server.ts
│   ├── api/                          // fetch wrappers para o backend Java
│   │   ├── contracts.ts
│   │   ├── time-entries.ts
│   │   └── invoices.ts
│   └── utils/
│       ├── currency.ts
│       └── date.ts
│
└── middleware.ts                     // proteção de rotas via Supabase Auth
```

---

## 9. Autenticação & Autorização

### Provedores de Auth (Supabase)
- ✅ **Google OAuth** (principal — implementar primeiro)
- ✅ **E-mail + Senha** (fallback)
- 🔲 **Magic Link** (V2)

### Configuração Google OAuth no Supabase
```
Supabase Dashboard → Auth → Providers → Google
  - Client ID: (Google Cloud Console)
  - Client Secret: (Google Cloud Console)
  - Authorized redirect URI: https://<project>.supabase.co/auth/v1/callback
```

### Controle de Acesso no Backend

| Role | Permissão |
|---|---|
| `PROVIDER` | CRUD em contratos próprios, lançar horas, criar milestones |
| `CLIENT` | Ver contratos vinculados, aprovar/disputar horas, inserir KPIs |
| `CLIENT_VIEWER` | Read-only no dashboard do cliente |
| `ADMIN` (futuro) | Acesso total à organização |

### Validação JWT no Spring Boot

```java
// application.yml
supabase:
  jwt-secret: ${SUPABASE_JWT_SECRET}
  project-url: ${SUPABASE_URL}

// JwtSupabaseFilter valida o token em cada request:
// 1. Extrai o Bearer token do header
// 2. Valida assinatura com a chave do Supabase
// 3. Extrai sub (userId) e role do payload
// 4. Seta no SecurityContextHolder
```

---

## 10. Banco de Dados (Supabase/PostgreSQL)

### Estratégia de Migrations
- Backend Java usa **Flyway** para versionar as migrations
- Scripts SQL ficam em `src/main/resources/db/migration/`
- Padrão: `V1__create_users.sql`, `V2__create_contracts.sql`, etc.

### Row Level Security (RLS) — Supabase
Ativar RLS nas tabelas para acesso direto do frontend (realtime, storage):

```sql
-- Exemplo: contract só visível para quem pertence
ALTER TABLE contracts ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Provider vê seus contratos"
  ON contracts FOR SELECT
  USING (organization_id IN (
    SELECT id FROM organizations WHERE owner_id = auth.uid()
  ));

CREATE POLICY "Client vê contratos vinculados"
  ON contracts FOR SELECT
  USING (client_user_id = auth.uid());
```

### Índices Essenciais

```sql
CREATE INDEX idx_time_entries_contract_status ON time_entries(contract_id, status);
CREATE INDEX idx_time_entries_date ON time_entries(date);
CREATE INDEX idx_invoices_contract_period ON invoices(contract_id, period_start, period_end);
CREATE INDEX idx_notifications_user_read ON notifications(user_id, read_at);
```

---

## 11. Integrações Externas

### Conversão de Moeda
- **AwesomeAPI** (`https://economia.awesomeapi.com.br`) — gratuita, BR-friendly
- Fallback: API do Banco Central (PTAX)
- Cache da cotação por dia (evitar chamadas repetidas)

### Notificações
| Canal | Provedor sugerido | Quando usar |
|---|---|---|
| E-mail | **Resend** (JS-friendly) ou SendGrid | Resumos semanais, faturas |
| WhatsApp | **Z-API** ou Twilio | Alertas urgentes (burn rate 80%) |
| In-app | Supabase Realtime | Notificações em tempo real no dashboard |

### Cobrança (V2)
- **Asaas** (melhor para Brasil — PIX, boleto, cartão)
- **Stripe** (para contratos internacionais)

### IA — Resumo de Logs (V1.5)
- **OpenAI API** (`gpt-4o-mini`) para traduzir lançamentos técnicos em linguagem de negócios
- Prompt: _"Transforme este log técnico em uma frase clara para um sócio não técnico: {description}"_
- Chamada feita no `AiSummaryService` ao submeter o time entry

---

## 12. Funcionalidades por Perfil

### Dashboard do Provider (Prestador)

| Feature | Descrição |
|---|---|
| Faturamento projetado do mês | Soma de horas aprovadas × valor + fixo + bônus estimado |
| Pipeline de aprovações pendentes | Horas enviadas aguardando OK do cliente |
| Alertas de contrato | Contratos próximos ao teto de horas |
| Health Score por contrato | Indicador de saúde do relacionamento |
| Criar/editar contratos | Wizard por tipo de contrato |
| Lançar horas | Com descrição e data |
| Upload de snapshots | Evidências de KPI |
| Configurar milestones | Gatilhos de pagamento |

### Dashboard do Client (Cliente)

| Feature | Descrição |
|---|---|
| Burn rate visual | Gráfico de consumo de horas/orçamento |
| Saldo de horas | Horas restantes no banco atual |
| Aprovação de horas | Aceitar ou disputar lançamentos |
| Status de entregas | Milestones e seu estado atual |
| Histórico de faturas | Faturas pagas e em aberto |
| Inserir KPI | Para contratos de profit sharing |
| Visualizar snapshots | Evidências do prestador |
| Health Score | Score do contrato e histórico |

---

## 13. Health Score — Métrica Proprietária

O **ContractPulse Health Score** é um índice de 0 a 100 que representa a saúde do relacionamento contratual.

### Cálculo (ponderado)

| Dimensão | Peso | Como mede |
|---|---|---|
| Pontualidade de entrega | 30% | % de milestones entregues antes do prazo |
| Velocidade de aprovação (cliente) | 20% | Média de horas para aprovar lançamentos |
| Taxa de retrabalho | 25% | % de horas marcadas como rework/bugfix |
| Índice de disputas | 15% | % de lançamentos disputados vs aprovados |
| Consistência de lançamento | 10% | % de semanas com pelo menos 1 lançamento |

### Formula

```
score = (
  (on_time_pct * 0.30) +
  (approval_speed_score * 0.20) +   // 100 se < 24h, 0 se > 120h
  ((1 - rework_rate) * 0.25) +
  ((1 - dispute_rate) * 0.15) +
  (consistency_score * 0.10)
) * 100
```

### Faixas

| Score | Classificação | Cor |
|---|---|---|
| 80–100 | 🟢 Excellent | Verde |
| 60–79 | 🟡 Good | Amarelo |
| 40–59 | 🟠 At Risk | Laranja |
| 0–39 | 🔴 Critical | Vermelho |

O score é público para o cliente (incentiva aprovações rápidas) e pode ser exportado pelo provider como "referência de portfólio".

---

## 14. Roadmap MVP → V1 → V2

### MVP (8–12 semanas)
- [ ] Auth com Supabase (Google + e-mail)
- [ ] CRUD de Organizações e Contratos (tipo Retainer)
- [ ] Lançamento e aprovação de horas (fluxo semanal)
- [ ] Dashboard Provider: faturamento projetado + horas
- [ ] Dashboard Client: burn rate + saldo
- [ ] Geração de fatura mensal (cálculo manual, PDF simples)
- [ ] Notificações por e-mail (Resend)
- [ ] Health Score básico (pontualidade + disputas)

### V1 (+ 6–8 semanas)
- [ ] Tipos de contrato: Fixed Overage + Profit Sharing
- [ ] Milestones com gatilhos de pagamento
- [ ] Upload de snapshots (Supabase Storage)
- [ ] Resumo de IA para logs técnicos (OpenAI)
- [ ] Conversão de moeda (AwesomeAPI)
- [ ] Notificações WhatsApp (Z-API)
- [ ] Health Score completo + histórico

### V2 (+ 8–12 semanas)
- [ ] Integração Asaas (PIX, boleto automático)
- [ ] Integração Stripe (contratos internacionais)
- [ ] Magic Link auth
- [ ] Multi-contratos por workspace
- [ ] Planos de assinatura (Free / Pro / Enterprise)
- [ ] API pública para integrações de clientes
- [ ] Mobile App (React Native ou PWA)

---

## 15. Padrões de Engenharia de Software

Este projeto adota rigorosamente os principais princípios de engenharia de software. Eles não são sugestões — são requisitos de qualidade que devem ser seguidos em todo o codebase.

---

### 15.1 Clean Code

O código deve ser **legível como prosa**. Um desenvolvedor que nunca viu o projeto deve entender o que um método faz só pelo nome.

**Nomenclatura**
- Nomes revelam intenção: `calculateMonthlyInvoice()` não `calc()` ou `process()`
- Sem abreviações ambíguas: `contractRepository` não `cRepo`
- Booleanos com prefixo: `isOverageAllowed`, `hasReachedThreshold`, `canSubmitEntry`
- Classes são substantivos, métodos são verbos: `InvoiceGenerator`, `generateInvoice()`

**Funções e Métodos**
- Fazem **uma coisa só** — se o nome precisa de "e" para ser descrito, está fazendo duas coisas
- Máximo de 20 linhas como guia (exceções documentadas)
- Máximo de 3 parâmetros; se precisar de mais, encapsular em um objeto/DTO
- Sem side effects escondidos — o nome deve refletir completamente o comportamento
- Retorno consistente — evitar `null`; preferir `Optional<T>` no Java e tipos explícitos no TypeScript

**Comentários**
- O código deve se auto-documentar; comentários explicam o **porquê**, não o **o quê**
- Javadoc obrigatório em todas as interfaces públicas dos módulos
- Nunca comentar código morto — usar o controle de versão (git) para isso

```java
// ❌ Ruim — comentário explica o óbvio
// Incrementa o contador de horas
totalHours = totalHours + entry.getHours();

// ✅ Bom — comentário explica decisão de negócio
// Horas em DISPUTED não entram no cálculo até resolução,
// evitando faturamento antecipado de itens contestados
if (!entry.getStatus().equals(TimeEntryStatus.DISPUTED)) {
    totalHours = totalHours.add(entry.getHours());
}
```

---

### 15.2 Princípios SOLID

#### S — Single Responsibility Principle (SRP)
Cada classe tem **um único motivo para mudar**.

```
✅ InvoiceCalculator      → só calcula valores
✅ InvoiceRepository      → só persiste/busca no banco
✅ InvoicePdfGenerator    → só gera o PDF
✅ InvoiceEmailSender     → só envia o e-mail

❌ InvoiceService         → calcula + persiste + gera PDF + envia e-mail (violação)
```

#### O — Open/Closed Principle (OCP)
Aberto para extensão, fechado para modificação. Novos tipos de contrato não devem exigir alteração do código existente.

```java
// Interface fechada para modificação
public interface ContractCalculator {
    InvoiceBreakdown calculate(Contract contract, List<TimeEntry> approvedEntries);
}

// Extensão via nova implementação — não muda o código existente
public class RetainerCalculator implements ContractCalculator { ... }
public class FixedOverageCalculator implements ContractCalculator { ... }
public class ProfitSharingCalculator implements ContractCalculator { ... }

// Factory que resolve qual usar
public class ContractCalculatorFactory {
    public ContractCalculator getCalculator(ContractType type) { ... }
}
```

#### L — Liskov Substitution Principle (LSP)
Implementações de uma interface devem ser intercambiáveis sem quebrar o comportamento esperado.

- Todo `ContractCalculator` deve retornar um `InvoiceBreakdown` válido e não nulo
- Todo `NotificationAdapter` deve garantir entrega ou lançar exceção tipada — nunca engolir o erro silenciosamente

#### I — Interface Segregation Principle (ISP)
Interfaces finas e específicas. Nenhuma classe deve depender de métodos que não usa.

```java
// ❌ Interface gorda — força implementar o que não usa
public interface ContractManager {
    void createContract(...);
    void calculateInvoice(...);
    void sendNotification(...);
    void uploadSnapshot(...);
}

// ✅ Interfaces segregadas
public interface ContractCreator { void createContract(...); }
public interface InvoiceCalculator { InvoiceBreakdown calculate(...); }
public interface NotificationSender { void send(...); }
```

#### D — Dependency Inversion Principle (DIP)
Módulos de alto nível não dependem de módulos de baixo nível — ambos dependem de abstrações.

```java
// ✅ O serviço depende da interface, não da implementação concreta
public class NotificationService {
    private final List<NotificationAdapter> adapters; // interface

    public NotificationService(List<NotificationAdapter> adapters) {
        this.adapters = adapters; // Spring injeta EmailAdapter + WhatsappAdapter
    }
}
```

---

### 15.3 DRY — Don't Repeat Yourself

Cada pedaço de conhecimento do sistema deve ter **uma representação única e autoritativa**.

**Regras práticas:**
- Lógica de cálculo de burn rate existe em **um único lugar** — se precisar em dois contextos, extrair para um utilitário/service compartilhado
- Validações de DTO não são replicadas no service — usar Bean Validation (`@NotNull`, `@Positive`) nos DTOs
- Constantes de negócio (ex: threshold de alerta padrão = 80%) ficam em `ContractDefaults.java`, não espalhadas no código
- Queries complexas e frequentes extraídas para métodos nomeados no repositório, nunca repetidas inline

```java
// ❌ Repetição — mesma query em 3 lugares
contractRepository.findByOrganizationIdAndStatus(orgId, ContractStatus.ACTIVE);

// ✅ DRY — método nomeado no repositório
contractRepository.findActiveByOrganization(orgId);
```

---

### 15.4 YAGNI — You Aren't Gonna Need It

Não implementar funcionalidades especulativas. O código deve resolver o problema **atual**, não o hipotético.

- Se não está no roadmap da fase atual, não entra no código
- Evitar generalizações prematuras ("e se um dia quisermos suportar X?") sem evidência de necessidade
- Abstrações surgem do código existente (refactor), não de antecipação

---

### 15.5 KISS — Keep It Simple, Stupid

A solução mais simples que funciona é a correta. Complexidade só se justifica quando a simplicidade não resolve.

- Preferir código linear e explícito a estruturas "inteligentes" e difíceis de depurar
- Evitar over-engineering: um `if/else` claro é melhor que um Strategy Pattern desnecessário
- Se um PR precisa de mais de 10 minutos para ser explicado, provavelmente é complexo demais

---

### 15.6 Separation of Concerns (SoC)

Cada camada tem responsabilidades bem definidas e não ultrapassa seus limites.

| Camada | Responsabilidade | O que NÃO deve fazer |
|---|---|---|
| `Controller` | Receber request, validar input, retornar response HTTP | Lógica de negócio, acesso ao banco |
| `Service` | Orquestrar regras de negócio | Montar queries SQL, formatar JSON de resposta |
| `Repository` | Persistência e consultas ao banco | Regras de negócio, transformação de dados |
| `Domain/Model` | Representar entidades e seus invariantes | Depender de infraestrutura (banco, HTTP) |
| `DTO` | Transferência de dados entre camadas | Lógica de negócio, anotações JPA |

---

### 15.7 Fail Fast

Erros devem ser detectados e sinalizados o mais cedo possível na execução, nunca propagados silenciosamente.

- Validar input na borda do sistema (Controller/DTO) antes de qualquer processamento
- Lançar exceção de domínio tipada imediatamente ao detectar estado inválido
- Nunca retornar `null` quando um valor é esperado — preferir `Optional`, exceção ou valor padrão explícito
- Transações financeiras com rollback automático em qualquer falha (`@Transactional`)

```java
// ❌ Fail late — o erro só aparece lá na frente
public InvoiceBreakdown calculate(Contract contract) {
    if (contract == null) return new InvoiceBreakdown(); // mascara o problema
}

// ✅ Fail fast — falha imediatamente com contexto claro
public InvoiceBreakdown calculate(Contract contract) {
    Objects.requireNonNull(contract, "Contract must not be null for calculation");
    if (!contract.isActive()) {
        throw new ContractInactiveException(contract.getId());
    }
    // ...
}
```

---

### 15.8 Padrões de Design Aplicados ao Projeto

| Padrão | Onde usar | Por quê |
|---|---|---|
| **Strategy** | `ContractCalculator` por tipo de contrato | Permite adicionar novos tipos sem alterar código existente (OCP) |
| **Factory** | `ContractCalculatorFactory` | Desacopla a criação da implementação concreta |
| **Adapter** | `EmailNotificationAdapter`, `WhatsappNotificationAdapter` | Isola dependências externas; facilita troca de provedor |
| **Repository** | Toda persistência de dados | Abstrai o banco de dados do domínio |
| **DTO (Data Transfer Object)** | Todas as entradas e saídas da API | Evita expor detalhes internos do domínio |
| **Builder** | Montagem de `Invoice`, `HealthScore` | Evita construtores com muitos parâmetros |
| **Observer / Event** | Notificações após aprovação, fatura gerada | Desacopla o disparo de notificações da lógica de negócio |

---

### 15.9 Convenções Específicas por Camada

#### Backend (Java)
- DTOs separados para request e response: `CreateContractRequest`, `ContractResponse`
- Exceções customizadas por domínio com HTTP status mapeado: `ContractNotFoundException` → 404, `UnauthorizedApprovalException` → 403
- Toda operação financeira deve ser **idempotente** (usar `idempotency_key` nas faturas)
- Valores monetários: sempre `BigDecimal`, **nunca** `float` ou `double`
- Datas: `LocalDate` / `LocalDateTime`; `ZonedDateTime` para contratos internacionais
- Testes: cobertura mínima de **80% nos `Service` layers** (JUnit 5 + Mockito + Testcontainers)

#### Frontend (Next.js / TypeScript)
- **`strict: true`** no `tsconfig.json` — sem `any` implícito
- Server Components por padrão; `"use client"` somente quando necessário (interatividade, hooks de browser)
- Validação de formulários com **Zod** — schema é a fonte única de verdade para tipos e validação
- Componentes pequenos e focados: um componente não deve ter mais de ~150 linhas
- Custom hooks para encapsular lógica reutilizável: `useContractBurnRate()`, `useApprovalFlow()`
- Sem lógica de negócio em componentes — delegar para hooks ou funções utilitárias

---

### 15.10 Git e Qualidade de Código

**Branches**
- `main` → produção (protegida, requer PR + review)
- `develop` → staging (integração contínua)
- Feature: `feat/contract-wizard`
- Fix: `fix/approval-email-timeout`
- Chore: `chore/update-dependencies`

**Commits — Conventional Commits**
```
feat(contract): add profit sharing calculator
fix(invoice): correct BigDecimal rounding on overage
refactor(auth): extract JWT validation to dedicated filter
test(timeentry): add approval flow integration tests
docs(readme): update environment variable reference
chore(deps): upgrade Spring Boot to 3.3.x
```

**Pull Requests**
- Todo PR deve ter descrição do **problema** e da **solução**
- Testes obrigatórios para novas features e bug fixes
- Sem `TODO` ou código comentado mergeado em `main`
- Review obrigatório de ao menos 1 desenvolvedor antes do merge

## Padrões de Teste

### Estrutura de arquivos
- Um arquivo por camada: `{Entidade}ServiceTest.java` e `{Entidade}ControllerTest.java`
- Localização: `src/test/java/com/contractpulse/{módulo}/`
- Testes de integração (fluxos críticos apenas): `src/test/java/com/contractpulse/integration/`

### ServiceTest — unitário puro
- Anotação: `@ExtendWith(MockitoExtension.class)` — sem Spring, sem banco
- Mockar dependências com `@Mock`, injetar com `@InjectMocks`
- Cobrir obrigatoriamente: caminho feliz, entidade não encontrada, regras de negócio (ex: contrato pausado, horas excedidas)
- Nunca usar `@SpringBootTest` em testes de service

### ControllerTest — camada web
- Anotação: `@WebMvcTest(XController.class)` — sobe só a camada web
- Mockar o service com `@MockBean`
- Cobrir obrigatoriamente: HTTP status correto, validação do DTO (`@Valid`), formato do JSON de resposta
- Nunca testar regra de negócio aqui — isso é responsabilidade do ServiceTest

### IntegrationTest — fluxos críticos
- Anotação: `@SpringBootTest` + `@Testcontainers` (PostgreSQL real em Docker)
- Escrever apenas para fluxos completos de negócio (ex: criar contrato → lançar horas → aprovar → gerar fatura)
- Um teste por fluxo, não por entidade

### Regras gerais
- Nome do teste: `should{Resultado}When{Cenário}` — ex: `shouldThrowWhenContractNotFound`
- Estrutura interna: Arrange / Act / Assert com comentários separando as seções
- Cobertura mínima: 80% nos Service layers
- Nunca usar `@Autowired` em ServiceTest
- Valores monetários nos testes: sempre `BigDecimal`, nunca `double` ou `int`