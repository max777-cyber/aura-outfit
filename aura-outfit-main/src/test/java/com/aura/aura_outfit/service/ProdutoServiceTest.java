package com.aura.aura_outfit.service;

import com.aura.aura_outfit.model.Produto;
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
  class ProdutoServiceTest {

    @Mock
        private ProdutoRepository produtoRepository;

    @InjectMocks
        private ProdutoService produtoService;

    private Produto criarProduto(Long id, String nome, Double preco) {
              Produto p = new Produto();
              p.setId(id);
              p.setNome(nome);
              p.setMarca("Nike");
              p.setPreco(preco);
              p.setQuantidade(10);
              return p;
    }

    @Test
        @DisplayName("listar: deve retornar todos os produtos do repositorio")
        void listar_deveRetornarTodosProdutos() {
                  List<Produto> esperado = List.of(
                                    criarProduto(1L, "Camiseta", 59.90),
                                    criarProduto(2L, "Calca", 129.90)
                            );
                  when(produtoRepository.findAll()).thenReturn(esperado);

            List<Produto> resultado = produtoService.listar();

            assertEquals(2, resultado.size());
                  verify(produtoRepository, times(1)).findAll();
        }

    @Test
        @DisplayName("buscarPorId: deve retornar produto quando ele existe")
        void buscarPorId_deveRetornarProdutoExistente() {
                  Produto produto = criarProduto(1L, "Camiseta", 59.90);
                  when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

            Produto resultado = produtoService.buscarPorId(1L);

            assertNotNull(resultado);
                  assertEquals("Camiseta", resultado.getNome());
                  assertEquals(59.90, resultado.getPreco());
        }

    @Test
        @DisplayName("buscarPorId: deve lancar RuntimeException quando produto nao existe")
        void buscarPorId_deveLancarExcecaoQuandoNaoEncontrado() {
                  when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                                                               () -> produtoService.buscarPorId(99L));

            assertEquals("Produto nao encontrado", ex.getMessage());
        }

    @Test
        @DisplayName("salvar: deve chamar save e retornar o produto salvo")
        void salvar_devePersistirProduto() {
                  Produto produto = criarProduto(null, "Bermuda", 89.90);
                  Produto salvo   = criarProduto(3L,   "Bermuda", 89.90);
                  when(produtoRepository.save(produto)).thenReturn(salvo);

            Produto resultado = produtoService.salvar(produto);

            assertNotNull(resultado.getId());
                  assertEquals(3L, resultado.getId());
                  verify(produtoRepository).save(produto);
        }

    @Test
        @DisplayName("atualizar: deve atualizar os campos e salvar")
        void atualizar_deveAtualizarCamposEPersistir() {
                  Produto existente  = criarProduto(1L, "Camiseta Velha", 49.90);
                  Produto novosDados = criarProduto(null, "Camiseta Nova", 69.90);
                  novosDados.setCor("Azul");

            when(produtoRepository.findById(1L)).thenReturn(Optional.of(existente));
                  when(produtoRepository.save(any(Produto.class))).thenAnswer(inv -> inv.getArgument(0));

            Produto resultado = produtoService.atualizar(1L, novosDados);

            assertEquals("Camiseta Nova", resultado.getNome());
                  assertEquals(69.90, resultado.getPreco());
                  assertEquals("Azul", resultado.getCor());
                  verify(produtoRepository).save(existente);
        }

    @Test
        @DisplayName("deletar: deve chamar deleteById quando produto existe")
        void deletar_deveChamarDeleteById() {
              when(produtoRepository.existsById(1L)).thenReturn(true);

            produtoService.deletar(1L);

            verify(produtoRepository).deleteById(1L);
    }

    @Test
        @DisplayName("deletar: deve lancar RuntimeException quando produto nao existe")
        void deletar_deveLancarExcecaoQuandoNaoExiste() {
              when(produtoRepository.existsById(99L)).thenReturn(false);

            RuntimeException ex = assertThrows(RuntimeException.class,
                                                               () -> produtoService.deletar(99L));

            assertEquals("Produto nao encontrado", ex.getMessage());
              verify(produtoRepository, never()).deleteById(any());
    }
  }
