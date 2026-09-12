package com.aura.aura_outfit.dto;

import com.aura.aura_outfit.model.Usuario;

public class PerfilDTO {

    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String documento;
    private String endereco;
    private String fotoPerfil;

    public PerfilDTO() {}

    public PerfilDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.telefone = usuario.getTelefone();
        this.documento = usuario.getDocumento();
        this.endereco = usuario.getEndereco();
        this.fotoPerfil = usuario.getFotoPerfil();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }
}