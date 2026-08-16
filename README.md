# UniPDS Tests - Backend Java Spring Boot com Java 21 & SpotBugs

🇧🇷 Português | 🇺🇸 English

---

## 🇧🇷 Português

### 📋 Visão Geral
O **UniPDS Tests** é um backend Java construído com **Spring Boot** e **Java 21**, estruturado com arquitetura em camadas (Controllers, Services, Repositories e Models), integração com o banco de dados **Oracle FREE** via Docker Compose, testes automatizados cobrindo todas as camadas (JUnit 5, H2 Database e MockMvc) e análise estática de código com o plugin **SpotBugs** (`com.github.spotbugs:spotbugs-maven-plugin`).

### 🛠️ Tecnologias Utilizadas
- **Java 21**: Linguagem principal (LTS).
- **Spring Boot 3.3.11**: Framework para criação da API REST.
- **Spring Data JPA**: Abstração de persistência.
- **JUnit 5 & Mockito**: Testes automatizados unitários e de integração.
- **H2 Database**: Banco de dados em memória para execução rápida de suítes de testes.
- **SpotBugs Plugin (`com.github.spotbugs`)**: Análise estática de código e prevenção de bugs (`effort: Max`, `threshold: Low`).
- **Oracle FREE**: Banco de dados relacional em container.
- **Docker & Docker Compose**: Orquestração dos serviços.
- **Maven**: Gerenciamento de dependências e build.

### 📦 Pré-requisitos
- JDK 21
- Maven 3.8+
- Docker e Docker Compose

### 🧪 Execução dos Testes Automatizados
Para executar a suíte completa de testes (Model, Repository, Service e Controller):
```bash
mvn clean test
```

### 🔍 Comandos e Análise de Qualidade de Código (SpotBugs & PMD)

O projeto conta com ferramentas de análise estática de código e checagem de qualidade integradas ao ciclo de vida do Maven:

- **`mvn spotbugs:spotbugs`**
  - **Descrição:** Executa a análise estática (SpotBugs) sobre o bytecode Java compilado (`.class`) e gera relatórios em formato XML (`target/spotbugsXml.xml`) e HTML (`target/spotbugshtml.html`).
  - **Comportamento:** **Não falha o build**, apenas gera os relatórios para consulta e inspeção.

- **`mvn spotbugs:check`**
  - **Descrição:** Executa a análise do SpotBugs e **falha a compilação (BUILD FAILURE)** caso qualquer bug seja detectado conforme as regras configuradas no `pom.xml`.
  - **Comportamento:** Recomendado para ambientes de **CI/CD** e verificações pré-commit para bloquear a integração de código com bugs.

- **`mvn pmd:check`**
  - **Descrição:** Executa a análise estática com o **PMD** sobre o código-fonte Java (`.java`), verificando boas práticas, código limpo, variáveis não utilizadas e complexidade de código.
  - **Comportamento:** **Falha o build (BUILD FAILURE)** se encontrar violações das regras configuradas do PMD.

- **`mvn verify`**
  - **Descrição:** Executa a fase de verificação completa do Maven (`verify` lifecycle stage). Passa por compilação, testes unitários (`mvn test`), empacotamento (`package`), testes de integração e executa todas as checagens dos plugins de qualidade.
  - **Comportamento:** É o comando mais seguro e abrangente para garantir que o projeto está saudável antes de criar um Pull Request ou release.

- **`mvn site`**
  - **Descrição:** Gera o site/documentação HTML completo do projeto dentro do diretório `target/site/`. Inclui uma página formatada e navegável do SpotBugs em `target/site/spotbugs.html`.
  - **Comportamento:** Compila a documentação completa do projeto Maven, integrando relatórios do SpotBugs, plugins e dependências.

---

### 📸 Demonstrações e Arquivos de Mídia (`src/main/resources/images`)

A pasta `src/main/resources/images` armazena imagens e animações de demonstração do projeto:

1. **`Screenshot from 2026-08-15 22-51-57.png`**
   - **Descrição:** Captura de tela da IDE (VS Code / Antigravity) exibindo a estrutura de arquivos do projeto `unipds-tests` e a execução do comando `mvn spotbugs:spotbugs` no terminal.
   - ![Estrutura do Projeto e Execução do SpotBugs](src/main/resources/images/Screenshot%20from%202026-08-15%2022-51-57.png)

2. **`SpotBugsWithMvnSite.gif`**
   - **Descrição:** Animação (GIF) que demonstra o fluxo de execução do SpotBugs e a integração com a geração do site do Maven (`mvn site`).
   - ![Demonstração do SpotBugs com Maven Site](src/main/resources/images/SpotBugsWithMvnSite.gif)

---

### 🚀 Como Iniciar o Projeto
1. **Compilar o Projeto:**
   ```bash
   mvn clean package -DskipTests
   ```

2. **Subir Serviços com Docker Compose:**
   ```bash
   docker-compose up --build -d
   ```
   Ou utilize o script automatizado que gerencia o ciclo completo de build e inicialização do Oracle FREE:
   ```bash
   ./start_app.sh
   ```

3. **Endpoints da API:**
   - **Usuários:**
     - `GET /usuarios`: Lista todos os usuários.
     - `POST /usuarios`: Cadastra um novo usuário (`{"nome": "Maria", "email": "maria@example.com"}`).
   - **Carros:**
     - `GET /carros`: Lista todos os carros.
     - `POST /carros`: Cadastra um novo carro (`{"modelo": "Corolla", "ano": 2023}`).

---

## 🇺🇸 English

### 📋 Overview
**UniPDS Tests** is a Java backend built with **Spring Boot** and **Java 21**, adopting a clean layered architecture (Controllers, Services, Repositories, Models), automated JUnit 5 tests, **Oracle FREE** database integration via Docker Compose, and static code analysis powered by **SpotBugs** and **PMD** plugins.

### 🔍 Quality & Code Analysis Commands Guide
- **`mvn spotbugs:spotbugs`**: Runs SpotBugs static code analysis and generates XML/HTML report files in `target/`.
- **`mvn spotbugs:check`**: Analyzes bytecode and **triggers a build failure** if bugs are detected.
- **`mvn pmd:check`**: Inspects Java source files against PMD rule sets and **triggers a build failure** on code quality violations.
- **`mvn verify`**: Runs the complete Maven verification lifecycle (compilation, unit tests, packaging, integration tests, and static quality checks).
- **`mvn site`**: Generates a complete project HTML website under `target/site/`, containing an interactive SpotBugs report at `target/site/spotbugs.html`.

---
*Projeto configurado por Antigravity AI / Configured by Antigravity AI*
