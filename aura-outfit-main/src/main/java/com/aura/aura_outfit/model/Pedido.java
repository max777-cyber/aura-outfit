package com.aura.aura_outfit.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Ignora TUDO que pode causar ciclo: senha, carrinho, pedidos, e proxy do Hibernate
    @JsonIgnoreProperties({
        "senha", "telefone", "endereco", "documento",
        "carrinho", "pedidos",
        "hibernateLazyInitializer", "handler"
    })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    @Column(nullable = false)
    private Double valorTotal = 0.0;

    @Column(nullable = false)
    private String status = "Pendente";

    @Column(nullable = false)
    private LocalDateTime dataPedido = LocalDateTime.now();

    public Pedido() {}

    public Pedido(Usuario usuario) {
        this.usuario = usuario;
    }

    public void calcularTotal() {
        this.valorTotal = itens.stream()
                .mapToDouble(item -> item.getPrecoUnitario() * item.getQuantidade())
                .sum();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public List<ItemPedido> getItens() { return itens; }
    public void setItens(List<ItemPedido> itens) { this.itens = itens; }

    public Double getValorTotal() { return valorTotal; }
    public void setValorTotal(Double valorTotal) { this.valorTotal = valorTotal; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getDataPedido() { return dataPedido; }
    public void setDataPedido(LocalDateTime dataPedido) { this.dataPedido = dataPedido; }

    @Override
    public String toString() {
        return "Pedido{id=" + id + ", status='" + status + "', valorTotal=" + valorTotal + "}";
    }
}