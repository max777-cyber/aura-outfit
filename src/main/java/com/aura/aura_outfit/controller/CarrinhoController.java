package com.aura.aura_outfit.controller;

import com.aura.aura_outfit.model.Carrinho;
import com.aura.aura_outfit.service.CarrinhoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carrinhos")
@CrossOrigin("*")
public class CarrinhoController {

    @Autowired
    private CarrinhoService carrinhoService;

    @PostMapping
    public Carrinho criarCarrinho(@RequestBody Carrinho carrinho) {
        return carrinhoService.criarCarrinho(carrinho);
    }

    @GetMapping("/{id}")
    public Carrinho buscarPorId(@PathVariable Long id) {
        return carrinhoService.buscarPorId(id);
    }

    @PostMapping("/{carrinhoId}/produtos/{produtoId}")
    public Carrinho adicionarProduto(
            @PathVariable Long carrinhoId,
            @PathVariable Long produtoId,
            @RequestParam int quantidade) {
        return carrinhoService.adicionarProduto(carrinhoId, produtoId, quantidade);
    }

    @PutMapping("/{carrinhoId}/produtos/{produtoId}")
    public Carrinho atualizarQuantidade(
            @PathVariable Long carrinhoId,
            @PathVariable Long produtoId,
            @RequestParam int quantidade) {
        return carrinhoService.atualizarQuantidade(carrinhoId, produtoId, quantidade);
    }

    @DeleteMapping("/{carrinhoId}/produtos/{produtoId}")
    public Carrinho removerProduto(
            @PathVariable Long carrinhoId,
            @PathVariable Long produtoId) {
        return carrinhoService.removerProduto(carrinhoId, produtoId);
    }

    @DeleteMapping("/{carrinhoId}/limpar")
    public Carrinho limparCarrinho(@PathVariable Long carrinhoId) {
        return carrinhoService.limparCarrinho(carrinhoId);
    }
}