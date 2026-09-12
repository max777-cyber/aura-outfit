package com.aura.aura_outfit.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class EmailConfirmadoMigration implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(EmailConfirmadoMigration.class);

    private final JdbcTemplate jdbcTemplate;

    public EmailConfirmadoMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        if (colunaEmailConfirmadoExiste()) {
            return;
        }

        log.info("Adicionando coluna usuarios.email_confirmado ao banco local existente");
        jdbcTemplate.execute("ALTER TABLE usuarios ADD COLUMN email_confirmado BOOLEAN DEFAULT FALSE");
        jdbcTemplate.execute("UPDATE usuarios SET email_confirmado = FALSE WHERE email_confirmado IS NULL");
        jdbcTemplate.execute("ALTER TABLE usuarios ALTER COLUMN email_confirmado SET NOT NULL");
    }

    private boolean colunaEmailConfirmadoExiste() {
        try {
            Integer total = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*)
                    FROM INFORMATION_SCHEMA.COLUMNS
                    WHERE UPPER(TABLE_NAME) = UPPER('usuarios')
                      AND UPPER(COLUMN_NAME) = UPPER('email_confirmado')
                    """,
                    Integer.class
            );
            return total != null && total > 0;
        } catch (Exception e) {
            return true;
        }
    }
}
