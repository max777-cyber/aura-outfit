package com.aura.aura_outfit.controller;

import com.aura.aura_outfit.model.Carrinho;
import com.aura.aura_outfit.security.SessionUtil;
import com.aura.aura_outfit.service.CarrinhoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carrinho")
public class CarrinhoController {

    private final CarrinhoService carrinhoService;
    private final SessionUtil sessionUtil;

    public CarrinhoController(CarrinhoService carrinhoService, SessionUtil sessionUtil) {
        this.carrinhoService = carrinhoService;
        this.sessionUtil = sessionUtil;
    }

    @GetMapping("/{usuarioId}")
    public ResponseEntity<Carrinho> buscarCarrinho(@PathVariable Long usuarioId, HttpSession session) {
        sessionUtil.exigirDonoOuAdmin(session, usuarioId);
        return ResponseEntity.ok(carrinhoService.buscarCarrinho(usuarioId));
    }

    @PostMapping("/{usuarioId}/produtos/{produtoId}")
    public ResponseEntity<Carrinho> adicionarProduto(
            @PathVariable Long usuarioId,
            @PathVariable Long produtoId,
            @RequestParam(defaultValue = "1") Integer quantidade,
            @RequestParam(required = false) String tamanho,
            HttpSession session) {

        sessionUtil.exigirDonoOuAdmin(session, usuarioId);
        if (quantidade <= 0 || quantidade > 99) {
            throw new IllegalArgumentException("Quantidade inválida");
        }
        return ResponseEntity.ok(carrinhoService.adicionarProduto(usuarioId, produtoId, quantidade, tamanho));
    }

    @PutMapping("/{usuarioId}/produtos/{produtoId}")
    public ResponseEntity<Carrinho> atualizarQuantidade(
            @PathVariable Long usuarioId,
            @PathVariable Long produtoId,
            @RequestParam Integer quantidade,
            @RequestParam(required = false) String tamanho,
            HttpSession session) {

        sessionUtil.exigirDonoOuAdmin(session, usuarioId);
        if (quantidade < 0 || quantidade > 99) {
            throw new IllegalArgumentException("Quantidade inválida");
        }
        return ResponseEntity.ok(
                carrinhoService.atualizarQuantidade(usuarioId, produtoId, quantidade, tamanho)
        );
    }

    @DeleteMapping("/{usuarioId}/produtos/{produtoId}")
    public ResponseEntity<Carrinho> removerProduto(
            @PathVariable Long usuarioId,
            @PathVariable Long produtoId,
            @RequestParam(required = false) String tamanho,
            HttpSession session) {

        sessionUtil.exigirDonoOuAdmin(session, usuarioId);
        if (tamanho != null) {
            return ResponseEntity.ok(carrinhoService.removerProduto(usuarioId, produtoId, tamanho));
        }
        return ResponseEntity.ok(carrinhoService.removerProduto(usuarioId, produtoId));
    }

    @DeleteMapping("/{usuarioId}/limpar")
    public ResponseEntity<Carrinho> limparCarrinho(@PathVariable Long usuarioId, HttpSession session) {
        sessionUtil.exigirDonoOuAdmin(session, usuarioId);
        return ResponseEntity.ok(carrinhoService.limparCarrinho(usuarioId));
    }
}