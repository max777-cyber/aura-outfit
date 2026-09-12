package com.aura.aura_outfit.dto;

import com.aura.aura_outfit.model.Usuario;

public class UsuarioResponseDTO {

    private Long id;
    private String nome;
    private String email;
    private String role;
    private String fotoPerfil;
    private boolean emailConfirmado;

    public UsuarioResponseDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.role = usuario.getRole();
        this.fotoPerfil = usuario.getFotoPerfil();
        this.emailConfirmado = usuario.isEmailConfirmado();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getFotoPerfil() { return fotoPerfil; }
    public boolean isEmailConfirmado() { return emailConfirmado; }
}
