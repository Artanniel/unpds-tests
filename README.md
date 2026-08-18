# UniPDS Tests & Payment Service - Backend Java Spring Boot com Java 21 & TDD

🇧🇷 Português | 🇺🇸 English

---

## 🇧🇷 Português

### 📋 Visão Geral
O **UniPDS Tests** é um backend Java construído com **Spring Boot 3.3.11** e **Java 21**, estruturado em arquitetura em camadas (Controllers, Services, Repositories e Models). O projeto inclui a implementação completa do **Payment Service**, desenvolvido sob a metodologia **TDD (Test-Driven Development)** para processamento de pagamentos com controle de limite diário acumulado.

Principais destaques:
- **Payment Service (TDD)**: Processamento de pagamentos com validação de limite diário acumulado de **R$ 2.000,00** por fonte (`PIX`, `DEBIT_CARD`, `CREDIT_CARD`).
- **OpenAPI 3.0 & Swagger UI**: Documentação interativa da API REST.
- **Collection do Postman**: Collection pronta para importação e execução de requisições.
- **Qualidade & Análise Estática**: Verificações com **SpotBugs** e **PMD** sem nenhuma violação.
- **Testes Automatizados (65 Casos)**: Suíte robusta cobrindo testes unitários, validações de domínio, manipulação de cache JPA com `TestEntityManager`, mapeamento com `ObjectMapper` e testes de integração com **Testcontainers MySQL**.

---

## 🛠️ Tecnologias Utilizadas
- **Java 21**: Linguagem principal (LTS).
- **Spring Boot 3.3.11**: Framework backend.
- **Spring Data JPA**: Abstração de persistência relacional.
- **Springdoc OpenAPI (v2.5.0)**: Geração de documentação Swagger UI.
- **JUnit 5, Mockito & AssertJ**: Testes automatizados unitários, de comportamento e de integração.
- **Testcontainers & MySQL 9.2.0**: Containers Docker efêmeros para testes de integração com banco de dados MySQL real.
- **H2 Database**: Banco em memória para execução ultrarrápida de testes de repositório e serviços.
- **SpotBugs & PMD**: Plugins Maven para análise estática rigorosa de código.
- **Oracle FREE**: Banco de dados relacional em container para ambiente de produção.
- **Docker & Docker Compose**: Orquestração dos serviços.
- **Maven**: Gerenciamento de dependências e build.

---

## 🚀 Como Inicializar o Projeto

### 1. Pré-requisitos
- **Java 21** instalado (`java -version`)
- **Maven 3.8+** (ou utilizar o wrapper `./mvnw`)
- **Docker & Docker Compose** (necessário para o perfil `mysql` com Testcontainers e ambiente Oracle)

---

### 2. Inicialização em Modo Desenvolvimento (Rápido)

Para rodar a aplicação localmente utilizando o Spring Boot Maven Plugin:

```bash
./mvnw spring-boot:run
```

A aplicação iniciará na porta **8080** por padrão (`http://localhost:8080`).

---

### 3. Inicialização via JAR Compilado

Você pode empacotar o projeto e rodar o arquivo `.jar`:

```bash
# 1. Compilar e empacotar a aplicação
./mvnw clean package -DskipTests

# 2. Executar o JAR compilado
java -jar target/unipds-tests-0.0.1-SNAPSHOT.jar
```

---

### 4. Inicialização via Docker Compose (Com Banco Oracle)

Para subir o banco de dados Oracle FREE e a aplicação em containers Docker:

```bash
# 1. Gerar o pacote JAR
./mvnw clean package -DskipTests

# 2. Subir os containers em segundo plano
docker-compose up --build -d
```

Ou execute o script automatizado de inicialização:
```bash
./start_app.sh
```

---

## 🗄️ Perfis e Inicialização de Banco de Dados

O banco de dados é **inicializado automaticamente**, variando conforme o perfil ativo:

### 🧪 Perfil Testcontainers MySQL (`application-mysql.yaml`)
- **Container Efêmero**: Utiliza `jdbc:tc:mysql:9.2.0:///payments` via `org.testcontainers.jdbc.ContainerDatabaseDriver`.
- **Injeção de Perfil**: Ativado na suíte de integração através da anotação `@ActiveProfiles("mysql")` e `@Testcontainers`.

### ⚡ Modo Testes Padrão (`H2 Database`)
- **Automático**: O Spring Boot cria a estrutura de tabelas em memória (`H2`) automaticamente ao iniciar os testes unitários/repositórios (`./mvnw test`), sem exigir ação manual.

### 🐳 Modo Docker / Produção (`Oracle FREE`)
- **Orquestração via Script**: O script [`./start_app.sh`](file:///home/artanniel/git/java/unipds-tests/start_app.sh) inicia o container do Oracle FREE via `docker-compose`, aguarda o status *healthycheck* e configura o schema.
- **Criação de Tabelas via Spring Boot**: A propriedade `spring.sql.init.mode=always` executa o script [`src/main/resources/schema.sql`](file:///home/artanniel/git/java/unipds-tests/src/main/resources/schema.sql) automaticamente ao se conectar.

---

## 📖 Documentação da API (Swagger UI & Postman Collection)

Após inicializar a aplicação (`http://localhost:8080`), você pode acessar a documentação interativa e os arquivos da API:

- **Swagger UI Interativo**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI 3.0 JSON (Servidor)**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **Especificação OpenAPI local**: [`openapi.json`](file:///home/artanniel/git/java/unipds-tests/openapi.json)
- **Collection do Postman**: [`Payment_Service.postman_collection.json`](file:///home/artanniel/git/java/unipds-tests/Payment_Service.postman_collection.json)

---

## 📍 Endpoints da API

### 💳 Payment Service (`/payments`)

| Método | Endpoint | Descrição | Status de Sucesso |
| :--- | :--- | :--- | :--- |
| `POST` | `/payments` | Criar novo pagamento (status inicial `PENDING`) | `201 Created` |
| `GET` | `/payments/{id}` | Consultar pagamento por ID | `200 OK` |
| `GET` | `/payments` | Listar todos os pagamentos ordenados por data | `200 OK` |
| `PUT` | `/payments/{id}` | Atualizar status (`PAID` ou `FRAUD`) | `200 OK` |
| `GET` | `/payer/{id}` | Consultar pagamentos por ID do pagador | `200 OK` |

#### Exemplo de Payload para Criação de Pagamento (`POST /payments`):
```json
{
  "transactionId": "TX-998811",
  "paymentSource": "PIX",
  "amount": 250.50,
  "payerId": "PAYER-123"
}
```

#### Regra do Limite Diário (R$ 2.000,00 por fonte):
A soma acumulada dos pagamentos criados no dia corrente para a mesma fonte (`PIX`, `DEBIT_CARD`, `CREDIT_CARD`) não pode exceder R$ 2.000,00. Caso exceda, a API retorna **`422 Unprocessable Entity`**.

---

## 🧪 Testes Automatizados & Qualidade de Código

Conceitos avançados implementados na suíte de testes (65 casos no total):
- **Object Mapping (`ObjectMapper`)**: Serialização de requisições e desserialização de DTOs e coleções (`getTypeFactory().constructCollectionType(List.class, PaymentResponseDTO.class)`).
- **Gerenciamento de Cache de Persistência (`TestEntityManager`)**: Uso de `entityManager.clear()` após updates JPQL para garantir sincronização real de datas e somas com o banco de dados.
- **Testcontainers MySQL**: Testes E2E executados contra container real do MySQL 9.2.0 configurado via `application-mysql.yaml` e `@ActiveProfiles("mysql")`.

Comandos de execução:

```bash
# Executar a suíte completa de 65 testes automatizados
./mvnw clean test

# Executar a verificação estática SpotBugs
./mvnw spotbugs:check

# Executar a fase completa de verificação (testes + qualidade + empacotamento)
./mvnw clean verify
```

---

## 🇺🇸 English

### 📋 Overview
**UniPDS Tests & Payment Service** is a Java 21 backend application powered by **Spring Boot 3.3.11**. It implements a complete **Payment Service** built with strict **TDD** practices, daily payment limit enforcement (R$ 2,000.00 per source), Swagger UI OpenAPI 3.0 documentation, **Testcontainers MySQL 9.2.0** integration tests, `ObjectMapper` DTO mapping, and zero-warning SpotBugs code quality compliance.

### 🗄️ Database Profiles & Initialization
- **Testcontainers MySQL Profile (`application-mysql.yaml`)**: Spawns an ephemeral MySQL 9.2.0 Docker container using `jdbc:tc:mysql:9.2.0:///payments` activated via `@ActiveProfiles("mysql")` and `@Testcontainers`.
- **H2 (Standard Test Mode)**: Created in-memory automatically by Spring Boot during unit/repository tests (`./mvnw test`).
- **Oracle (Docker/Prod Mode)**: Schema script [`schema.sql`](file:///home/artanniel/git/java/unipds-tests/src/main/resources/schema.sql) is automatically executed by Spring Boot (`spring.sql.init.mode=always`).

### 🚀 How to Run

1. **Development Mode**:
   ```bash
   ./mvnw spring-boot:run
   ```

2. **Run All 65 Automated Tests**:
   ```bash
   ./mvnw clean test
   ```

3. **Swagger UI**: Access `http://localhost:8080/swagger-ui.html` once the service is running.
4. **Postman Collection**: Import [`Payment_Service.postman_collection.json`](file:///home/artanniel/git/java/unipds-tests/Payment_Service.postman_collection.json) directly into Postman.

---
*Projeto mantido e configurado por Antigravity AI / Configured by Antigravity AI*

