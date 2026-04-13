package com.aura.aura_outfit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.aura.aura_outfit.model.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar por email (usado no login)
    Optional<Usuario> findByEmail(String email);

}