package com.aura.aura_outfit.security;

import com.aura.aura_outfit.model.Usuario;
import com.aura.aura_outfit.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

/**
 * Utilitário central pra autorização baseada em sessão.
 *
 * Motivação: antes, cada controller fazia seu próprio `session.getAttribute(...)`
 * e alguns esqueciam (resultando em IDOR). Agora tudo passa por aqui.
 */
@Component
public class SessionUtil {

    private final UsuarioRepository usuarioRepository;

    public SessionUtil(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /** Retorna o ID do usuário logado, ou null se não houver sessão. */
    public Long getUsuarioIdLogado(HttpSession session) {
        if (session == null) return null;
        return (Long) session.getAttribute("usuarioId");
    }

    /** Retorna o papel do usuário logado, ou null. */
    public String getRoleLogada(HttpSession session) {
        if (session == null) return null;
        return (String) session.getAttribute("usuarioRole");
    }

    /** Lança UnauthorizedException se não houver sessão. Retorna o ID logado. */
    public Long exigirLogado(HttpSession session) {
        Long id = getUsuarioIdLogado(session);
        if (id == null) throw new UnauthorizedException("Faça login para continuar");
        return id;
    }

    /** Lança ForbiddenException se o usuário logado não for admin. */
    public void exigirAdmin(HttpSession session) {
        exigirLogado(session);
        if (!"ADMIN".equalsIgnoreCase(getRoleLogada(session))) {
            throw new ForbiddenException("Acesso restrito a administradores");
        }
    }

    /**
     * Garante que o usuário logado é o dono do recurso OU é admin.
     * Use em todos os endpoints que recebem usuarioId/pedidoId/carrinhoId.
     */
    public void exigirDonoOuAdmin(HttpSession session, Long donoDoRecursoId) {
        Long logadoId = exigirLogado(session);
        boolean ehDono = logadoId.equals(donoDoRecursoId);
        boolean ehAdmin = "ADMIN".equalsIgnoreCase(getRoleLogada(session));
        if (!ehDono && !ehAdmin) {
            throw new ForbiddenException("Você não tem permissão para acessar esse recurso");
        }
    }

    /** Busca o usuário logado completo, ou lança UnauthorizedException. */
    public Usuario getUsuarioLogado(HttpSession session) {
        Long id = exigirLogado(session);
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UnauthorizedException("Sessão inválida"));
    }

    // ─── Exceções de autorização (tratadas no GlobalExceptionHandler) ───

    public static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String msg) { super(msg); }
    }

    public static class ForbiddenException extends RuntimeException {
        public ForbiddenException(String msg) { super(msg); }
    }
}