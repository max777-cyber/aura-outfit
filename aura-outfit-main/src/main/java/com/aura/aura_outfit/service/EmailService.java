package com.aura.aura_outfit.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.email.from:Aura Outfit <no-reply@aura.local>}")
    private String remetente;

    @Value("${spring.mail.host:}")
    private String mailHost;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    public void enviarConfirmacaoCadastro(String destino, String nome, String link) {
        String assunto = "Confirme seu cadastro na Aura Outfit";
        String corpo = """
                Oi, %s!

                Seu cadastro na Aura Outfit foi criado.
                Confirme seu e-mail clicando no link abaixo:

                %s

                Se voce nao criou essa conta, ignore esta mensagem.
                """.formatted(nome, link);

        enviarOuLogar(destino, assunto, corpo, link);
    }

    public void enviarRecuperacaoSenha(String destino, String nome, String link) {
        String assunto = "Recuperacao de senha - Aura Outfit";
        String corpo = """
                Oi, %s!

                Recebemos um pedido para redefinir sua senha.
                Clique no link abaixo para criar uma nova senha:

                %s

                Esse link expira em 30 minutos.
                Se voce nao pediu isso, ignore esta mensagem.
                """.formatted(nome, link);

        enviarOuLogar(destino, assunto, corpo, link);
    }

    private void enviarOuLogar(String destino, String assunto, String corpo, String link) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();

        if (mailSender == null || mailHost == null || mailHost.isBlank()) {
            log.warn("Email nao enviado porque o SMTP nao esta configurado. Destino={} Assunto={} Link={}",
                    destino, assunto, link);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(remetente);
            message.setTo(destino);
            message.setSubject(assunto);
            message.setText(corpo);
            mailSender.send(message);
            log.info("Email enviado para {}: {}", destino, assunto);
        } catch (RuntimeException e) {
            log.warn("Falha ao enviar email para {}. Link para teste: {}", destino, link, e);
        }
    }
}
