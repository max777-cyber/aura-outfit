# Aura Outfit — E-commerce de Moda

> Plataforma de e-commerce de roupas desenvolvida em Java com Spring Boot, autenticacao OAuth2 via Google, integracao com Mercado Pago e deploy em producao no Railway.
>
> [![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com)
> [![Spring Boot](https://img.shields.io/badge/Spring_Boot-3-6DB33F?style=for-the-badge&logo=spring-boot)](https://spring.io/projects/spring-boot)
> [![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org)
> [![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com)
> [![Railway](https://img.shields.io/badge/Railway-131415?style=for-the-badge&logo=railway&logoColor=white)](https://railway.app)
>
> ---
>
> ## Sobre o Projeto
>
> A **Aura Outfit** e um sistema completo de e-commerce de moda desenvolvido como projeto final de curso. O objetivo foi construir uma aplicacao web funcional com foco em boas praticas de desenvolvimento, seguranca e deploy em ambiente real de producao.
>
> ---
>
> ## Funcionalidades
>
> - Catalogo de produtos com listagem e detalhamento de pecas
> - - Autenticacao segura via Google OAuth2 (Login Social) e formulario tradicional com Spring Security
>   - - Carrinho de compras com gerenciamento de sessao persistente
>     - - Pagamento integrado com Mercado Pago (Checkout Pro)
>       - - Painel administrativo para gerenciamento de produtos e pedidos
>         - - Interface responsiva desenvolvida com HTML, CSS e JavaScript
>          
>           - ---
>
> ## Stack Tecnologica
>
> | Camada | Tecnologia |
> |--------|-----------|
> | **Back-end** | Java 17, Spring Boot 3, Spring MVC |
> | **Seguranca** | Spring Security, OAuth2 (Google), Spring Session JDBC |
> | **Banco de Dados** | PostgreSQL (producao), MySQL (desenvolvimento) |
> | **Pagamentos** | Mercado Pago SDK |
> | **Front-end** | Thymeleaf, HTML5, CSS3, JavaScript |
> | **Infraestrutura** | Docker, Docker Compose, Railway |
> | **Build** | Maven |
>
> ---
>
> ## Como Rodar Localmente
>
> ### Pre-requisitos
>
> - Java 17+
> - - Maven 3.8+
>   - - Docker e Docker Compose
>     - - Conta no Google Cloud Console (para OAuth2)
>       - - Conta no Mercado Pago Developers
>        
>         - ### 1. Clone o repositorio
>        
>         - ```bash
>           git clone https://github.com/max777-cyber/aura-outfit.git
>           cd aura-outfit/aura-outfit-main
>           ```
>
> ### 2. Configure as variaveis de ambiente
>
> Crie um arquivo `.env` na raiz do projeto:
>
> ```env
> DB_URL=jdbc:mysql://localhost:3306/aura_outfit
> DB_USERNAME=seu_usuario
> DB_PASSWORD=sua_senha
> GOOGLE_CLIENT_ID=seu_client_id
> GOOGLE_CLIENT_SECRET=seu_client_secret
> MP_ACCESS_TOKEN=seu_access_token
> APP_BASE_URL=http://localhost:8080
> ```
>
> ### 3. Suba o banco com Docker
>
> ```bash
> docker-compose up -d
> ```
>
> ### 4. Execute a aplicacao
>
> ```bash
> ./mvnw spring-boot:run
> ```
>
> Acesse: http://localhost:8080
>
> ---
>
> ## Deploy em Producao
>
> A aplicacao esta containerizada com **Docker** e publicada no **Railway**. Desafios resolvidos em producao:
>
> - URLs dinamicas via variavel `APP_BASE_URL` (eliminando URLs hardcoded)
> - - Configuracao de CORS para o dominio de producao
>   - - Persistencia de sessao com **Spring Session JDBC** (resolvendo perda de sessao entre pods)
>     - - Redirect URI do Google OAuth2 configurado para o dominio do Railway
>      
>       - ---
>
> ## Desafios Tecnicos Resolvidos
>
> | Problema | Solucao Aplicada |
> |----------|-----------------|
> | Perda de sessao em ambiente cloud | Spring Session JDBC com persistencia no banco |
> | URLs hardcoded quebrando em producao | Variaveis de ambiente para `BASE_URL` |
> | OAuth2 rejeitando redirect em producao | Registro do redirect URI no Google Cloud Console |
> | CORS bloqueando requisicoes | `CorsConfigurationSource` com dominio do Railway |
> | Lazy loading causando erros | Ajuste de `FetchType` e uso de `@Transactional` |
>
> ---
>
> ## Autor
>
> **Maximillian Benjamin Vicente**
> Estudante de Analise e Desenvolvimento de Sistemas — Cruzeiro do Sul (prev. 2028)
> Tecnico em TI — CEFSA (prev. 2026)
>
> [![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://linkedin.com/in/maximillian-benjamin-vicente)
> [![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/max777-cyber)
