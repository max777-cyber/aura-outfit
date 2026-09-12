CREATE DATABASE IF NOT EXISTS aura_outfit
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE aura_outfit;


CREATE TABLE usuarios (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    nome       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    senha      VARCHAR(255) NOT NULL,
    telefone   VARCHAR(50)  NULL,
    endereco   VARCHAR(500) NULL,
    documento  VARCHAR(50)  NULL,
    foto_perfil LONGTEXT     NULL,
    email_confirmado BOOLEAN NOT NULL DEFAULT FALSE,
    token_confirmacao_email VARCHAR(120) NULL,
    token_recuperacao_senha VARCHAR(120) NULL,
    token_recuperacao_expira DATETIME NULL,
    role       VARCHAR(20)  NOT NULL DEFAULT 'USER',

    PRIMARY KEY (id),
    UNIQUE KEY uq_usuario_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE produtos (
    id          BIGINT         NOT NULL AUTO_INCREMENT,
    nome        VARCHAR(255)   NOT NULL,
    marca       VARCHAR(255)   NULL,
    cor         VARCHAR(100)   NULL,
    tamanho     VARCHAR(50)    NULL,
    genero      VARCHAR(50)    NULL,
    preco       DOUBLE         NOT NULL,
    imagem_url  VARCHAR(500)   NULL,   -- primeira imagem (thumbnail / fallback)

    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE produto_imagens (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    produto_id  BIGINT        NOT NULL,
    url         VARCHAR(500)  NOT NULL,
    ordem       INT           NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    CONSTRAINT fk_img_produto
        FOREIGN KEY (produto_id) REFERENCES produtos (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE estoque_produto (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    produto_id  BIGINT      NOT NULL,
    tamanho     VARCHAR(20) NOT NULL,
    quantidade  INT         NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uq_estoque_produto_tamanho (produto_id, tamanho),
    CONSTRAINT fk_estoque_produto
        FOREIGN KEY (produto_id) REFERENCES produtos (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE carrinhos (
    id          BIGINT  NOT NULL AUTO_INCREMENT,
    usuario_id  BIGINT  NOT NULL,
    total       DOUBLE  NOT NULL DEFAULT 0.0,

    PRIMARY KEY (id),
    UNIQUE KEY uq_carrinho_usuario (usuario_id),
    CONSTRAINT fk_carrinho_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE itens_carrinho (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    carrinho_id  BIGINT      NOT NULL,
    produto_id   BIGINT      NOT NULL,
    quantidade   INT         NOT NULL,
    tamanho      VARCHAR(20) NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_item_carrinho_carrinho
        FOREIGN KEY (carrinho_id) REFERENCES carrinhos (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_item_carrinho_produto
        FOREIGN KEY (produto_id) REFERENCES produtos (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE pedidos (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    usuario_id   BIGINT       NOT NULL,
    valor_total  DOUBLE       NOT NULL DEFAULT 0.0,
    status       VARCHAR(50)  NOT NULL DEFAULT 'Pendente',
    data_pedido  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_pedido_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE itens_pedido (
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    pedido_id        BIGINT      NOT NULL,
    produto_id       BIGINT      NOT NULL,
    quantidade       INT         NOT NULL,
    preco_unitario   DOUBLE      NOT NULL,
    tamanho          VARCHAR(20) NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_item_pedido_pedido
        FOREIGN KEY (pedido_id) REFERENCES pedidos (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_item_pedido_produto
        FOREIGN KEY (produto_id) REFERENCES produtos (id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE comentarios (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    produto_id    BIGINT       NOT NULL,
    usuario_id    BIGINT       NOT NULL,
    texto         VARCHAR(500) NOT NULL,
    nota          INT          NOT NULL,
    data_criacao  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_comentario_produto_usuario (produto_id, usuario_id),
    CONSTRAINT fk_comentario_produto
        FOREIGN KEY (produto_id) REFERENCES produtos (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_comentario_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


INSERT INTO usuarios (nome, email, senha, role, email_confirmado)
VALUES (
    'Administrador',
    'adm@aura.local',
    '$2a$12$placeholder_sera_substituido_pelo_spring_ao_iniciar____',
    'ADMIN',
    TRUE
)
ON DUPLICATE KEY UPDATE role = 'ADMIN', email_confirmado = TRUE;

INSERT INTO produtos (nome, marca, cor, genero, preco, imagem_url) VALUES
-- 1. BonÃ©
('BonÃ© Aura Outfit',  'Aura', 'Preto', 'Unissex',  59.90, '/imagens/bone/1.png'),
-- 2. CalÃ§a Cargo Preta
('CalÃ§a Cargo Preta', 'Aura', 'Preto', 'Masculino', 159.90, '/imagens/calca-cargo/1.png'),
-- 3. CalÃ§a Cargo Jeans
('CalÃ§a Cargo Jeans', 'Aura', 'Jeans', 'Masculino', 189.90, '/imagens/calca-cargo-jeans/1.png'),
-- 4. Oversized Aura Dark
('Oversized Aura Dark',    'Aura', 'Branco', 'Unissex', 89.90, '/imagens/camisa-1/1.png'),
-- 5. Oversized Aura Shadow
('Oversized Aura Shadow',    'Aura', 'Preto',  'Unissex', 89.90, '/imagens/camisa-2/1.png'),
-- 6. Camisa Oversized Aura
('Camisa Oversized Aura', 'Aura', 'Verde',  'Unissex', 89.90, '/imagens/camisa-3/1.png'),
-- 7. Jaqueta Colegial
('Jaqueta Colegial', 'Aura', 'Preto', 'Unissex', 249.90, '/imagens/jaqueta-colegial/1.png'),
-- 8. Jaqueta Jeans
('Jaqueta Jeans',    'Aura', 'Jeans', 'Unissex', 279.90, '/imagens/jaqueta-jeans/1.png'),
-- 9. Moletom com Toca
('Moletom com Toca', 'Aura', 'Cinza', 'Unissex', 219.90, '/imagens/moletom-toca/1.png'),
-- 10. Corta Vento Aura
('Corta Vento Aura', 'Aura', 'Preto/Roxo', 'Unissex', 199.90, '/imagens/corta-vento-aura/1.png'),
-- 11. Tênis Aura Runner
('Tênis Aura Runner', 'Aura', 'Preto/Azul', 'Unissex', 299.90, '/imagens/tenis-aura-runner/1.png'),
-- 12. Blusa Roxa Aura
('Blusa Roxa Aura', 'Aura', 'Roxo', 'Unissex', 189.90, '/imagens/blusa-roxa-aura/1.png');


INSERT INTO produto_imagens (produto_id, url, ordem) VALUES
-- 1. BonÃ© (2 fotos)
(1, '/imagens/bone/1.png', 1),
(1, '/imagens/bone/2.png', 2),

-- 2. CalÃ§a Cargo Preta (4 fotos)
(2, '/imagens/calca-cargo/1.png', 1),
(2, '/imagens/calca-cargo/2.png', 2),
(2, '/imagens/calca-cargo/3.png', 3),
(2, '/imagens/calca-cargo/4.png', 4),

-- 3. CalÃ§a Cargo Jeans (4 fotos)
(3, '/imagens/calca-cargo-jeans/1.png', 1),
(3, '/imagens/calca-cargo-jeans/2.png', 2),
(3, '/imagens/calca-cargo-jeans/3.png', 3),
(3, '/imagens/calca-cargo-jeans/4.png', 4),

-- 4. Oversized Aura Dark (3 fotos)
(4, '/imagens/camisa-1/1.png', 1),
(4, '/imagens/camisa-1/2.png', 2),
(4, '/imagens/camisa-1/3.png', 3),

-- 5. Oversized Aura Shadow (4 fotos)
(5, '/imagens/camisa-2/1.png', 1),
(5, '/imagens/camisa-2/2.png', 2),
(5, '/imagens/camisa-2/3.png', 3),
(5, '/imagens/camisa-2/4.png', 4),

-- 6. Camisa Oversized Aura (3 fotos)
(6, '/imagens/camisa-3/1.png', 1),
(6, '/imagens/camisa-3/2.png', 2),
(6, '/imagens/camisa-3/3.png', 3),

-- 7. Jaqueta Colegial (4 fotos)
(7, '/imagens/jaqueta-colegial/1.png', 1),
(7, '/imagens/jaqueta-colegial/2.png', 2),
(7, '/imagens/jaqueta-colegial/3.png', 3),
(7, '/imagens/jaqueta-colegial/4.png', 4),

-- 8. Jaqueta Jeans (3 fotos)
(8, '/imagens/jaqueta-jeans/1.png', 1),
(8, '/imagens/jaqueta-jeans/2.png', 2),
(8, '/imagens/jaqueta-jeans/3.png', 3),

-- 9. Moletom com Toca (3 fotos)
(9, '/imagens/moletom-toca/1.png', 1),
(9, '/imagens/moletom-toca/2.png', 2),
(9, '/imagens/moletom-toca/3.png', 3),

-- 10. Corta Vento Aura (5 fotos)
(10, '/imagens/corta-vento-aura/1.png', 1),
(10, '/imagens/corta-vento-aura/2.png', 2),
(10, '/imagens/corta-vento-aura/3.png', 3),
(10, '/imagens/corta-vento-aura/4.png', 4),
(10, '/imagens/corta-vento-aura/5.png', 5),

-- 11. Tênis Aura Runner (3 fotos)
(11, '/imagens/tenis-aura-runner/1.png', 1),
(11, '/imagens/tenis-aura-runner/3.png', 2),
(11, '/imagens/tenis-aura-runner/4.png', 3),

-- 12. Blusa Roxa Aura (1 foto)
(12, '/imagens/blusa-roxa-aura/1.png', 1);


INSERT INTO estoque_produto (produto_id, tamanho, quantidade) VALUES

-- 1. BonÃ© (tamanho Ãºnico)
(1, 'Ãšnico', 50),

-- 2. CalÃ§a Cargo Preta
(2, 'P',  10), (2, 'M',  15), (2, 'G',  12), (2, 'GG',  8),

-- 3. CalÃ§a Cargo Jeans
(3, 'P',   8), (3, 'M',  12), (3, 'G',  10), (3, 'GG',  6),

-- 4. Oversized Aura Dark
(4, 'PP', 10), (4, 'P',  20), (4, 'M',  25), (4, 'G',  20), (4, 'GG', 10),

-- 5. Oversized Aura Shadow
(5, 'PP',  8), (5, 'P',  18), (5, 'M',  22), (5, 'G',  18), (5, 'GG',  8),

-- 6. Camisa Oversized Aura
(6, 'PP',  6), (6, 'P',  15), (6, 'M',  20), (6, 'G',  15), (6, 'GG',  6),

-- 7. Jaqueta Colegial
(7, 'P',  10), (7, 'M',  12), (7, 'G',  10), (7, 'GG',  5),

-- 8. Jaqueta Jeans
(8, 'P',   8), (8, 'M',  10), (8, 'G',   8), (8, 'GG',  4),

-- 9. Moletom com Toca
(9, 'P',  12), (9, 'M',  18), (9, 'G',  15), (9, 'GG',  8),

-- 10. Corta Vento Aura
(10, 'P', 8), (10, 'M', 12), (10, 'G', 10), (10, 'GG', 5),

-- 11. Tênis Aura Runner
(11, '38', 7), (11, '39', 9), (11, '40', 12), (11, '41', 10), (11, '42', 8), (11, '43', 5),

-- 12. Blusa Roxa Aura
(12, 'PP', 8), (12, 'P', 16), (12, 'M', 18), (12, 'G', 12), (12, 'GG', 6);


SELECT p.id, p.nome, p.preco, p.genero,
       COUNT(pi.id) AS qtd_fotos,
       GROUP_CONCAT(e.tamanho ORDER BY e.tamanho SEPARATOR ' | ') AS tamanhos
FROM produtos p
LEFT JOIN produto_imagens pi ON pi.produto_id = p.id
LEFT JOIN estoque_produto  e  ON e.produto_id  = p.id
GROUP BY p.id, p.nome, p.preco, p.genero;




