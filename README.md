# UniPDS Tests & Payment Service - Backend Java Spring Boot com Java 21, TDD & Data-Driven Testing (DDT)

🇧🇷 Português | 🇺🇸 English

---

## 🇧🇷 Português

### 📋 Visão Geral
O **UniPDS Tests** é um backend Java construído com **Spring Boot 3.3.11** e **Java 21**, estruturado em arquitetura limpa em camadas (Controllers, Services, Repositories, Models e Validators). O projeto inclui a implementação completa do **Payment Service**, desenvolvido sob a metodologia **TDD (Test-Driven Development)** para processamento de pagamentos com controle de limite diário acumulado e uma arquitetura avançada de **Data-Driven Testing (DDT)** com JUnit 5.

Principais destaques:
- **Payment Service (TDD)**: Processamento de pagamentos com validação de limite diário acumulado de **R$ 2.000,00** por fonte (`PIX`, `DEBIT_CARD`, `CREDIT_CARD`).
- **Arquitetura Data-Driven Testing (DDT)**: Estrutura dedicada sob subpacotes `*.ddt` em todas as camadas da aplicação, utilizando anotações JUnit 5 (`@ValueSource`, `@CsvSource`, `@MethodSource`, `@ArgumentsSource`).
- **Testes Automatizados (246 Casos - 100% Sucesso)**: Suíte robusta cobrindo testes unitários, validações de domínio, manipuladores globais de exceção, repositórios, serviços, controllers e testes de integração com **Testcontainers MySQL**.
- **Cobertura de Código (JaCoCo)**: Relatórios detalhados de cobertura de instrução e código via `jacoco-maven-plugin`.
- **OpenAPI 3.0 & Swagger UI**: Documentação interativa da API REST.
- **Collection do Postman**: Collection pronta para importação e execução de requisições.
- **Qualidade & Análise Estática**: Análise contínua com **SpotBugs**, **PMD** e **SonarQube Community**.

---

## 🛠️ Tecnologias Utilizadas
- **Java 21**: Linguagem principal (LTS).
- **Spring Boot 3.3.11**: Framework backend.
- **Spring Data JPA**: Abstração de persistência relacional com Hibernate ORM.
- **Springdoc OpenAPI (v2.5.0)**: Geração de documentação Swagger UI.
- **JUnit 5 (Jupiter)**: Framework de testes automatizados e suítes parametrizadas Data-Driven Testing.
- **Mockito & AssertJ**: Mocking de dependências e asserções fluentes.
- **JaCoCo (v0.8.12)**: Cobertura e métricas de execução de testes.
- **Testcontainers & MySQL 9.2.0**: Containers Docker efêmeros para testes de integração com banco de dados relacional real.
- **H2 Database**: Banco em memória para execução ultrarrápida de testes de unidade e repositório.
- **SpotBugs, PMD & SonarQube**: Ferramentas de análise estática e qualidade de código.
- **Oracle FREE / MySQL / MS SQL Server**: Ambientes de persistência relacional.
- **Docker & Docker Compose**: Orquestração dos serviços da aplicação e SonarQube.
- **Maven**: Gerenciamento de dependências, ciclo de vida e build.

---

## 📐 Padrão Data-Driven Testing (DDT) com JUnit 5

Para garantir alta manutenibilidade, cobertura extensiva de cenários de borda e separação limpa da massa de testes, adotamos o padrão **Data-Driven Testing (DDT)** através de subpacotes dedicados `*.ddt` em todas as camadas da aplicação:

![Conceito Data-Driven Testing](src/main/resources/images/TestDataDriven.png)

> **Figura 1**: *Conceito e motivação do Data-Driven Testing (DDT): expansão de cenários sem alterar o código dos testes, garantindo manutenibilidade e flexibilidade.*

### Estrutura de Pacotes DDT
```text
src/test/java/
├── com/artantech/paymentservice/
│   ├── controller/ddt/         # PaymentControllerDataDrivenTest
│   ├── exceptions/ddt/         # GlobalExceptionHandlerDataDrivenTest
│   ├── model/ddt/              # PaymentDataDrivenTest
│   ├── repository/ddt/         # PaymentRepositoryDataDrivenTest
│   ├── service/ddt/            # PaymentServiceImplDataDrivenTest
│   └── validator/ddt/          # DailyLimitValidatorDataDrivenTest
└── com/unipds/tests/
    ├── controller/ddt/         # CarroControllerDataDrivenTest, UsuarioControllerDataDrivenTest
    ├── model/ddt/              # CarroDataDrivenTest, UsuarioDataDrivenTest
    ├── repository/ddt/         # CarroRepositoryDataDrivenTest, UsuarioRepositoryDataDrivenTest
    └── service/ddt/            # CarroServiceDataDrivenTest, UsuarioServiceDataDrivenTest, ValueSourceTest
```

### Técnicas Parametrizadas Utilizadas:
1. **`@ValueSource`**: Testes com arrays de primitivos, Strings ou Enums (ex: validação de faixas etárias de votação, IDs numéricos e status).
2. **`@CsvSource`**: Matrizes de dados tabulares delimitados por vírgula para testes com múltiplos parâmetros de entrada e resultados esperados.
3. **`@MethodSource`**: Provedores estáticos que retornam `Stream<Arguments>` para construção de DTOs complexos e coleções.
4. **`@ArgumentsSource`**: Classes customizadas que implementam `ArgumentsProvider` para desacoplar a geração de dados da lógica de teste.

---

## 🚀 Como Inicializar o Projeto

### 1. Pré-requisitos
- **Java 21** instalado (`java -version`)
- **Maven 3.8+** (ou utilizar o wrapper `./mvnw`)
- **Docker & Docker Compose** (necessário para Testcontainers MySQL, banco Oracle e SonarQube)

---

### 2. Inicialização em Modo Desenvolvimento

Para rodar a aplicação localmente utilizando o Spring Boot Maven Plugin:

```bash
./mvnw spring-boot:run
```

A aplicação iniciará na porta **8080** por padrão (`http://localhost:8080`).

---

### 3. Inicialização via JAR Compilado

```bash
# 1. Compilar e empacotar a aplicação
./mvnw clean package -DskipTests

# 2. Executar o JAR compilado
java -jar target/unipds-tests-0.0.1-SNAPSHOT.jar
```

---

### 4. Inicialização via Docker Compose (Com Banco Oracle)

```bash
# Executar o script automatizado de inicialização e ambiente Docker
./start_app.sh
```

---

## 🧪 Testes Automatizados & Relatórios JaCoCo

A suíte conta com **246 testes automatizados executados com 100% de sucesso**.

Comandos de execução de testes e geração de relatórios:

```bash
# Executar a suíte completa de 246 testes automatizados
./mvnw clean test

# Gerar relatório visual de cobertura de código com JaCoCo
./mvnw jacoco:report

# Executar a verificação de código com SpotBugs
./mvnw spotbugs:check

# Executar a fase completa de verificação (build + testes + cobertura + qualidade)
./mvnw clean verify
```

O relatório interativo em HTML do JaCoCo é gerado em: `target/site/jacoco/index.html`.

![Relatório de Cobertura JaCoCo](src/main/resources/images/JacocoCoverageReport.png)

> **Figura 2**: *Relatório de cobertura do JaCoCo destacando taxas elevadas de cobertura por instrução (100% em controllers, DTOs, configs e modelos).*

---

## 🔍 Análise de Qualidade com SonarQube

O projeto inclui suporte integrado ao **SonarQube Community** para análise estática contínua e verificação de métricas de cobertura:

1. Subir a instância local do SonarQube (porta 9000):
   ```bash
   docker-compose -f docker-compose-sonarqube.yml up -d
   ```
2. Executar o script de análise automatizada:
   ```bash
   ./run_sonar.sh
   ```
3. Painel do SonarQube: [http://localhost:9000](http://localhost:9000)

### 📸 Evidências e Configurações do SonarQube

#### 1. Integração Build Maven + Importação JaCoCo + SonarQube
![Build Maven com SonarQube e JaCoCo](src/main/resources/images/BuildAndCoverageWithSonarQubeAndJacoco.png)
> **Figura 3**: *Execução do comando `mvn sonar:sonar` com importação automática do relatório XML do JaCoCo e sincronização com o painel SonarQube.*

#### 2. Configuração de Quality Gates Personalizados ("Artan Quality Gate")
![Criando Quality Gate no SonarQube](src/main/resources/images/CreateMyQualityGate.png)
> **Figura 4**: *Definição de regras customizadas para o Quality Gate, como exigência de 90% de cobertura em código novo e limite zero de duplicidades.*

![Associando Quality Gate](src/main/resources/images/MyQualityGate.png)
> **Figura 5**: *Associação do Quality Gate customizado ao projeto `unipds-tests`.*

#### 3. Autenticação Segura via Token
![Geração de Token SonarQube](src/main/resources/images/SonarToken.png)
> **Figura 6**: *Geração do User Token (`maven-token`) no SonarQube para autenticação segura no script `./run_sonar.sh`.*

#### 4. Resultados da Análise & Aprovação no Quality Gate
![Overview SonarQube Failed](src/main/resources/images/SonarOverview.png)
> **Figura 7**: *Estado inicial da análise antes da vinculação do relatório XML do JaCoCo.*

![Overview SonarQube Passed](src/main/resources/images/SonarQubeTestsExecutionPassed.png)
> **Figura 8**: *Painel do SonarQube com status **Passed** após atingir as métricas de qualidade e cobertura configuradas.*

#### 5. Gestão de Issues e Histórico de Análises
![Issues SonarQube](src/main/resources/images/Issues.png)
> **Figura 9**: *Detalhamento e acompanhamento de Code Smells, Bugs e Hotspots de segurança.*

![Histórico de Execuções](src/main/resources/images/HistoryBuilds.png)
> **Figura 10**: *Histórico temporal de builds e evolução da qualidade do código.*

---

## 🛡️ Análise Estática Local (SpotBugs & PMD)

Além do SonarQube, o projeto possui integrações com **SpotBugs** e **PMD** no ciclo de vida do Maven para detectar problemas antes do commit:

![SpotBugs com Maven Site](src/main/resources/images/SpotBugsWithMvnSite.gif)
> **Figura 11**: *Execução do SpotBugs integrado ao plugin `maven-site-plugin`.*

![Execução do SpotBugs via Terminal](src/main/resources/images/Screenshot%20from%202026-08-15%2022-51-57.png)
> **Figura 12**: *Execução de `mvn spotbugs:spotbugs` no VS Code Terminal.*

---

## 📖 Documentação da API

- **Swagger UI Interativo**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI 3.0 JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **Arquivo OpenAPI Local**: [`openapi.json`](file:///home/artanniel/git/java/unipds-tests/openapi.json)
- **Postman Collection**: [`Payment_Service.postman_collection.json`](file:///home/artanniel/git/java/unipds-tests/Payment_Service.postman_collection.json)

---

## 📍 Endpoints da API (`/payments`)

| Método | Endpoint | Descrição | Status de Sucesso |
| :--- | :--- | :--- | :--- |
| `POST` | `/payments` | Criar novo pagamento (status inicial `PENDING`) | `201 Created` |
| `GET` | `/payments/{id}` | Consultar pagamento por ID | `200 OK` |
| `GET` | `/payments` | Listar todos os pagamentos ordenados por data | `200 OK` |
| `PUT` | `/payments/{id}` | Atualizar status (`PAID` ou `FRAUD`) | `200 OK` |
| `GET` | `/payer/{id}` | Consultar pagamentos por ID do pagador | `200 OK` |

---

## 🇺🇸 English

### 📋 Overview
**UniPDS Tests & Payment Service** is a Java 21 backend application built on **Spring Boot 3.3.11** following **TDD** and structured **Data-Driven Testing (DDT)** architecture using JUnit 5 features (`@ValueSource`, `@CsvSource`, `@MethodSource`, `@ArgumentsSource`).

Key Features:
- **246 Automated Test Cases** with 100% pass rate.
- Dedicated `*.ddt` subpackage structure across all application layers.
- **JaCoCo** code coverage reporting (`target/site/jacoco/index.html`).
- **Testcontainers MySQL 9.2.0** integration tests.
- **SonarQube Community** code analysis support via `./run_sonar.sh`.
- **Swagger UI & OpenAPI 3.0** documentation.

### 🚀 Quick Start & Testing

```bash
# Run full automated test suite (246 tests)
./mvnw clean test

# Generate JaCoCo coverage report
./mvnw jacoco:report

# Run application
./mvnw spring-boot:run
```

---
