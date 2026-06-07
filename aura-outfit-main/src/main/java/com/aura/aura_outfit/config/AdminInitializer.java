package com.aura.aura_outfit.config;

import com.aura.aura_outfit.model.Usuario;
import com.aura.aura_outfit.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cria um usuário administrador padrão se ele ainda não existir.
 *
 * Login:   configurado via application.properties (app.admin.email)
 * Senha:   configurada via application.properties (app.admin.senha)
 *
 * ⚠️  SEGURANÇA: a senha padrão "130398" é FRACA (6 dígitos numéricos).
 *     Troque pelo painel de perfil assim que logar pela primeira vez,
 *     ou sobrescreva via variável de ambiente em produção.
 */
@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:adm@aura.local}")
    private String adminEmail;

    @Value("${app.admin.senha:130398}")
    private String adminSenha;

    @Value("${app.admin.nome:Administrador}")
    private String adminNome;

    public AdminInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        usuarioRepository.findByEmail(adminEmail).ifPresentOrElse(
            existente -> {
                boolean alterado = false;

                if (!"ADMIN".equalsIgnoreCase(existente.getRole())) {
                    existente.setRole("ADMIN");
                    alterado = true;
                    log.info("Usuario {} promovido a ADMIN", adminEmail);
                }

                if (!existente.isEmailConfirmado()) {
                    existente.setEmailConfirmado(true);
                    existente.setTokenConfirmacaoEmail(null);
                    alterado = true;
                }

                String senhaAtual = existente.getSenha();
                boolean senhaPlaceholder = senhaAtual == null
                        || senhaAtual.isBlank()
                        || senhaAtual.contains("placeholder")
                        || senhaAtual.startsWith("$2a$12$placeholder");
                boolean senhaPadraoAntiga = senhaAtual != null && passwordEncoder.matches("130398", senhaAtual);

                if (senhaPlaceholder || senhaPadraoAntiga) {
                    existente.setSenha(passwordEncoder.encode(adminSenha));
                    alterado = true;
                    log.warn("Senha do admin {} foi redefinida pela configuracao app.admin.senha", adminEmail);
                }

                if (alterado) {
                    usuarioRepository.save(existente);
                }

                log.info("Admin carregado: {} (role={})", existente.getEmail(), existente.getRole());
            },
            () -> {
                Usuario admin = new Usuario();
                admin.setNome(adminNome);
                admin.setEmail(adminEmail);
                admin.setSenha(passwordEncoder.encode(adminSenha));
                admin.setRole("ADMIN");
                admin.setEmailConfirmado(true);
                usuarioRepository.save(admin);
                log.warn("═══════════════════════════════════════════════════════════");
                log.warn("🔐 ADMIN criado: email={}", adminEmail);
                log.warn("   Senha padrão configurada. TROQUE EM PRODUÇÃO!");
                log.warn("═══════════════════════════════════════════════════════════");
            }
        );
    }
}
