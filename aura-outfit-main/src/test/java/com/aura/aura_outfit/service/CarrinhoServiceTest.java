package com.aura.aura_outfit.service;

import com.aura.aura_outfit.model.Carrinho;
import com.aura.aura_outfit.model.ItemCarrinho;
import com.aura.aura_outfit.model.Produto;
import com.aura.aura_outfit.model.Usuario;
import com.aura.aura_outfit.repository.CarrinhoRepository;
import com.aura.aura_outfit.repository.ProdutoRepository;
import com.aura.aura_outfit.repository.UsuarioRepository;
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
  class CarrinhoServiceTest {

    @Mock private CarrinhoRepository carrinhoRepository;
        @Mock private ProdutoRepository  produtoRepository;
        @Mock private UsuarioRepository  usuarioRepository;
        @Mock private EstoqueService     estoqueService;

    @InjectMocks
        private CarrinhoService carrinhoService;

    private Usuario criarUsuario(Long id) {
              Usuario u = new Usuario();
              u.setId(id);
              u.setNome("Max");
              u.setEmail("max@email.com");
              return u;
    }

    private Produto criarProduto(Long id, Double preco) {
              Produto p = new Produto();
              p.setId(id);
              p.setNome("Camiseta");
              p.setPreco(preco);
              p.setQuantidade(20);
              return p;
    }

    private Carrinho carrinhoVazio(Long usuarioId) {
              Usuario usuario = criarUsuario(usuarioId);
              Carrinho c = new Carrinho(usuario);
              c.setId(1L);
              return c;
    }

    @Test
        @DisplayName("obterOuCriarCarrinho: deve retornar o carrinho existente do banco")
        void obterOuCriarCarrinho_deveRetornarCarrinhoExistente() {
                  Carrinho existente = carrinhoVazio(10L);
                  when(carrinhoRepository.findByUsuarioIdComItens(10L))
                                    .thenReturn(Optional.of(existente));

            Carrinho resultado = carrinhoService.obterOuCriarCarrinho(10L);

            assertEquals(existente.getId(), resultado.getId());
                  verify(carrinhoRepository, never()).save(any());
        }

    @Test
        @DisplayName("obterOuCriarCarrinho: deve criar e salvar novo carrinho quando nao existe")
        void obterOuCriarCarrinho_deveCriarNovoCarrinho() {
                  Usuario usuario = criarUsuario(10L);
                  when(carrinhoRepository.findByUsuarioIdComItens(10L))
                                    .thenReturn(Optional.empty());
                  when(usuarioRepository.findById(10L))
                                    .thenReturn(Optional.of(usuario));
                  when(carrinhoRepository.save(any(Carrinho.class)))
                                    .thenAnswer(inv -> inv.getArgument(0));

            Carrinho resultado = carrinhoService.obterOuCriarCarrinho(10L);

            assertNotNull(resultado);
                  verify(carrinhoRepository).save(any(Carrinho.class));
        }

    @Test
        @DisplayName("adicionarProduto: deve adicionar novo item ao carrinho vazio")
        void adicionarProduto_deveAdicionarNovoItem() {
                  Carrinho carrinho = carrinhoVazio(10L);
                  Produto produto   = criarProduto(5L, 99.90);

            when(carrinhoRepository.findByUsuarioIdComItens(10L))
                              .thenReturn(Optional.of(carrinho));
                  when(produtoRepository.findById(5L))
                                    .thenReturn(Optional.of(produto));
                  when(estoqueService.listarPorProduto(5L)).thenReturn(List.of());
                  when(carrinhoRepository.save(any(Carrinho.class)))
                                    .thenAnswer(inv -> inv.getArgument(0));

            Carrinho resultado = carrinhoService.adicionarProduto(10L, 5L, 2, "M");

            assertEquals(1, resultado.getItens().size());
                  assertEquals(2, resultado.getItens().get(0).getQuantidade());
                  assertEquals("M", resultado.getItens().get(0).getTamanho());
        }

    @Test
        @DisplayName("adicionarProduto: deve somar quantidade quando item ja existe com mesmo tamanho")
        void adicionarProduto_deveSomarQuantidadeItemExistente() {
              Carrinho carrinho = carrinhoVazio(10L);
              Produto produto   = criarProduto(5L, 99.90);

            ItemCarrinho itemExistente = new ItemCarrinho();
              itemExistente.setCarrinho(carrinho);
              itemExistente.setProduto(produto);
              itemExistente.setQuantidade(1);
              itemExistente.setTamanho("M");
              carrinho.getItens().add(itemExistente);

            when(carrinhoRepository.findByUsuarioIdComItens(10L))
                              .thenReturn(Optional.of(carrinho));
              when(produtoRepository.findById(5L))
                                .thenReturn(Optional.of(produto));
              when(estoqueService.listarPorProduto(5L)).thenReturn(List.of());
              when(carrinhoRepository.save(any(Carrinho.class)))
                                .thenAnswer(inv -> inv.getArgument(0));

            carrinhoService.adicionarProduto(10L, 5L, 2, "M");

            assertEquals(1, carrinho.getItens().size());
              assertEquals(3, carrinho.getItens().get(0).getQuantidade());
    }

    @Test
        @DisplayName("limparCarrinho: deve remover todos os itens e zerar o total")
        void limparCarrinho_deveRemoverTodosOsItens() {
              Carrinho carrinho = carrinhoVazio(10L);
              Produto produto   = criarProduto(5L, 99.90);

            ItemCarrinho item = new ItemCarrinho();
              item.setCarrinho(carrinho);
              item.setProduto(produto);
              item.setQuantidade(3);
              carrinho.getItens().add(item);

            when(carrinhoRepository.findByUsuarioIdComItens(10L))
                              .thenReturn(Optional.of(carrinho));
              when(carrinhoRepository.save(any(Carrinho.class)))
                                .thenAnswer(inv -> inv.getArgument(0));

            Carrinho resultado = carrinhoService.limparCarrinho(10L);

            assertTrue(resultado.getItens().isEmpty());
              assertEquals(0.0, resultado.getTotal());
    }
  }
