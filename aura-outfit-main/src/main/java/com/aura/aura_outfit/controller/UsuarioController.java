package com.aura.aura_outfit.controller;

import com.aura.aura_outfit.dto.CadastroDTO;
import com.aura.aura_outfit.dto.LoginDTO;
import com.aura.aura_outfit.dto.RecuperarSenhaDTO;
import com.aura.aura_outfit.dto.RedefinirSenhaDTO;
import com.aura.aura_outfit.dto.UsuarioResponseDTO;
import com.aura.aura_outfit.dto.PerfilDTO;
import com.aura.aura_outfit.model.Pedido;
import com.aura.aura_outfit.model.Usuario;
import com.aura.aura_outfit.security.SessionUtil;
import com.aura.aura_outfit.service.PedidoService;
import com.aura.aura_outfit.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PedidoService pedidoService;
    private final SessionUtil sessionUtil;

    public UsuarioController(UsuarioService usuarioService,
                             PedidoService pedidoService,
                             SessionUtil sessionUtil) {
        this.usuarioService = usuarioService;
        this.pedidoService = pedidoService;
        this.sessionUtil = sessionUtil;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@Valid @RequestBody CadastroDTO dto) {
        return ResponseEntity.status(201).body(usuarioService.cadastrar(dto));
    }

    @GetMapping("/confirmar-email")
    public ResponseEntity<Map<String, String>> confirmarEmail(@RequestParam String token) {
        usuarioService.confirmarEmail(token);
        return ResponseEntity.ok(Map.of("mensagem", "Email confirmado com sucesso."));
    }

    @PostMapping("/recuperar-senha")
    public ResponseEntity<Map<String, String>> recuperarSenha(@Valid @RequestBody RecuperarSenhaDTO dto) {
        usuarioService.solicitarRecuperacaoSenha(dto.getEmail());
        return ResponseEntity.ok(Map.of(
                "mensagem",
                "Se esse email estiver cadastrado, enviaremos um link de recuperacao."
        ));
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<Map<String, String>> redefinirSenha(@Valid @RequestBody RedefinirSenhaDTO dto) {
        usuarioService.redefinirSenha(dto.getToken(), dto.getSenha());
        return ResponseEntity.ok(Map.of("mensagem", "Senha redefinida com sucesso."));
    }

    /**
     * Login com proteção contra session fixation:
     * - Invalida a sessão existente (se houver)
     * - Cria uma nova sessão com os dados do usuário
     */
    @PostMapping("/login")
    public ResponseEntity<UsuarioResponseDTO> login(
            @Valid @RequestBody LoginDTO dto,
            HttpServletRequest request) {

        Usuario usuario = usuarioService.login(dto.getEmail(), dto.getSenha());

        // ✅ Mata qualquer sessão anterior pra prevenir fixation
        HttpSession antiga = request.getSession(false);
        if (antiga != null) {
            antiga.invalidate();
        }

        HttpSession nova = request.getSession(true);
        nova.setAttribute("usuarioId", usuario.getId());
        nova.setAttribute("usuarioNome", usuario.getNome());
        nova.setAttribute("usuarioEmail", usuario.getEmail());
        nova.setAttribute("usuarioRole", usuario.getRole());

        return ResponseEntity.ok(new UsuarioResponseDTO(usuario));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession s = request.getSession(false);
        if (s != null) s.invalidate();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sessao")
    public ResponseEntity<Map<String, Object>> sessao(HttpSession session) {
        Long id = sessionUtil.getUsuarioIdLogado(session);
        if (id == null) {
            return ResponseEntity.ok(Map.of("logado", false));
        }
        Usuario usuario = usuarioService.buscarEntidadePorId(id);
        return ResponseEntity.ok(Map.of(
                "logado", true,
                "id", id,
                "nome", usuario.getNome(),
                "email", usuario.getEmail(),
                "role", usuario.getRole(),
                "emailConfirmado", usuario.isEmailConfirmado(),
                "fotoPerfil", usuario.getFotoPerfil() == null ? "" : usuario.getFotoPerfil()
        ));
    }

    /** Listar todos — só admin */
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listar(HttpSession session) {
        sessionUtil.exigirAdmin(session);
        return ResponseEntity.ok(usuarioService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id, HttpSession session) {
        sessionUtil.exigirDonoOuAdmin(session, id);
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(
            @PathVariable Long id,
            @RequestBody Usuario dadosNovos,
            HttpSession session) {

        sessionUtil.exigirDonoOuAdmin(session, id);
        UsuarioResponseDTO atualizado = usuarioService.atualizar(id, dadosNovos);
        session.setAttribute("usuarioNome", atualizado.getNome());
        session.setAttribute("usuarioEmail", atualizado.getEmail());
        return ResponseEntity.ok(atualizado);
    }

    @GetMapping("/{id}/perfil")
    public ResponseEntity<PerfilDTO> buscarPerfil(@PathVariable Long id, HttpSession session) {
        sessionUtil.exigirDonoOuAdmin(session, id);
        Usuario u = usuarioService.buscarEntidadePorId(id);
        return ResponseEntity.ok(new PerfilDTO(u));
    }

    @PutMapping("/{id}/perfil")
    public ResponseEntity<PerfilDTO> atualizarPerfil(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpSession session) {

        sessionUtil.exigirDonoOuAdmin(session, id);
        PerfilDTO atualizado = usuarioService.atualizarDadosPessoais(
                id,
                body.get("nome"),
                body.get("telefone"),
                body.get("documento"),
                body.get("endereco")
        );
        if (atualizado.getNome() != null) {
            session.setAttribute("usuarioNome", atualizado.getNome());
        }
        return ResponseEntity.ok(atualizado);
    }

    @PutMapping("/{id}/perfil/foto")
    public ResponseEntity<PerfilDTO> atualizarFotoPerfil(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpSession session) {

        sessionUtil.exigirDonoOuAdmin(session, id);
        return ResponseEntity.ok(usuarioService.atualizarFotoPerfil(id, body.get("fotoPerfil")));
    }

    /** Deletar usuário — só admin, e proíbe deletar a si mesmo */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id, HttpSession session) {
        sessionUtil.exigirAdmin(session);
        Long logadoId = sessionUtil.getUsuarioIdLogado(session);
        if (logadoId != null && logadoId.equals(id)) {
            throw new IllegalArgumentException("Você não pode deletar sua própria conta de admin");
        }
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pedidos")
    public ResponseEntity<List<Pedido>> getPedidos(@PathVariable Long id, HttpSession session) {
        sessionUtil.exigirDonoOuAdmin(session, id);
        return ResponseEntity.ok(pedidoService.buscarPorUsuario(id));
    }
}
