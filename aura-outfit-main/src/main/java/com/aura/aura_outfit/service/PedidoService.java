package com.aura.aura_outfit.service;

import com.aura.aura_outfit.model.Carrinho;
import com.aura.aura_outfit.model.ItemCarrinho;
import com.aura.aura_outfit.model.ItemPedido;
import com.aura.aura_outfit.model.Pedido;
import com.aura.aura_outfit.model.Usuario;
import com.aura.aura_outfit.repository.CarrinhoRepository;
import com.aura.aura_outfit.repository.PedidoRepository;
import com.aura.aura_outfit.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CarrinhoRepository carrinhoRepository;
    private final CarrinhoService carrinhoService;
    private final EstoqueService estoqueService;

    public PedidoService(PedidoRepository pedidoRepository,
                         UsuarioRepository usuarioRepository,
                         CarrinhoRepository carrinhoRepository,
                         CarrinhoService carrinhoService,
                         EstoqueService estoqueService) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.carrinhoRepository = carrinhoRepository;
        this.carrinhoService = carrinhoService;
        this.estoqueService = estoqueService;
    }

    public List<Pedido> listar() {
        return pedidoRepository.findAll();
    }

    public List<Pedido> buscarPorUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId);
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
    }

    @Transactional
    public Pedido finalizarPedido(Long usuarioId) {
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("🛒 FINALIZAR PEDIDO — usuarioId recebido: " + usuarioId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado (id=" + usuarioId + ")"));
        System.out.println("👤 Usuário: " + usuario.getEmail());

        Carrinho carrinho = carrinhoRepository.findByUsuarioIdComItens(usuarioId)
                .orElseGet(() -> carrinhoService.obterOuCriarCarrinho(usuarioId));

        long totalCarrinhosNoBanco = carrinhoRepository.findAll().stream()
                .filter(c -> c.getUsuario() != null && c.getUsuario().getId().equals(usuarioId))
                .count();
        System.out.println("🗄️  Carrinhos desse usuário no banco: " + totalCarrinhosNoBanco);

        List<ItemCarrinho> itens = new ArrayList<>(carrinho.getItens());
        System.out.println("📦 Carrinho id=" + carrinho.getId() + " tem " + itens.size() + " item(ns)");
        for (ItemCarrinho it : itens) {
            System.out.println("   → " + it.getProduto().getNome()
                    + " | tamanho=" + it.getTamanho()
                    + " | qtd=" + it.getQuantidade());
        }
        System.out.println("═══════════════════════════════════════════════");

        if (itens.isEmpty()) {
            throw new RuntimeException(
                "Carrinho está vazio! (carrinhoId=" + carrinho.getId()
                + ", carrinhosDoUsuarioNoBanco=" + totalCarrinhosNoBanco + ")"
            );
        }

        // ✅ Valida estoque (só verifica, NÃO diminui)
        for (ItemCarrinho item : itens) {
            if (item.getTamanho() != null) {
                boolean temEstoqueCadastrado = !estoqueService
                        .listarPorProduto(item.getProduto().getId()).isEmpty();
                if (temEstoqueCadastrado
                        && !estoqueService.temEstoque(
                                item.getProduto().getId(),
                                item.getTamanho(),
                                item.getQuantidade())) {
                    throw new RuntimeException(
                            "Estoque insuficiente para " + item.getProduto().getNome()
                                    + " (tamanho " + item.getTamanho() + ")"
                    );
                }
            }
        }

        // Cria o pedido com status "Pendente" (estoque NÃO é baixado aqui)
        Pedido pedido = new Pedido(usuario);

        for (ItemCarrinho itemCarrinho : itens) {
            // ✅ Passa o tamanho pro ItemPedido — necessário pra baixar estoque depois
            ItemPedido itemPedido = new ItemPedido(
                    pedido,
                    itemCarrinho.getProduto(),
                    itemCarrinho.getQuantidade(),
                    itemCarrinho.getTamanho()
            );
            pedido.getItens().add(itemPedido);

            // ⚠️ NÃO diminui estoque aqui!
            //    Estoque só é baixado em confirmarPagamento(), quando o pagamento for aprovado.
        }

        pedido.calcularTotal();
        Pedido salvo = pedidoRepository.save(pedido);

        // Limpa o carrinho após criar o pedido
        carrinho.getItens().clear();
        carrinho.setTotal(0.0);
        carrinhoRepository.save(carrinho);

        System.out.println("✅ Pedido #" + salvo.getId()
                + " criado (status=Pendente)! Total: R$ " + salvo.getValorTotal());
        System.out.println("   ⏳ Aguardando pagamento — estoque NÃO foi baixado ainda.");

        return salvo;
    }

    /**
     * ✅ Confirma pagamento de um pedido.
     *
     * Chamado pelo webhook do Mercado Pago quando o pagamento é aprovado.
     * SÓ AQUI o estoque é baixado de verdade.
     *
     * Idempotente: se o pedido já está Pago, não faz nada (o MP pode chamar o
     * webhook várias vezes pro mesmo pagamento).
     */
    @Transactional
    public Pedido confirmarPagamento(Long pedidoId) {
        Pedido pedido = buscarPorId(pedidoId);

        if ("Pago".equalsIgnoreCase(pedido.getStatus())) {
            System.out.println("ℹ️  Pedido #" + pedidoId + " já estava Pago — ignorando.");
            return pedido;
        }

        System.out.println("💰 Confirmando pagamento do pedido #" + pedidoId);

        // Baixa o estoque agora (só agora, pagamento confirmado!)
        for (ItemPedido item : pedido.getItens()) {
            if (item.getTamanho() != null) {
                boolean temEstoqueCadastrado = !estoqueService
                        .listarPorProduto(item.getProduto().getId()).isEmpty();
                if (temEstoqueCadastrado) {
                    estoqueService.diminuirEstoque(
                            item.getProduto().getId(),
                            item.getTamanho(),
                            item.getQuantidade()
                    );
                    System.out.println("   ↓ Estoque baixado: "
                            + item.getProduto().getNome()
                            + " | tam=" + item.getTamanho()
                            + " | qtd=" + item.getQuantidade());
                }
            }
        }

        pedido.setStatus("Pago");
        Pedido salvo = pedidoRepository.save(pedido);

        System.out.println("✅ Pedido #" + pedidoId + " marcado como Pago e estoque atualizado!");
        return salvo;
    }

    /**
     * ✅ Cancela um pedido.
     * Se já estava pago, devolve o estoque. Se não, só muda o status.
     */
    @Transactional
    public Pedido cancelarPedido(Long pedidoId) {
        Pedido pedido = buscarPorId(pedidoId);
        boolean estavaPago = "Pago".equalsIgnoreCase(pedido.getStatus());

        if (estavaPago) {
            // Devolve o estoque
            for (ItemPedido item : pedido.getItens()) {
                if (item.getTamanho() != null) {
                    var estoques = estoqueService.listarPorProduto(item.getProduto().getId());
                    var estoque = estoques.stream()
                            .filter(e -> item.getTamanho().equals(e.getTamanho()))
                            .findFirst()
                            .orElse(null);
                    if (estoque != null) {
                        estoqueService.salvar(
                                item.getProduto().getId(),
                                item.getTamanho(),
                                estoque.getQuantidade() + item.getQuantidade()
                        );
                        System.out.println("   ↑ Estoque devolvido: "
                                + item.getProduto().getNome()
                                + " | tam=" + item.getTamanho()
                                + " | qtd=" + item.getQuantidade());
                    }
                }
            }
        }

        pedido.setStatus("Cancelado");
        return pedidoRepository.save(pedido);
    }

    public Pedido atualizarStatus(Long id, String status) {
        Pedido pedido = buscarPorId(id);
        pedido.setStatus(status);
        return pedidoRepository.save(pedido);
    }

    public void deletar(Long id) {
        if (!pedidoRepository.existsById(id)) {
            throw new RuntimeException("Pedido não encontrado");
        }
        pedidoRepository.deleteById(id);
    }
}