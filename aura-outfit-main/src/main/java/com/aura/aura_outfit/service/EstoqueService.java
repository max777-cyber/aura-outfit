package com.aura.aura_outfit.service;

import com.aura.aura_outfit.model.EstoqueProduto;
import com.aura.aura_outfit.model.Produto;
import com.aura.aura_outfit.repository.EstoqueRepository;
import com.aura.aura_outfit.repository.ProdutoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EstoqueService {

    private final EstoqueRepository estoqueRepository;
    private final ProdutoRepository produtoRepository;

    public EstoqueService(EstoqueRepository estoqueRepository, ProdutoRepository produtoRepository) {
        this.estoqueRepository = estoqueRepository;
        this.produtoRepository = produtoRepository;
    }

    // Lista todos os tamanhos de um produto
    public List<EstoqueProduto> listarPorProduto(Long produtoId) {
        return estoqueRepository.findByProdutoId(produtoId);
    }

    // Adiciona ou atualiza um tamanho
    public EstoqueProduto salvar(Long produtoId, String tamanho, Integer quantidade) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        // Se já existe esse tamanho, atualiza a quantidade
        EstoqueProduto estoque = estoqueRepository
                .findByProdutoIdAndTamanho(produtoId, tamanho)
                .orElse(new EstoqueProduto(produto, tamanho, 0));

        estoque.setQuantidade(quantidade);
        return estoqueRepository.save(estoque);
    }

    // Verifica se tem estoque disponível para um tamanho
    public boolean temEstoque(Long produtoId, String tamanho, Integer quantidade) {
        return estoqueRepository
                .findByProdutoIdAndTamanho(produtoId, tamanho)
                .map(e -> e.getQuantidade() >= quantidade)
                .orElse(false);
    }

    // Diminui o estoque ao adicionar ao carrinho
    public void diminuirEstoque(Long produtoId, String tamanho, Integer quantidade) {
        EstoqueProduto estoque = estoqueRepository
                .findByProdutoIdAndTamanho(produtoId, tamanho)
                .orElseThrow(() -> new RuntimeException("Tamanho não encontrado"));

        if (estoque.getQuantidade() < quantidade) {
            throw new RuntimeException("Estoque insuficiente para o tamanho " + tamanho);
        }

        estoque.setQuantidade(estoque.getQuantidade() - quantidade);
        estoqueRepository.save(estoque);
    }

    // Deleta um tamanho
    public void deletar(Long id) {
        estoqueRepository.deleteById(id);
    }
}