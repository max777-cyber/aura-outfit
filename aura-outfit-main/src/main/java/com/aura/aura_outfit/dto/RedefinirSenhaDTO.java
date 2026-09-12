package com.aura.aura_outfit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RedefinirSenhaDTO {

    @NotBlank(message = "Token e obrigatorio")
    private String token;

    @NotBlank(message = "Senha e obrigatoria")
    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
    private String senha;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}
