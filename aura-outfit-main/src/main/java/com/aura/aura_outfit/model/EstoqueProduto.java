package com.aura.aura_outfit.model;

import jakarta.persistence.*;

@Entity
@Table(name = "estoque_produto")
public class EstoqueProduto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    private String tamanho;

    @Column(nullable = false)
    private Integer quantidade;

    public EstoqueProduto() {}

    public EstoqueProduto(Produto produto, String tamanho, Integer quantidade) {
        this.produto = produto;
        this.tamanho = tamanho;
        this.quantidade = quantidade;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }

    public String getTamanho() { return tamanho; }
    public void setTamanho(String tamanho) { this.tamanho = tamanho; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
}