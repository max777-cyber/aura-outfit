package com.aura.aura_outfit.model;

import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;



@Entity
public class Carrinho {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
     @ManyToOne
    private Usuario usuario;
    @ElementCollection
    private Map<Produto, Integer> itens;
    private double total;

    public Carrinho() {
        this.itens = new HashMap<>();
        this.total = 0.0;
    }

    public Carrinho(long id, Usuario usuario) {
        this.id = id;
        this.usuario = usuario;
        this.itens = new HashMap<>();
        this.total = 0.0;
    }

    // Adicionar produto
    public void adicionarProduto(Produto produto, int quantidade) {

        if (itens.containsKey(produto)) {
            itens.put(produto, itens.get(produto) + quantidade);
        } else {
            itens.put(produto, quantidade);
        }

        calcularTotal();
        System.out.println("Produto adicionado ao carrinho!");
    }

    // Remover produto
    public void removerProduto(Produto produto) {

        if (itens.containsKey(produto)) {
            itens.remove(produto);
            calcularTotal();
            System.out.println("Produto removido!");
        } else {
            System.out.println("Produto não está no carrinho.");
        }
    }

    // Atualizar quantidade
    public void atualizarQuantidade(Produto produto, int quantidade) {

        if (itens.containsKey(produto)) {
            if (quantidade <= 0) {
                itens.remove(produto);
            } else {
                itens.put(produto, quantidade);
            }
            calcularTotal();
        } else {
            System.out.println("Produto não encontrado.");
        }
    }

    // Calcular total
    public void calcularTotal() {
        total = 0.0;

        for (Map.Entry<Produto, Integer> item : itens.entrySet()) {
            total += item.getKey().getPreco() * item.getValue();
        }
    }

    // Mostrar carrinho
    public void visualizarCarrinho() {
        System.out.println("===== CARRINHO =====");

        if (usuario != null) {
            System.out.println("Usuário: " + usuario.getNome());
        }

        if (itens.isEmpty()) {
            System.out.println("Carrinho vazio.");
        } else {
            for (Map.Entry<Produto, Integer> item : itens.entrySet()) {
                Produto produto = item.getKey();
                int quantidade = item.getValue();

                System.out.println("Produto: " + produto.getNome());
                System.out.println("Preço: " + produto.getPreco());
                System.out.println("Quantidade: " + quantidade);
                System.out.println("Subtotal: " + (produto.getPreco() * quantidade));
                System.out.println("----------------------");
            }
        }

        System.out.println("Total: " + total);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Map<Produto, Integer> getItens() {
        return itens;
    }

    public double getTotal() {
        return total;
    }

    @Override
    public String toString() {
        return "Carrinho{" +
                "id=" + id +
                ", usuario=" + (usuario != null ? usuario.getNome() : "sem usuário") +
                ", total=" + total +
                '}';
    }
}