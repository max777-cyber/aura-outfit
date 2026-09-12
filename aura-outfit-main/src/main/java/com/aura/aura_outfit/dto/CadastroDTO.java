package com.aura.aura_outfit.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO de cadastro de usuário.
 *
 * ✅ Boa prática: validar no DTO, não na entidade JPA.
 *    A entidade pode ser persistida com regras diferentes (ex: importação,
 *    seed do admin). O DTO é o contrato HTTP — aqui que valida input do cliente.
 */
public class CadastroDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Size(max = 150, message = "Email muito longo")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, max = 100, message = "Senha deve ter pelo menos 8 caracteres")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
        message = "Senha deve conter pelo menos uma letra e um número"
    )
    private String senha;

    // ─── Campos opcionais ─────────────────────────────────────────
    @Size(max = 20, message = "Telefone muito longo")
    @Pattern(
        regexp = "^[\\d\\s()+-]*$",
        message = "Telefone contém caracteres inválidos"
    )
    private String telefone;

    @Size(max = 20, message = "Documento muito longo")
    private String documento;

    @Size(max = 250, message = "Endereço muito longo")
    private String endereco;

    public CadastroDTO() {}

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
}