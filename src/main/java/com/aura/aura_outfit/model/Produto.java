
package com.aura.aura_outfit.model;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


@Entity
public class Produto {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)

    private long id;
    private String nome;
    private String cor;
    private String tamanho;
    private String descricao;
    private double preco;
    private int quantidade;
    private String categoria;

    // Construtor vazio
    public Produto() {
    }

    // Construtor com parâmetros
    public Produto(long id, String nome, String cor, String tamanho, String descricao, double preco, int quantidade, String categoria) {
        this.id = id;
        this.nome = nome;
        this.cor = cor;
        this.tamanho = tamanho;
        this.descricao = descricao;
        this.preco = preco;
        this.quantidade = quantidade;
        this.categoria = categoria;
    }

    // Método de cadastro
    public void cadastrar(String nome, String cor, String tamanho, String descricao, double preco, int quantidade, String categoria) {
        this.nome = nome;
        this.cor = cor;
        this.tamanho = tamanho;
        this.descricao = descricao;
        this.preco = preco;
        this.quantidade = quantidade;
        this.categoria = categoria;

        System.out.println("Produto cadastrado com sucesso!");
    }

    // Método de visualização
    public void visualizar() {
        System.out.println("Id: " + id);
        System.out.println("Nome: " + nome);
        System.out.println("Cor: " + cor);
        System.out.println("Tamanho: " + tamanho);
        System.out.println("Descrição: " + descricao);
        System.out.println("Preço: " + preco);
        System.out.println("Quantidade: " + quantidade);
        System.out.println("Categoria: " + categoria);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public String getTamanho() {
        return tamanho;
    }

    public void setTamanho(String tamanho) {
        this.tamanho = tamanho;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    @Override
    public String toString() {
        return "Produto{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", cor='" + cor + '\'' +
                ", tamanho='" + tamanho + '\'' +
                ", descricao='" + descricao + '\'' +
                ", preco=" + preco +
                ", quantidade=" + quantidade +
                ", categoria='" + categoria + '\'' +
                '}';
    }
}