package com.aura.aura_outfit.service;

import com.aura.aura_outfit.model.Produto;
import com.aura.aura_outfit.repository.ProdutoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public Produto salvar(Produto produto) {
        return produtoRepository.save(produto);
    }

    public List<Produto> listar() {
        return produtoRepository.findAll();
    }

    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    }

    public List<Produto> buscarPorNome(String nome) {
        return produtoRepository.findByNomeContainingIgnoreCase(nome);
    }

    public List<Produto> buscarPorMarca(String marca) {
        return produtoRepository.findByMarcaIgnoreCase(marca);
    }

    public List<Produto> buscarPorGenero(String genero) {
        return produtoRepository.findByGeneroIgnoreCase(genero);
    }

    public Produto atualizar(Long id, Produto dadosNovos) {
        Produto produto = buscarPorId(id);
        produto.setNome(dadosNovos.getNome());
        produto.setMarca(dadosNovos.getMarca());
        produto.setCor(dadosNovos.getCor());
        produto.setTamanho(dadosNovos.getTamanho());
        produto.setGenero(dadosNovos.getGenero());
        produto.setPreco(dadosNovos.getPreco());
        produto.setQuantidade(dadosNovos.getQuantidade());
        if (dadosNovos.getImagemUrl() != null) {
            produto.setImagemUrl(dadosNovos.getImagemUrl());
        }
        return produtoRepository.save(produto);
    }

    public void deletar(Long id) {
        if (!produtoRepository.existsById(id)) {
            throw new RuntimeException("Produto não encontrado");
        }
        produtoRepository.deleteById(id);
    }
}