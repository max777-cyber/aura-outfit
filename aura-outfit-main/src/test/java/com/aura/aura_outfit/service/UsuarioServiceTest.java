package com.aura.aura_outfit.service;

import com.aura.aura_outfit.exception.CredenciaisInvalidasException;
import com.aura.aura_outfit.exception.EmailJaCadastradoException;
import com.aura.aura_outfit.model.Usuario;
import com.aura.aura_outfit.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;

    @InjectMocks
    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(usuarioService, "frontendUrl", "http://localhost:8080");
    }

    private Usuario criarUsuario(Long id, String email) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNome("Max");
        u.setEmail(email);
        u.setSenha("$2a$12$hashFake");
        u.setRole("USER");
        u.setEmailConfirmado(true);
        return u;
    }

    @Test
    @DisplayName("cadastrar: deve salvar usuario com role USER")
    void cadastrar_deveSalvarComRoleUser() {
        Usuario novo = criarUsuario(null, "max@email.com");
        novo.setSenha("senha123");
        when(usuarioRepository.findByEmail("max@email.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hashFake");
        when(usuarioRepository.save(any())).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        var resultado = usuarioService.cadastrar(novo);

        assertNotNull(resultado);
        verify(usuarioRepository).save(argThat(u -> "USER".equals(u.getRole())));
    }

    @Test
    @DisplayName("cadastrar: deve lancar excecao quando email ja cadastrado")
    void cadastrar_deveLancarExcecaoEmailDuplicado() {
        Usuario existente = criarUsuario(1L, "max@email.com");
        when(usuarioRepository.findByEmail("max@email.com")).thenReturn(Optional.of(existente));

        Usuario novo = criarUsuario(null, "max@email.com");
        novo.setSenha("senha123");

        assertThrows(EmailJaCadastradoException.class, () -> usuarioService.cadastrar(novo));
    }

    @Test
    @DisplayName("cadastrar: deve lancar excecao quando email e vazio")
    void cadastrar_deveLancarExcecaoEmailVazio() {
        Usuario novo = new Usuario();
        novo.setEmail("");
        novo.setSenha("senha123");

        assertThrows(IllegalArgumentException.class, () -> usuarioService.cadastrar(novo));
    }

    @Test
    @DisplayName("cadastrar: deve lancar excecao quando senha menor que 6 caracteres")
    void cadastrar_deveLancarExcecaoSenhaCurta() {
        Usuario novo = new Usuario();
        novo.setEmail("max@email.com");
        novo.setSenha("123");

        assertThrows(IllegalArgumentException.class, () -> usuarioService.cadastrar(novo));
    }

    @Test
    @DisplayName("login: deve retornar usuario quando credenciais corretas")
    void login_deveRetornarUsuarioComCredenciaisCorretas() {
        Usuario usuario = criarUsuario(1L, "max@email.com");
        when(usuarioRepository.findByEmail("max@email.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha123", "$2a$12$hashFake")).thenReturn(true);

        Usuario resultado = usuarioService.login("max@email.com", "senha123");

        assertNotNull(resultado);
        assertEquals("max@email.com", resultado.getEmail());
    }

    @Test
    @DisplayName("login: deve lancar excecao quando email nao existe")
    void login_deveLancarExcecaoEmailNaoExiste() {
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(CredenciaisInvalidasException.class,
                () -> usuarioService.login("naoexiste@email.com", "senha123"));
    }

    @Test
    @DisplayName("login: deve lancar excecao quando senha incorreta")
    void login_deveLancarExcecaoSenhaIncorreta() {
        Usuario usuario = criarUsuario(1L, "max@email.com");
        when(usuarioRepository.findByEmail("max@email.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senhaErrada", "$2a$12$hashFake")).thenReturn(false);

        assertThrows(CredenciaisInvalidasException.class,
                () -> usuarioService.login("max@email.com", "senhaErrada"));
    }

    @Test
    @DisplayName("deletar: deve chamar deleteById quando usuario existe")
    void deletar_deveChamarDeleteById() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        usuarioService.deletar(1L);

        verify(usuarioRepository).deleteById(1L);
    }
}
