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
public class Pedido {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)


    private long id;
    @ManyToOne
    private Usuario usuario;
    @ElementCollection
    private Map<Produto, Integer> itens;
    private double total;
    private String status;
    private String enderecoEntrega;

    public Pedido() {
        this.itens = new HashMap<>();
        this.total = 0.0;
    }

    public Pedido(long id, Usuario usuario, String enderecoEntrega) {
        this.id = id;
        this.usuario = usuario;
        this.enderecoEntrega = enderecoEntrega;
        this.itens = new HashMap<>();
        this.total = 0.0;
        this.status = "Aguardando pagamento";
    }

    // Adicionar produto ao pedido
    public void adicionarProduto(Produto produto, int quantidade) {
        if (itens.containsKey(produto)) {
            itens.put(produto, itens.get(produto) + quantidade);
        } else {
            itens.put(produto, quantidade);
        }

        calcularTotal();
        System.out.println("Produto adicionado ao pedido!");
    }

    // Remover produto do pedido
    public void removerProduto(Produto produto) {
        if (itens.containsKey(produto)) {
            itens.remove(produto);
            calcularTotal();
            System.out.println("Produto removido do pedido!");
        } else {
            System.out.println("Produto não encontrado no pedido.");
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
            System.out.println("Quantidade atualizada com sucesso!");
        } else {
            System.out.println("Produto não encontrado no pedido.");
        }
    }

    // Calcular total
    public void calcularTotal() {
        total = 0.0;

        for (Map.Entry<Produto, Integer> item : itens.entrySet()) {
            Produto produto = item.getKey();
            int quantidade = item.getValue();
            total += produto.getPreco() * quantidade;
        }
    }

    // Confirmar pedido
    public void confirmarPedido() {
        if (itens.isEmpty()) {
            System.out.println("Não é possível confirmar um pedido vazio.");
        } else {
            status = "Confirmado";
            System.out.println("Pedido confirmado com sucesso!");
        }
    }

    // Cancelar pedido
    public void cancelarPedido() {
        status = "Cancelado";
        System.out.println("Pedido cancelado.");
    }

    // Visualizar pedido
    public void visualizarPedido() {
        System.out.println("===== PEDIDO =====");
        System.out.println("Id do pedido: " + id);

        if (usuario != null) {
            System.out.println("Cliente: " + usuario.getNome());
        }

        System.out.println("Endereço de entrega: " + enderecoEntrega);
        System.out.println("Status: " + status);

        if (itens.isEmpty()) {
            System.out.println("Pedido vazio.");
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

        System.out.println("Total do pedido: " + total);
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

    public void setItens(Map<Produto, Integer> itens) {
        this.itens = itens;
        calcularTotal();
    }

    public double getTotal() {
        return total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEnderecoEntrega() {
        return enderecoEntrega;
    }

    public void setEnderecoEntrega(String enderecoEntrega) {
        this.enderecoEntrega = enderecoEntrega;
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + id +
                ", usuario=" + (usuario != null ? usuario.getNome() : "sem usuário") +
                ", total=" + total +
                ", status='" + status + '\'' +
                ", enderecoEntrega='" + enderecoEntrega + '\'' +
                '}';
    }
}