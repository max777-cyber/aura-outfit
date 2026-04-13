package com.aura.aura_outfit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aura.aura_outfit.model.Usuario;
import com.aura.aura_outfit.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Cadastrar usuário
    public Usuario cadastrar(Usuario usuario) {

        // Verifica se já existe email
        Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(usuario.getEmail());

        if (usuarioExistente.isPresent()) {
            throw new RuntimeException("Email já cadastrado!");
        }

        return usuarioRepository.save(usuario);
    }

    // Login
    public Usuario login(String email, String senha) {

        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);

        if (usuario.isPresent() && usuario.get().getSenha().equals(senha)) {
            return usuario.get();
        }

        throw new RuntimeException("Email ou senha inválidos!");
    }

    // Listar usuários
    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    // Buscar por ID
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    // Deletar
    public void deletar(Long id) {
        usuarioRepository.deleteById(id);
    }
}