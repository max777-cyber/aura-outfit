package com.aura.aura_outfit.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.aura.aura_outfit.dto.CadastroDTO;
import com.aura.aura_outfit.dto.UsuarioResponseDTO;
import com.aura.aura_outfit.dto.PerfilDTO;
import com.aura.aura_outfit.exception.CredenciaisInvalidasException;
import com.aura.aura_outfit.exception.EmailJaCadastradoException;
import com.aura.aura_outfit.exception.UsuarioNaoEncontradoException;
import com.aura.aura_outfit.model.Usuario;
import com.aura.aura_outfit.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.frontend.url:http://localhost:8080}")
    private String frontendUrl;

    /**
     * Hash dummy pra mitigar timing attack no login.
     * Quando o email não existe, ainda rodamos um matches() contra esse hash
     * pra gastar o mesmo tempo que rodaríamos contra um hash real.
     *
     * Este é o BCrypt de uma string qualquer — não tem significado.
     */
    private static final String DUMMY_HASH =
            "$2a$12$CwTycUXWue0Thq9StjUM0uJ8S4bYi4t7Ul1nFZR3MZ3NZxZK5GZZ2";

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * ✅ Sobrecarga que aceita o DTO validado pelo @Valid.
     *    Mantém o cadastrar(Usuario) por compatibilidade (AdminInitializer usa).
     */
    public UsuarioResponseDTO cadastrar(CadastroDTO dto) {
        Usuario u = new Usuario();
        u.setNome(dto.getNome().trim());
        u.setEmail(dto.getEmail().trim().toLowerCase());
        u.setSenha(dto.getSenha());
        u.setTelefone(dto.getTelefone());
        u.setDocumento(dto.getDocumento());
        u.setEndereco(dto.getEndereco());
        return cadastrar(u);
    }

    /**
     * Cadastra um usuário comum.
     *
     * ✅ Força role=USER ignorando qualquer valor vindo do request body.
     *    Impede mass assignment ("role":"ADMIN" no JSON).
     */
    public UsuarioResponseDTO cadastrar(Usuario usuario) {
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        if (usuario.getSenha() == null || usuario.getSenha().length() < 6) {
            throw new IllegalArgumentException("Senha deve ter pelo menos 6 caracteres");
        }

        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new EmailJaCadastradoException();
        }

        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        usuario.setRole("USER");     // ✅ sempre USER, nunca confiar no cliente
        usuario.setId(null);         // ✅ impede sobrescrita via ID no body
        usuario.setEmailConfirmado(true);
        usuario.setTokenConfirmacaoEmail(null);
        usuario.setTokenRecuperacaoSenha(null);
        usuario.setTokenRecuperacaoExpira(null);

        return new UsuarioResponseDTO(usuarioRepository.save(usuario));
    }

    /**
     * Login com mitigação de timing attack.
     * Retorna a entidade completa (com role) pro controller salvar na sessão.
     */
    public Usuario loginComGoogle(String email, String nome, String fotoPerfil) {
        if (email == null || email.isBlank()) {
            throw new CredenciaisInvalidasException();
        }

        String emailNormalizado = email.trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByEmail(emailNormalizado).orElse(null);

        if (usuario == null) {
            usuario = new Usuario();
            usuario.setEmail(emailNormalizado);
            usuario.setNome((nome == null || nome.isBlank()) ? emailNormalizado : nome.trim());
            usuario.setSenha(passwordEncoder.encode("GOOGLE_LOGIN_" + UUID.randomUUID()));
            usuario.setRole("USER");
        } else if (nome != null && !nome.isBlank()) {
            usuario.setNome(nome.trim());
        }

        if (fotoPerfil != null && !fotoPerfil.isBlank()) {
            usuario.setFotoPerfil(fotoPerfil);
        }

        usuario.setEmailConfirmado(true);
        usuario.setTokenConfirmacaoEmail(null);
        usuario.setTokenRecuperacaoSenha(null);
        usuario.setTokenRecuperacaoExpira(null);

        return usuarioRepository.save(usuario);
    }

    public Usuario login(String email, String senha) {
        if (email == null || senha == null) {
            throw new CredenciaisInvalidasException();
        }

        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

        if (usuario == null) {
            // ✅ Roda BCrypt de qualquer jeito pra manter tempo constante
            passwordEncoder.matches(senha, DUMMY_HASH);
            throw new CredenciaisInvalidasException();
        }

        if (!passwordEncoder.matches(senha, usuario.getSenha())) {
            throw new CredenciaisInvalidasException();
        }

        if (!usuario.isEmailConfirmado()) {
            usuario.setEmailConfirmado(true);
            usuario.setTokenConfirmacaoEmail(null);
            usuarioRepository.save(usuario);
        }

        return usuario;
    }

    public void confirmarEmail(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token invalido");
        }

        Usuario usuario = usuarioRepository.findByTokenConfirmacaoEmail(token)
                .orElseThrow(() -> new IllegalArgumentException("Token invalido ou ja usado"));

        usuario.setEmailConfirmado(true);
        usuario.setTokenConfirmacaoEmail(null);
        usuarioRepository.save(usuario);
    }

    public void solicitarRecuperacaoSenha(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        Optional<Usuario> encontrado = usuarioRepository.findByEmail(email.trim().toLowerCase());
        if (encontrado.isEmpty()) {
            return;
        }

        Usuario usuario = encontrado.get();
        usuario.setTokenRecuperacaoSenha(gerarToken());
        usuario.setTokenRecuperacaoExpira(LocalDateTime.now().plusMinutes(30));
        usuarioRepository.save(usuario);

        emailService.enviarRecuperacaoSenha(
                usuario.getEmail(),
                usuario.getNome(),
                frontendUrl + "/redefinir-senha.html?token=" + usuario.getTokenRecuperacaoSenha()
        );
    }

    public void redefinirSenha(String token, String senha) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token invalido");
        }
        if (senha == null || senha.length() < 8) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 8 caracteres");
        }
        if (!senha.matches(".*[A-Za-z].*") || !senha.matches(".*\\d.*")) {
            throw new IllegalArgumentException("A senha deve conter pelo menos uma letra e um numero");
        }

        Usuario usuario = usuarioRepository.findByTokenRecuperacaoSenha(token)
                .orElseThrow(() -> new IllegalArgumentException("Token invalido ou expirado"));

        LocalDateTime expira = usuario.getTokenRecuperacaoExpira();
        if (expira == null || expira.isBefore(LocalDateTime.now())) {
            usuario.setTokenRecuperacaoSenha(null);
            usuario.setTokenRecuperacaoExpira(null);
            usuarioRepository.save(usuario);
            throw new IllegalArgumentException("Token expirado. Solicite outro link.");
        }

        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setTokenRecuperacaoSenha(null);
        usuario.setTokenRecuperacaoExpira(null);
        usuarioRepository.save(usuario);
    }

    public List<UsuarioResponseDTO> listar() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioResponseDTO::new)
                .toList();
    }

    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));
        return new UsuarioResponseDTO(usuario);
    }

    /**
     * Atualiza dados básicos.
     *
     * ✅ Só atualiza campos permitidos. role, id, documento (CPF) não são
     *    alterados aqui — impede escalation via PUT.
     */
    public UsuarioResponseDTO atualizar(Long id, Usuario dadosNovos) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));

        if (dadosNovos.getNome() != null && !dadosNovos.getNome().isBlank()) {
            usuario.setNome(dadosNovos.getNome());
        }
        if (dadosNovos.getEmail() != null && !dadosNovos.getEmail().isBlank()
                && !dadosNovos.getEmail().equals(usuario.getEmail())) {
            // se for mudar email, garante que não existe outro usuário com ele
            if (usuarioRepository.findByEmail(dadosNovos.getEmail()).isPresent()) {
                throw new EmailJaCadastradoException();
            }
            usuario.setEmail(dadosNovos.getEmail());
        }
        if (dadosNovos.getSenha() != null && !dadosNovos.getSenha().isBlank()) {
            if (dadosNovos.getSenha().length() < 6) {
                throw new IllegalArgumentException("Senha deve ter pelo menos 6 caracteres");
            }
            usuario.setSenha(passwordEncoder.encode(dadosNovos.getSenha()));
        }
        // ❌ role, id e documento NÃO são alterados aqui

        return new UsuarioResponseDTO(usuarioRepository.save(usuario));
    }

    public Usuario buscarEntidadePorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));
    }

    public PerfilDTO atualizarDadosPessoais(Long id, String nome, String telefone, String documento, String endereco) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));

        if (nome != null && !nome.isBlank()) {
            usuario.setNome(nome);
        }
        if (telefone != null) {
            usuario.setTelefone(telefone.isBlank() ? null : telefone);
        }
        if (documento != null) {
            usuario.setDocumento(documento.isBlank() ? null : documento);
        }
        if (endereco != null) {
            usuario.setEndereco(endereco.isBlank() ? null : endereco);
        }

        return new PerfilDTO(usuarioRepository.save(usuario));
    }

    public PerfilDTO atualizarFotoPerfil(Long id, String fotoPerfil) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));

        if (fotoPerfil != null && !fotoPerfil.isBlank()) {
            if (!fotoPerfil.startsWith("data:image/jpeg;base64,")
                    && !fotoPerfil.startsWith("data:image/png;base64,")
                    && !fotoPerfil.startsWith("data:image/webp;base64,")) {
                throw new IllegalArgumentException("Formato de imagem invalido. Use JPEG, PNG ou WebP.");
            }
            if (fotoPerfil.length() > 800_000) {
                throw new IllegalArgumentException("Imagem muito grande. Escolha uma foto menor que 600 KB.");
            }
            usuario.setFotoPerfil(fotoPerfil);
        } else {
            usuario.setFotoPerfil(null);
        }

        return new PerfilDTO(usuarioRepository.save(usuario));
    }

    public void deletar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new UsuarioNaoEncontradoException(id);
        }
        usuarioRepository.deleteById(id);
    }

    private String gerarToken() {
        return UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
    }
}
