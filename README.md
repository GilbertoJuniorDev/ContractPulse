# ContractPulse

> Plataforma SaaS que cria confiança entre freelancers/agências e clientes através de visibilidade total sobre horas, entregas, métricas e pagamentos — em qualquer modelo de contrato variável.

---

## 📋 Índice

- [Visão Geral](#visão-geral)
- [Stack Tecnológica](#stack-tecnológica)
- [Arquitetura](#arquitetura)
- [Pré-requisitos](#pré-requisitos)
- [Configuração do Ambiente](#configuração-do-ambiente)
- [Executando o Projeto](#executando-o-projeto)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Modelos de Contrato](#modelos-de-contrato)
- [Autenticação](#autenticação)
- [Testes](#testes)
- [CI/CD](#cicd)
- [Health Score](#health-score)
- [Roadmap](#roadmap)
- [Convenções de Git](#convenções-de-git)

---

## Visão Geral

### Problema

Contratos variáveis (banco de horas, hora excedente, profit sharing) geram desconfiança porque o cliente não tem visibilidade do consumo e o prestador não tem proteção formal sobre seu esforço.

### Solução

Um SaaS que atua como **camada de transparência** entre as duas partes: o prestador lança, o cliente aprova, ambos têm dashboards dedicados, e o sistema cuida das regras financeiras automaticamente.

### Personas

| Persona | Papel | Dor principal |
|---|---|---|
| Freelancer / Dev | `PROVIDER` | Não receber por horas extras, sem histórico de confiabilidade |
| Agência | `PROVIDER` | Gerenciar múltiplos clientes com modelos diferentes |
| Cliente (PJ/PF) | `CLIENT` | Surpresas na fatura, sem visibilidade do burn rate |
| Sócio / Gestor | `CLIENT_VIEWER` | Quer ver progresso sem acesso operacional |

---

## Stack Tecnológica

### Backend

| Tecnologia | Versão / Detalhe |
|---|---|
| Java | 21 (LTS) |
| Spring Boot | 3.x |
| Spring Data JPA | Hibernate |
| Flyway | Migrations versionadas |
| Gradle | Build tool |
| JUnit 5 + Mockito | Testes unitários e de integração |
| SpringDoc OpenAPI | Swagger UI |
| Spring Security | Validação JWT do Supabase |

### Frontend

| Tecnologia | Versão / Detalhe |
|---|---|
| Next.js | 14.2 (App Router) |
| TypeScript | 5.7 (`strict: true`) |
| Tailwind CSS | 3.4 + Shadcn/UI |
| TanStack Query | 5.x (React Query) |
| React Hook Form | 7.x + Zod |
| Recharts | 2.x |
| Zustand | 5.x |

### Banco de Dados & Auth

| Serviço | Uso |
|---|---|
| Supabase (PostgreSQL) | Banco de dados gerenciado |
| Supabase Auth | Google OAuth + e-mail/senha |
| Supabase Storage | Snapshots e evidências |
| Supabase Realtime | Notificações ao vivo |

### Infraestrutura

| Serviço | Uso |
|---|---|
| Docker + Docker Compose | Desenvolvimento local |
| Railway / Render / Fly.io | Deploy do backend |
| Vercel | Deploy do frontend |
| GitHub Actions | CI/CD |

---

## Arquitetura

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
3. O frontend envia o JWT no header `Authorization: Bearer <token>` para o backend Java
4. O backend valida o JWT usando a chave pública do Supabase (JWKS endpoint)
5. O `userId` extraído do JWT é usado para todas as operações autorizadas

---

## Pré-requisitos

- **Java 21** (JDK)
- **Node.js 20+** (com npm)
- **Docker** e **Docker Compose**
- Conta no **Supabase** (banco de dados e autenticação)

---

## Configuração do Ambiente

### 1. Banco de Dados Local

Inicie o PostgreSQL via Docker Compose:

```bash
docker-compose up -d
```

Isso sobe um PostgreSQL 16 na porta `5432` com:
- **Database:** `contractpulse`
- **Usuário:** `postgres`
- **Senha:** `postgres`

### 2. Backend

Configure as variáveis de ambiente. O backend usa o `application.yml` com as seguintes propriedades:

```env
SUPABASE_JWT_SECRET=       # Secret JWT do seu projeto Supabase
SUPABASE_URL=              # URL do projeto Supabase
DATABASE_URL=              # URL JDBC do PostgreSQL
DATABASE_USERNAME=         # Usuário do banco
DATABASE_PASSWORD=         # Senha do banco
RESEND_API_KEY=            # Chave da API Resend (e-mails)
OPENAI_API_KEY=            # Chave da API OpenAI (resumos IA)
ZAPI_TOKEN=                # Token Z-API (WhatsApp)
```

### 3. Frontend

Crie o arquivo `.env.local` na pasta `frontend/`:

```env
NEXT_PUBLIC_SUPABASE_URL=          # URL do projeto Supabase
NEXT_PUBLIC_SUPABASE_ANON_KEY=     # Chave anônima do Supabase
NEXT_PUBLIC_API_URL=               # URL do backend (ex: http://localhost:8080)
```

---

## Executando o Projeto

### Backend

```bash
cd backend
./gradlew bootRun
```

O servidor inicia na porta **8080**. A documentação Swagger fica disponível em:
`http://localhost:8080/swagger-ui.html`

### Frontend

```bash
cd frontend
npm install
npm run dev
```

O frontend inicia em `http://localhost:3000`.

---

## Estrutura do Projeto

```
.
├── .github/
│   ├── instructions/
│   │   └── project.instructions.md    # Documento de referência do projeto
│   └── workflows/
│       └── ci.yml                      # Pipeline CI (GitHub Actions)
│
├── backend/                            # API Spring Boot (Java 21)
│   ├── src/main/java/com/contractpulse/
│   │   ├── auth/                       # JwtSupabaseFilter + SecurityConfig + CurrentUserService
│   │   ├── user/                       # CRUD de usuários + DTOs + exceções
│   │   ├── organization/              # Organizações (agências/freelancers)
│   │   ├── contract/                   # Contratos + modelos + configs JSONB
│   │   │   ├── model/                  # Contract, ContractType, ContractStatus, RolloverPolicy
│   │   │   ├── config/                 # RetainerConfig, FixedOverageConfig, ProfitSharingConfig
│   │   │   ├── dto/                    # CreateContractRequest, ContractResponse, etc.
│   │   │   └── exception/             # ContractNotFoundException, ContractInactiveException
│   │   ├── timeentry/                  # Lançamento de horas + aprovação + resumo IA
│   │   ├── milestone/                  # Milestones e gatilhos de pagamento
│   │   ├── invoice/                    # Faturas + calculators (Strategy Pattern)
│   │   │   └── calculator/             # RetainerCalculator, FixedOverageCalculator, ProfitSharingCalculator
│   │   ├── kpi/                        # KPI para Profit Sharing + Snapshots
│   │   ├── healthscore/              # Health Score proprietário
│   │   ├── notification/              # Adaptadores: Email (Resend) + WhatsApp (Z-API)
│   │   ├── currency/                   # Conversão de moeda (AwesomeAPI)
│   │   └── scheduler/                  # Jobs: WeeklyApproval, MonthlyInvoice, HealthScore
│   └── src/main/resources/
│       ├── application.yml
│       └── db/migration/               # Migrations Flyway
│
├── frontend/                           # Next.js 14 (App Router)
│   └── src/
│       ├── app/
│       │   ├── (auth)/                 # Login e callback OAuth
│       │   ├── (dashboard)/            # Área autenticada do provider
│       │   │   ├── overview/           # Visão geral
│       │   │   ├── contracts/          # Listagem, criação e detalhe
│       │   │   └── settings/           # Configurações
│       │   ├── client-view/            # Dashboard view-only do cliente
│       │   └── api/                    # API routes do Next.js
│       ├── components/
│       │   ├── charts/                 # Gráficos (BurnRate, HealthScore)
│       │   ├── contract/               # Cards, Wizard, StatusBadge
│       │   ├── notifications/          # NotificationBell
│       │   ├── organization/           # Cards e listagem
│       │   ├── time-entry/             # Formulário e listagem de horas
│       │   └── ui/                     # Shadcn/UI components
│       ├── hooks/                      # Custom hooks (useContracts, useClients, etc.)
│       └── lib/
│           ├── api/                    # Fetch wrappers para o backend
│           ├── supabase/               # Clients Supabase (server/client)
│           ├── types/                  # Tipos TypeScript (espelho dos DTOs)
│           ├── validations/            # Schemas Zod
│           └── utils/                  # Utilitários (moeda, data)
│
├── docker-compose.yml                  # PostgreSQL 16 para desenvolvimento local
└── README.md
```

---

## Modelos de Contrato

### Banco de Horas (Retainer)

Pacote mensal de horas com política de rollover configurável.

| Campo | Tipo | Descrição |
|---|---|---|
| `monthlyHours` | Integer | Horas contratadas por mês |
| `hourlyRate` | BigDecimal | Valor por hora |
| `rolloverPolicy` | Enum | `EXPIRE` · `ACCUMULATE` · `PARTIAL` |
| `alertThreshold` | Integer | % de consumo para alerta (padrão: 80%) |
| `overageAllowed` | Boolean | Permite ultrapassar o pacote? |
| `overageRate` | BigDecimal | Valor da hora excedente |

**Regras:** ao virar o mês, aplica-se a `rolloverPolicy`. Alerta automático ao atingir o threshold. Se `overageAllowed = false`, bloqueia novos lançamentos em 100%.

### Hora Excedente (Fixed + Overage)

Valor fixo mensal + cobrança por hora extra além do teto.

| Campo | Tipo | Descrição |
|---|---|---|
| `baseFee` | BigDecimal | Valor fixo mensal |
| `includedHours` | Integer | Horas incluídas no fixo |
| `overageRate` | BigDecimal | Valor por hora excedente |
| `overageCeiling` | Integer | Teto máximo de horas extras (opcional) |

### Profit Sharing (Participação nos Resultados)

Valor base reduzido + bônus vinculado a KPIs.

| Campo | Tipo | Descrição |
|---|---|---|
| `baseFee` | BigDecimal | Valor base reduzido |
| `kpiType` | Enum | `REVENUE` · `PROFIT` · `CUSTOM` |
| `kpiTarget` | BigDecimal | Meta de KPI |
| `bonusPercentage` | BigDecimal | % de participação acima da meta |
| `bonusCap` | BigDecimal | Teto do bônus (opcional) |

**Fórmula do bônus:** `max(0, (kpi_actual - kpi_target) × bonus_percentage)`

---

## Autenticação

### Provedores Suportados

- **Google OAuth** (login social)
- **E-mail e senha** (cadastro tradicional)
- Magic Link (planejado para V2)

### Controle de Acesso

| Role | Permissão |
|---|---|
| `PROVIDER` | CRUD em contratos próprios, lançar horas, criar milestones |
| `CLIENT` | Ver contratos vinculados, aprovar/disputar horas, inserir KPIs |
| `CLIENT_VIEWER` | Read-only no dashboard do cliente |

### Validação JWT no Backend

O `JwtSupabaseFilter` intercepta cada requisição e:

1. Extrai o Bearer token do header `Authorization`
2. Valida a assinatura com a chave do Supabase
3. Extrai `sub` (userId) e role do payload
4. Seta no `SecurityContextHolder`

---

## Testes

### Backend

```bash
cd backend
./gradlew test
```

#### Convenções

| Tipo | Anotação | Escopo |
|---|---|---|
| **ServiceTest** (unitário) | `@ExtendWith(MockitoExtension.class)` | Sem Spring, sem banco |
| **ControllerTest** (web) | `@WebMvcTest` | Apenas camada web |
| **IntegrationTest** | `@SpringBootTest` + `@Testcontainers` | Fluxos completos |

**Regras gerais:**
- Nomenclatura: `should{Resultado}When{Cenário}`
- Estrutura interna: Arrange / Act / Assert
- Cobertura mínima: **80% nos Service layers**
- Valores monetários: sempre `BigDecimal`, nunca `double`
- `@Mock` para dependências, `@InjectMocks` para o SUT

### Frontend

```bash
cd frontend
npm run lint
```

---

## CI/CD

O pipeline está em `.github/workflows/ci.yml` e executa automaticamente em pushes e PRs para `main` e `develop`.

### Backend

1. Checkout do código
2. Setup JDK 21 (Temurin)
3. Setup Gradle
4. Build (`./gradlew build -x test`)
5. Testes (`./gradlew test`)

### Frontend

1. Checkout do código
2. Setup Node.js 20
3. Install (`npm ci`)
4. Lint (`npm run lint`)
5. Build (`npm run build`)

---

## Health Score

O **ContractPulse Health Score** é um índice proprietário de 0 a 100 que representa a saúde do relacionamento contratual.

### Cálculo (ponderado)

| Dimensão | Peso | Como mede |
|---|---|---|
| Pontualidade de entrega | 30% | % de milestones entregues antes do prazo |
| Velocidade de aprovação (cliente) | 20% | Média de horas para aprovar lançamentos |
| Taxa de retrabalho | 25% | % de horas marcadas como rework/bugfix |
| Índice de disputas | 15% | % de lançamentos disputados vs aprovados |
| Consistência de lançamento | 10% | % de semanas com pelo menos 1 lançamento |

### Fórmula

```
score = (
  (on_time_pct × 0.30) +
  (approval_speed_score × 0.20) +
  ((1 - rework_rate) × 0.25) +
  ((1 - dispute_rate) × 0.15) +
  (consistency_score × 0.10)
) × 100
```

### Faixas

| Score | Classificação | Cor |
|---|---|---|
| 80–100 | 🟢 Excellent | Verde |
| 60–79 | 🟡 Good | Amarelo |
| 40–59 | 🟠 At Risk | Laranja |
| 0–39 | 🔴 Critical | Vermelho |

---

## Roadmap

### MVP (8–12 semanas)

- [x] Auth com Supabase (Google + e-mail)
- [x] CRUD de Organizações e Contratos (tipo Retainer)
- [x] Lançamento e aprovação de horas (fluxo semanal)
- [x] Dashboard Provider e Client
- [ ] Geração de fatura mensal
- [ ] Notificações por e-mail (Resend)
- [ ] Health Score básico

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
- [ ] API pública para integrações
- [ ] Mobile App (React Native ou PWA)

---

## Convenções de Git

### Branches

| Branch | Uso |
|---|---|
| `main` | Produção (protegida, requer PR + review) |
| `develop` | Staging / integração contínua |
| `feat/nome` | Feature branches |
| `fix/nome` | Bug fixes |
| `chore/nome` | Tarefas de manutenção |

### Commits — Conventional Commits

```
feat(contract): add profit sharing calculator
fix(invoice): correct BigDecimal rounding on overage
refactor(auth): extract JWT validation to dedicated filter
test(timeentry): add approval flow integration tests
docs(readme): update environment variable reference
chore(deps): upgrade Spring Boot to 3.3.x
```

### Pull Requests

- Descrição obrigatória do **problema** e da **solução**
- Testes obrigatórios para novas features e bug fixes
- Sem `TODO` ou código comentado mergeado em `main`
- Review obrigatório de ao menos 1 desenvolvedor

---

## Licença

Projeto privado — todos os direitos reservados.
