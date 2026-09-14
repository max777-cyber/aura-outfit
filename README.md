# Aura Outfit — E-commerce de Moda

Plataforma de e-commerce de roupas desenvolvida em **Java** com **Spring Boot**, login com Google (OAuth2), pagamentos via **Mercado Pago** e deploy contínuo no **Railway**.

### 🔗 [Acesse a aplicação em produção](https://aura-outfit-production-a01a.up.railway.app)

[![CI](https://github.com/max777-cyber/aura-outfit/actions/workflows/ci.yml/badge.svg)](https://github.com/max777-cyber/aura-outfit/actions/workflows/ci.yml)
[![CD](https://github.com/max777-cyber/aura-outfit/actions/workflows/cd.yml/badge.svg)](https://github.com/max777-cyber/aura-outfit/actions/workflows/cd.yml)

[![Java](https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot_4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org)
[![Hibernate](https://img.shields.io/badge/Hibernate_JPA-59666C?style=for-the-badge&logo=hibernate&logoColor=white)](https://hibernate.org)

[![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white)](https://developer.mozilla.org/docs/Web/HTML)
[![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white)](https://developer.mozilla.org/docs/Web/CSS)
[![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)](https://developer.mozilla.org/docs/Web/JavaScript)

[![Mercado Pago](https://img.shields.io/badge/Mercado_Pago-00B1EA?style=for-the-badge&logo=mercadopago&logoColor=white)](https://www.mercadopago.com.br/developers)
[![OAuth2 Google](https://img.shields.io/badge/OAuth2_Google-4285F4?style=for-the-badge&logo=google&logoColor=white)](https://developers.google.com/identity)
[![Caffeine](https://img.shields.io/badge/Caffeine_Cache-B07219?style=for-the-badge&logoColor=white)](https://github.com/ben-manes/caffeine)
[![JUnit 5](https://img.shields.io/badge/JUnit_5-25A162?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org)

[![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com)
[![Railway](https://img.shields.io/badge/Railway-131415?style=for-the-badge&logo=railway&logoColor=white)](https://railway.app)
[![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)](https://github.com/features/actions)

---

## Índice

- [Sobre o Projeto](#sobre-o-projeto)
- [Funcionalidades](#funcionalidades)
- [Stack Tecnológica](#stack-tecnológica)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Como Rodar Localmente](#como-rodar-localmente)
- [Deploy e CI/CD](#deploy-e-cicd)
- [Desafios Técnicos Resolvidos](#desafios-técnicos-resolvidos)
- [Autores](#autores)

---

## Sobre o Projeto

A **Aura Outfit** é um e-commerce de moda desenvolvido como projeto final de curso. A proposta foi ir além do trabalho acadêmico: construir uma aplicação web completa e colocá-la **no ar em um ambiente real de produção**, com foco em boas práticas, segurança e automação.

Hoje a loja roda no Railway com banco PostgreSQL gerenciado, e cada commit na `main` passa por build, testes e deploy automáticos pelo GitHub Actions.

O projeto foi desenvolvido em equipe:

| Área | Responsável |
|------|-------------|
| Back-end, segurança, pagamentos e infraestrutura | [@max777-cyber](https://github.com/max777-cyber) |
| Front-end (HTML, CSS e JavaScript) | [@duda-zip](https://github.com/duda-zip) |
| Testes e QA | Pedro Henrique Correia — [@correia44](https://github.com/correia44) |

---

## Funcionalidades

**Loja**

- Catálogo de produtos com busca e filtros por marca e gênero
- Página de produto com galeria de imagens, controle de estoque e avaliações (comentários com nota média)
- Carrinho de compras
- Checkout com Mercado Pago (Checkout Pro) e confirmação do pagamento via webhook
- Histórico de pedidos ("Meus pedidos")

**Conta e segurança**

- Cadastro com confirmação de e-mail e recuperação de senha
- Login tradicional (Spring Security) e login social com Google (OAuth2)
- Sessão persistida no banco de dados (Spring Session JDBC), preservada entre reinícios e deploys
- Rate limiting por IP nos endpoints de login e cadastro

**Administração**

- Rotas administrativas (perfil `ADMIN`) para gerenciar produtos e estoque

**Interface**

- Páginas responsivas em HTML, CSS e JavaScript, servidas pelo próprio Spring Boot

---

## Stack Tecnológica

| Camada | Tecnologia |
|--------|-----------|
| **Linguagem** | Java 21 |
| **Framework** | Spring Boot 4 — Web, Data JPA, Validation, Cache, Mail |
| **Segurança** | Spring Security, OAuth2 Client (Google), Spring Session JDBC |
| **Persistência** | Hibernate (JPA), PostgreSQL (produção), H2 em modo MySQL (desenvolvimento e testes) |
| **Cache** | Spring Cache + Caffeine |
| **Pagamentos** | Mercado Pago SDK Java 2.1.7 (Checkout Pro + webhook) |
| **E-mail** | Spring Mail / SMTP — confirmação de cadastro e recuperação de senha |
| **Produtividade** | Lombok |
| **Front-end** | HTML5, CSS3 (Grid, Flexbox, custom properties e media queries), JavaScript ES6+ com Fetch API, Google Fonts (Bebas Neue + DM Sans) |
| **Testes** | JUnit 5, Mockito, Spring Boot Test |
| **Build** | Maven (com Maven Wrapper) |
| **Infraestrutura** | Docker (build multi-stage), Docker Compose, Railway, GitHub Actions |

O front-end é composto por páginas estáticas servidas pelo próprio Spring Boot, que conversam com a API por `fetch` — sem framework de UI e sem etapa de build no cliente.

---

## Estrutura do Projeto

```
aura-outfit/
├── .github/workflows/            # CI (build + testes) e CD (deploy no Railway)
└── aura-outfit-main/
    ├── Dockerfile
    ├── docker-compose.yml
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/com/aura/aura_outfit/
        │   │   ├── config/       # Security, OAuth2, CORS e carga inicial de dados
        │   │   ├── controller/   # Endpoints REST (produto, carrinho, pedido, pagamento...)
        │   │   ├── dto/          # Objetos de entrada e saída da API
        │   │   ├── exception/    # Exceções de negócio e handler global
        │   │   ├── model/        # Entidades JPA (Usuario, Produto, Pedido, Carrinho...)
        │   │   ├── repository/   # Repositórios Spring Data
        │   │   ├── security/     # Rate limiting e utilitários de sessão
        │   │   └── service/      # Regras de negócio
        │   └── resources/
        │       ├── static/       # Front-end (HTML, CSS, JS e imagens dos produtos)
        │       └── application.properties
        └── test/                 # Testes unitários dos services (JUnit 5 + Mockito)
```

---

## Como Rodar Localmente

### Pré-requisitos

- Java 21+
- (Opcional) Docker e Docker Compose
- (Opcional) Credenciais do Google Cloud Console, para testar o login com Google
- (Opcional) Access token do Mercado Pago Developers, para testar pagamentos

### 1. Clone o repositório

```bash
git clone https://github.com/max777-cyber/aura-outfit.git
cd aura-outfit/aura-outfit-main
```

### 2. Execute a aplicação

Sem nenhuma configuração, a aplicação sobe com um banco **H2 local** (arquivo em `./data`), então não é preciso instalar banco de dados:

```bash
./mvnw spring-boot:run
```

Acesse: http://localhost:8080

> Um usuário administrador é criado na inicialização com as credenciais definidas em `APP_ADMIN_EMAIL` / `APP_ADMIN_SENHA`.

### 3. (Opcional) Variáveis de ambiente

Para habilitar login com Google, pagamentos e envio de e-mails, configure as variáveis abaixo (no terminal ou em um arquivo `.env` dentro de `aura-outfit-main/`):

```env
# Google OAuth2
GOOGLE_CLIENT_ID=seu_client_id
GOOGLE_CLIENT_SECRET=seu_client_secret

# Mercado Pago (tokens TEST- usam o sandbox)
MERCADOPAGO_ACCESS_TOKEN=seu_access_token

# URL do front-end e origens permitidas no CORS
APP_FRONTEND_URL=http://localhost:8080
APP_CORS_ALLOWED_ORIGINS=http://localhost:8080
SESSION_COOKIE_SECURE=false

# Usuário administrador criado na inicialização
APP_ADMIN_EMAIL=admin@exemplo.com
APP_ADMIN_SENHA=troque_esta_senha

# SMTP (sem SMTP_HOST, os links de e-mail aparecem no log)
SMTP_HOST=
SMTP_PORT=587
SMTP_USER=
SMTP_PASS=
```

### 4. (Opcional) Rodando com Docker

O `docker-compose.yml` faz o build da imagem e sobe a aplicação com H2 persistido em volume (lê o arquivo `.env` acima):

```bash
docker compose up --build
```

### 5. Testes

```bash
./mvnw test
```

---

## Deploy e CI/CD

A aplicação está em produção no **Railway**, com banco **PostgreSQL** gerenciado:

👉 **https://aura-outfit-production-a01a.up.railway.app**

O pipeline roda no **GitHub Actions**:

1. **CI** ([`ci.yml`](.github/workflows/ci.yml)) — a cada push ou pull request na `main`, faz o build com Java 21 e roda os testes (`./mvnw verify`), publicando o relatório do Surefire.
2. **CD** ([`cd.yml`](.github/workflows/cd.yml)) — quando o CI passa na `main`, dispara o deploy automático no Railway via Railway CLI (`railway up`).

Em produção, o banco é configurado pelas variáveis `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_DATASOURCE_DRIVER_CLASS_NAME` e `JPA_PLATFORM`, apontando para o PostgreSQL do Railway.

---

## Desafios Técnicos Resolvidos

| Problema | Solução Aplicada |
|----------|-----------------|
| Sessões perdidas a cada restart/redeploy | Spring Session JDBC, com a sessão persistida no banco |
| URLs hardcoded quebrando em produção | URLs e origens configuradas por variáveis de ambiente |
| OAuth2 rejeitando o redirect em produção | Redirect URI do domínio do Railway registrado no Google Cloud Console + `forward-headers-strategy` |
| CORS bloqueando requisições | `CorsConfigurationSource` com as origens de produção |
| Lazy loading causando erros | Ajuste de `FetchType` e uso de `@Transactional` |
| Tentativas de login em massa | Filtro de rate limiting por IP nos endpoints sensíveis |

---

## Autores

**Maximillian Benjamin Vicente** — Back-end, segurança, integrações e deploy  
Estudante de Análise e Desenvolvimento de Sistemas — Cruzeiro do Sul (prev. 2028)  
Técnico em TI — CEFSA (prev. 2026)

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://linkedin.com/in/maximillian-benjamin-vicente)
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/max777-cyber)

**Maria Eduarda Pereira Santos** — Front-end (HTML, CSS e JavaScript), interface e responsividade  
Técnica em Informática — Colégio Engenheiro Salvador Arena (prev. 2026)

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/maria-eduarda-pereira-santos-7689073a7/)
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/duda-zip)

**Pedro Henrique Correia** — Testes e QA, validação de fluxos e reporte de bugs  

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/pedro-correia-476113437/)
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/correia44)
