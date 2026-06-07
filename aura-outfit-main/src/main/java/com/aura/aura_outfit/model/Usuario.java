package com.aura.aura_outfit.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    // ✅ @JsonIgnore garante que a senha NUNCA vaza em resposta JSON,
    //    mesmo que alguém esqueça de usar DTO.
    @JsonIgnore
    @Column(nullable = false)
    private String senha;

    private String telefone;
    private String endereco;
    private String documento;

    @Lob
    @Column(name = "foto_perfil")
    private String fotoPerfil;

    @Column(name = "email_confirmado", nullable = false)
    private boolean emailConfirmado = false;

    @JsonIgnore
    @Column(name = "token_confirmacao_email", length = 120)
    private String tokenConfirmacaoEmail;

    @JsonIgnore
    @Column(name = "token_recuperacao_senha", length = 120)
    private String tokenRecuperacaoSenha;

    @JsonIgnore
    @Column(name = "token_recuperacao_expira")
    private LocalDateTime tokenRecuperacaoExpira;

    // ✅ NOVO: papel do usuário. "USER" (padrão) ou "ADMIN".
    @Column(nullable = false)
    private String role = "USER";

    public Usuario() {}

    public Usuario(Long id, String nome, String email, String senha, String telefone, String endereco, String documento) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
        this.endereco = endereco;
        this.documento = documento;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    public boolean isEmailConfirmado() { return emailConfirmado; }
    public void setEmailConfirmado(boolean emailConfirmado) { this.emailConfirmado = emailConfirmado; }

    public String getTokenConfirmacaoEmail() { return tokenConfirmacaoEmail; }
    public void setTokenConfirmacaoEmail(String tokenConfirmacaoEmail) { this.tokenConfirmacaoEmail = tokenConfirmacaoEmail; }

    public String getTokenRecuperacaoSenha() { return tokenRecuperacaoSenha; }
    public void setTokenRecuperacaoSenha(String tokenRecuperacaoSenha) { this.tokenRecuperacaoSenha = tokenRecuperacaoSenha; }

    public LocalDateTime getTokenRecuperacaoExpira() { return tokenRecuperacaoExpira; }
    public void setTokenRecuperacaoExpira(LocalDateTime tokenRecuperacaoExpira) { this.tokenRecuperacaoExpira = tokenRecuperacaoExpira; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }

    @Override
    public String toString() {
        return "Usuario{id=" + id + ", nome='" + nome + "', email='" + email + "', role='" + role + "'}";
    }
}
