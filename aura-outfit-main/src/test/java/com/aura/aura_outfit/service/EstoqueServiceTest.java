package com.aura.aura_outfit.service;

import com.aura.aura_outfit.model.EstoqueProduto;
import com.aura.aura_outfit.model.Produto;
import com.aura.aura_outfit.repository.EstoqueRepository;
import com.aura.aura_outfit.repository.ProdutoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstoqueServiceTest {

    @Mock private EstoqueRepository estoqueRepository;
    @Mock private ProdutoRepository produtoRepository;

    @InjectMocks
    private EstoqueService estoqueService;

    private Produto criarProduto(Long id) {
        Produto p = new Produto();
        p.setId(id);
        p.setNome("Camiseta");
        return p;
    }

    @Test
    @DisplayName("listarPorProduto: deve retornar lista do repositorio")
    void listarPorProduto_deveRetornarLista() {
        Produto produto = criarProduto(1L);
        EstoqueProduto ep = new EstoqueProduto(produto, "M", 10);
        when(estoqueRepository.findByProdutoId(1L)).thenReturn(List.of(ep));

        List<EstoqueProduto> resultado = estoqueService.listarPorProduto(1L);

        assertEquals(1, resultado.size());
        assertEquals("M", resultado.get(0).getTamanho());
        verify(estoqueRepository).findByProdutoId(1L);
    }

    @Test
    @DisplayName("salvar: deve criar novo estoque quando tamanho nao existe")
    void salvar_deveCriarNovoEstoque() {
        Produto produto = criarProduto(1L);
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(estoqueRepository.findByProdutoIdAndTamanho(1L, "G")).thenReturn(Optional.empty());
        when(estoqueRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EstoqueProduto resultado = estoqueService.salvar(1L, "G", 15);

        assertEquals(15, resultado.getQuantidade());
        assertEquals("G", resultado.getTamanho());
    }

    @Test
    @DisplayName("salvar: deve atualizar quantidade quando tamanho ja existe")
    void salvar_deveAtualizarQuantidadeExistente() {
        Produto produto = criarProduto(1L);
        EstoqueProduto existente = new EstoqueProduto(produto, "M", 5);
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(estoqueRepository.findByProdutoIdAndTamanho(1L, "M")).thenReturn(Optional.of(existente));
        when(estoqueRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EstoqueProduto resultado = estoqueService.salvar(1L, "M", 20);

        assertEquals(20, resultado.getQuantidade());
    }

    @Test
    @DisplayName("temEstoque: deve retornar true quando quantidade suficiente")
    void temEstoque_deveRetornarTrue() {
        Produto produto = criarProduto(1L);
        EstoqueProduto ep = new EstoqueProduto(produto, "P", 10);
        when(estoqueRepository.findByProdutoIdAndTamanho(1L, "P")).thenReturn(Optional.of(ep));

        assertTrue(estoqueService.temEstoque(1L, "P", 5));
        assertTrue(estoqueService.temEstoque(1L, "P", 10));
    }

    @Test
    @DisplayName("temEstoque: deve retornar false quando estoque insuficiente")
    void temEstoque_deveRetornarFalseInsuficiente() {
        Produto produto = criarProduto(1L);
        EstoqueProduto ep = new EstoqueProduto(produto, "P", 2);
        when(estoqueRepository.findByProdutoIdAndTamanho(1L, "P")).thenReturn(Optional.of(ep));

        assertFalse(estoqueService.temEstoque(1L, "P", 5));
    }

    @Test
    @DisplayName("temEstoque: deve retornar false quando tamanho nao existe")
    void temEstoque_deveRetornarFalseQuandoNaoExiste() {
        when(estoqueRepository.findByProdutoIdAndTamanho(1L, "GG")).thenReturn(Optional.empty());

        assertFalse(estoqueService.temEstoque(1L, "GG", 1));
    }

    @Test
    @DisplayName("diminuirEstoque: deve reduzir quantidade corretamente")
    void diminuirEstoque_deveReduzirQuantidade() {
        Produto produto = criarProduto(1L);
        EstoqueProduto ep = new EstoqueProduto(produto, "M", 10);
        when(estoqueRepository.findByProdutoIdAndTamanho(1L, "M")).thenReturn(Optional.of(ep));
        when(estoqueRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        estoqueService.diminuirEstoque(1L, "M", 3);

        assertEquals(7, ep.getQuantidade());
        verify(estoqueRepository).save(ep);
    }

    @Test
    @DisplayName("diminuirEstoque: deve lancar excecao quando estoque insuficiente")
    void diminuirEstoque_deveLancarExcecao() {
        Produto produto = criarProduto(1L);
        EstoqueProduto ep = new EstoqueProduto(produto, "M", 2);
        when(estoqueRepository.findByProdutoIdAndTamanho(1L, "M")).thenReturn(Optional.of(ep));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> estoqueService.diminuirEstoque(1L, "M", 5));

        assertTrue(ex.getMessage().contains("Estoque insuficiente"));
    }

    @Test
    @DisplayName("deletar: deve chamar deleteById")
    void deletar_deveChamarDeleteById() {
        estoqueService.deletar(1L);
        verify(estoqueRepository).deleteById(1L);
    }
}
