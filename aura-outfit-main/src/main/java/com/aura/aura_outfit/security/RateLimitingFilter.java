package com.aura.aura_outfit.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiting para endpoints sensíveis (login e cadastro).
 *
 * Estratégia: janela deslizante simples por IP.
 *   - Login:    5 tentativas a cada 60 segundos
 *   - Cadastro: 3 tentativas a cada 5 minutos
 *
 * Implementação in-memory com ConcurrentHashMap. Suficiente pra app
 * single-instance (caso típico). Pra cluster/load balancer, trocar
 * por Redis (Bucket4j-redis ou similar).
 *
 * ⚠️  Sem isso, brute-force no login era trivial (mesmo com BCrypt
 *     de strength 12, alguém podia tentar milhares de senhas).
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    // Configurações por endpoint
    private static final RateConfig LOGIN_CONFIG    = new RateConfig(5, 60_000L);     // 5 / minuto
    private static final RateConfig CADASTRO_CONFIG = new RateConfig(3, 300_000L);    // 3 / 5 min

    // Mapas separados pra que login e cadastro tenham contadores independentes
    private final Map<String, Counter> loginAttempts    = new ConcurrentHashMap<>();
    private final Map<String, Counter> cadastroAttempts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                     HttpServletResponse res,
                                     FilterChain chain) throws ServletException, IOException {

        String path   = req.getRequestURI();
        String method = req.getMethod();

        if ("POST".equalsIgnoreCase(method)) {
            if (path.endsWith("/api/usuarios/login")) {
                if (excedeu(loginAttempts, getClienteIp(req), LOGIN_CONFIG)) {
                    bloqueio(res, "Muitas tentativas de login. Aguarde alguns minutos e tente novamente.");
                    return;
                }
            } else if (path.endsWith("/api/usuarios/cadastro")) {
                if (excedeu(cadastroAttempts, getClienteIp(req), CADASTRO_CONFIG)) {
                    bloqueio(res, "Muitas tentativas de cadastro. Aguarde alguns minutos.");
                    return;
                }
            }
        }

        chain.doFilter(req, res);
    }

    /**
     * Faz a contagem deslizante. True = excedeu.
     */
    private boolean excedeu(Map<String, Counter> mapa, String ip, RateConfig cfg) {
        long agora = Instant.now().toEpochMilli();

        // Limpeza preguiçosa: remove entradas expiradas se o mapa começar a crescer
        if (mapa.size() > 10_000) {
            mapa.entrySet().removeIf(e -> agora - e.getValue().firstAttemptMs > cfg.windowMs);
        }

        Counter counter = mapa.compute(ip, (k, existente) -> {
            if (existente == null || agora - existente.firstAttemptMs > cfg.windowMs) {
                // Janela expirou ou não existia — começa nova
                return new Counter(agora);
            }
            existente.attempts.incrementAndGet();
            return existente;
        });

        boolean bloqueado = counter.attempts.get() > cfg.maxRequests;
        if (bloqueado) {
            log.warn("⛔ Rate limit excedido | ip={} | tentativas={} | janela={}ms",
                    ip, counter.attempts.get(), cfg.windowMs);
        }
        return bloqueado;
    }

    /**
     * Obtém IP do cliente. Considera proxy reverso (X-Forwarded-For)
     * se a aplicação estiver atrás de Nginx, Cloudflare, Railway etc.
     */
    private String getClienteIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // X-Forwarded-For pode ter múltiplos IPs: "cliente, proxy1, proxy2"
            // o primeiro é o IP real do cliente
            return xff.split(",")[0].trim();
        }
        String xRealIp = req.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return req.getRemoteAddr();
    }

    private void bloqueio(HttpServletResponse res, String mensagem) throws IOException {
        res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        res.setHeader("Retry-After", "60");
        // ✅ JSON manual — evita dependência do Jackson ObjectMapper.
        //    Escapa aspas e barras na mensagem pra não quebrar o JSON.
        String mensagemSegura = mensagem.replace("\\", "\\\\").replace("\"", "\\\"");
        res.getWriter().write("{\"erro\":\"" + mensagemSegura + "\"}");
    }

    // ─── Estruturas auxiliares ────────────────────────────────────────────────

    private static class RateConfig {
        final int maxRequests;
        final long windowMs;
        RateConfig(int maxRequests, long windowMs) {
            this.maxRequests = maxRequests;
            this.windowMs = windowMs;
        }
    }

    private static class Counter {
        final long firstAttemptMs;
        final AtomicInteger attempts = new AtomicInteger(1);
        Counter(long firstAttemptMs) {
            this.firstAttemptMs = firstAttemptMs;
        }
    }
}