package com.aura.aura_outfit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aura.aura_outfit.model.Carrinho;
import com.aura.aura_outfit.model.Produto;
import com.aura.aura_outfit.repository.CarrinhoRepository;
import com.aura.aura_outfit.repository.ProdutoRepository;

@Service
public class CarrinhoService {

    @Autowired
    private CarrinhoRepository carrinhoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    // Criar carrinho
    public Carrinho criarCarrinho(Carrinho carrinho) {
        return carrinhoRepository.save(carrinho);
    }

    // Buscar carrinho por ID
    public Carrinho buscarPorId(Long id) {
        return carrinhoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Carrinho não encontrado"));
    }

    // Adicionar produto ao carrinho
    public Carrinho adicionarProduto(Long carrinhoId, Long produtoId, int quantidade) {

        Carrinho carrinho = buscarPorId(carrinhoId);

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        carrinho.adicionarProduto(produto, quantidade);

        return carrinhoRepository.save(carrinho);
    }

    // Remover produto
    public Carrinho removerProduto(Long carrinhoId, Long produtoId) {

        Carrinho carrinho = buscarPorId(carrinhoId);

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        carrinho.removerProduto(produto);

        return carrinhoRepository.save(carrinho);
    }

    // Atualizar quantidade
    public Carrinho atualizarQuantidade(Long carrinhoId, Long produtoId, int quantidade) {

        Carrinho carrinho = buscarPorId(carrinhoId);

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        carrinho.atualizarQuantidade(produto, quantidade);

        return carrinhoRepository.save(carrinho);
    }

    // Limpar carrinho
    public Carrinho limparCarrinho(Long carrinhoId) {

        Carrinho carrinho = buscarPorId(carrinhoId);

        carrinho.getItens().clear();
        carrinho.calcularTotal();

        return carrinhoRepository.save(carrinho);
    }
}