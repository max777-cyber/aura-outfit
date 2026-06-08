package com.aura.aura_outfit.service;

import com.aura.aura_outfit.model.Carrinho;
import com.aura.aura_outfit.model.ItemCarrinho;
import com.aura.aura_outfit.model.Produto;
import com.aura.aura_outfit.model.Usuario;
import com.aura.aura_outfit.repository.CarrinhoRepository;
import com.aura.aura_outfit.repository.ProdutoRepository;
import com.aura.aura_outfit.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CarrinhoService {

    private final CarrinhoRepository carrinhoRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstoqueService estoqueService;

    public CarrinhoService(CarrinhoRepository carrinhoRepository,
                           ProdutoRepository produtoRepository,
                           UsuarioRepository usuarioRepository,
                           EstoqueService estoqueService) {
        this.carrinhoRepository = carrinhoRepository;
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
        this.estoqueService = estoqueService;
    }

    public Carrinho obterOuCriarCarrinho(Long usuarioId) {
        return carrinhoRepository.findByUsuarioIdComItens(usuarioId)
                .orElseGet(() -> {
                    Usuario usuario = usuarioRepository.findById(usuarioId)
                            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
                    Carrinho novo = new Carrinho(usuario);
                    return carrinhoRepository.save(novo);
                });
    }

    // ─── Helper: compara tamanhos considerando null ─────────────────────────
    private boolean mesmoTamanho(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    public Carrinho adicionarProduto(Long usuarioId, Long produtoId, Integer quantidade, String tamanho) {
        Carrinho carrinho = obterOuCriarCarrinho(usuarioId);

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        // ✅ Verifica estoque apenas se houver cadastro para esse produto.
        // Se o produto não tem estoque cadastrado, permite adicionar (retrocompatível).
        if (tamanho != null) {
            int qtdAtualNoCarrinho = carrinho.getItens().stream()
                    .filter(i -> i.getProduto().getId().equals(produtoId)
                            && mesmoTamanho(tamanho, i.getTamanho()))
                    .mapToInt(ItemCarrinho::getQuantidade)
                    .sum();

            int qtdTotal = qtdAtualNoCarrinho + quantidade;

            boolean temEstoqueCadastrado = !estoqueService.listarPorProduto(produtoId).isEmpty();
            if (temEstoqueCadastrado
                    && !estoqueService.temEstoque(produtoId, tamanho, qtdTotal)) {
                throw new RuntimeException(
                        "Estoque insuficiente para o tamanho " + tamanho
                );
            }
        }

        // Verifica se já existe item com mesmo produto E tamanho
        ItemCarrinho itemExistente = carrinho.getItens().stream()
                .filter(i -> i.getProduto().getId().equals(produtoId)
                        && mesmoTamanho(tamanho, i.getTamanho()))
                .findFirst()
                .orElse(null);

        if (itemExistente != null) {
            itemExistente.setQuantidade(itemExistente.getQuantidade() + quantidade);
        } else {
            ItemCarrinho novoItem = new ItemCarrinho();
            novoItem.setCarrinho(carrinho);
            novoItem.setProduto(produto);
            novoItem.setQuantidade(quantidade);
            novoItem.setTamanho(tamanho);
            carrinho.getItens().add(novoItem);
        }

        carrinho.calcularTotal();
        return carrinhoRepository.save(carrinho);
    }

    // ✅ Remove por produtoId + tamanho (não apaga outros tamanhos do mesmo produto)
    public Carrinho removerProduto(Long usuarioId, Long produtoId, String tamanho) {
        Carrinho carrinho = obterOuCriarCarrinho(usuarioId);
        carrinho.getItens().removeIf(i ->
                i.getProduto().getId().equals(produtoId)
                && mesmoTamanho(tamanho, i.getTamanho())
        );
        carrinho.calcularTotal();
        return carrinhoRepository.save(carrinho);
    }

    // Overload: remove todos os tamanhos de um produto
    public Carrinho removerProduto(Long usuarioId, Long produtoId) {
        Carrinho carrinho = obterOuCriarCarrinho(usuarioId);
        carrinho.getItens().removeIf(i -> i.getProduto().getId().equals(produtoId));
        carrinho.calcularTotal();
        return carrinhoRepository.save(carrinho);
    }

    // ✅ Atualiza por produtoId + tamanho
    public Carrinho atualizarQuantidade(Long usuarioId, Long produtoId, Integer quantidade, String tamanho) {
        Carrinho carrinho = obterOuCriarCarrinho(usuarioId);

        if (quantidade <= 0) {
            return removerProduto(usuarioId, produtoId, tamanho);
        }

        if (tamanho != null) {
            boolean temEstoqueCadastrado = !estoqueService.listarPorProduto(produtoId).isEmpty();
            if (temEstoqueCadastrado
                    && !estoqueService.temEstoque(produtoId, tamanho, quantidade)) {
                throw new RuntimeException(
                        "Estoque insuficiente para o tamanho " + tamanho
                );
            }
        }

        carrinho.getItens().stream()
                .filter(i -> i.getProduto().getId().equals(produtoId)
                        && mesmoTamanho(tamanho, i.getTamanho()))
                .findFirst()
                .ifPresent(i -> i.setQuantidade(quantidade));

        carrinho.calcularTotal();
        return carrinhoRepository.save(carrinho);
    }

    public Carrinho limparCarrinho(Long usuarioId) {
        Carrinho carrinho = obterOuCriarCarrinho(usuarioId);
        carrinho.getItens().clear();
        carrinho.calcularTotal();
        return carrinhoRepository.save(carrinho);
    }

    @Transactional(readOnly = true)
    public Carrinho buscarCarrinho(Long usuarioId) {
        return carrinhoRepository.findByUsuarioIdComItens(usuarioId)
                .orElseGet(() -> obterOuCriarCarrinho(usuarioId));
    }
}