package com.aura.aura_outfit.controller;

import com.aura.aura_outfit.model.EstoqueProduto;
import com.aura.aura_outfit.security.SessionUtil;
import com.aura.aura_outfit.service.EstoqueService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/estoque")
public class EstoqueController {

    private final EstoqueService estoqueService;
    private final SessionUtil sessionUtil;

    public EstoqueController(EstoqueService estoqueService, SessionUtil sessionUtil) {
        this.estoqueService = estoqueService;
        this.sessionUtil = sessionUtil;
    }

    /**
     * Leitura pública: cliente precisa saber quais tamanhos têm estoque
     * pra não tentar comprar um tamanho esgotado.
     */
    @GetMapping("/{produtoId}")
    public ResponseEntity<List<EstoqueProduto>> listar(@PathVariable Long produtoId) {
        return ResponseEntity.ok(estoqueService.listarPorProduto(produtoId));
    }

    @PostMapping("/{produtoId}")
    public ResponseEntity<EstoqueProduto> salvar(
            @PathVariable Long produtoId,
            @RequestBody Map<String, Object> body,
            HttpSession session) {

        sessionUtil.exigirAdmin(session);
        String tamanho = (String) body.get("tamanho");
        Integer quantidade = (Integer) body.get("quantidade");
        if (tamanho == null || tamanho.isBlank() || quantidade == null || quantidade < 0) {
            throw new IllegalArgumentException("Tamanho e quantidade são obrigatórios e válidos");
        }
        return ResponseEntity.ok(estoqueService.salvar(produtoId, tamanho, quantidade));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id, HttpSession session) {
        sessionUtil.exigirAdmin(session);
        estoqueService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}