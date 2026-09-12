package com.aura.aura_outfit.controller;

import com.aura.aura_outfit.model.ProdutoImagem;
import com.aura.aura_outfit.model.Produto;
import com.aura.aura_outfit.repository.ProdutoImagemRepository;
import com.aura.aura_outfit.security.SessionUtil;
import com.aura.aura_outfit.service.ProdutoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;
    private final ProdutoImagemRepository produtoImagemRepository;
    private final SessionUtil sessionUtil;

    public ProdutoController(ProdutoService produtoService, ProdutoImagemRepository produtoImagemRepository, SessionUtil sessionUtil) {
        this.produtoService = produtoService;
        this.produtoImagemRepository = produtoImagemRepository;
        this.sessionUtil = sessionUtil;
    }

    // ─── Leitura: pública (qualquer visitante pode ver o catálogo) ───

    @GetMapping
    public ResponseEntity<List<Produto>> listar() {
        return ResponseEntity.ok(produtoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Produto> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    @GetMapping("/{id}/imagens")
    public ResponseEntity<List<ProdutoImagem>> listarImagens(@PathVariable Long id) {
        produtoService.buscarPorId(id);
        return ResponseEntity.ok(produtoImagemRepository.findByProdutoIdOrderByOrdemAsc(id));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Produto>> buscarPorNome(@RequestParam String nome) {
        return ResponseEntity.ok(produtoService.buscarPorNome(nome));
    }

    @GetMapping("/marca/{marca}")
    public ResponseEntity<List<Produto>> buscarPorMarca(@PathVariable String marca) {
        return ResponseEntity.ok(produtoService.buscarPorMarca(marca));
    }

    @GetMapping("/genero/{genero}")
    public ResponseEntity<List<Produto>> buscarPorGenero(@PathVariable String genero) {
        return ResponseEntity.ok(produtoService.buscarPorGenero(genero));
    }

    // ─── Escrita: apenas admin ───

    @PostMapping
    public ResponseEntity<Produto> salvar(@RequestBody Produto produto, HttpSession session) {
        sessionUtil.exigirAdmin(session);
        produto.setId(null); // ✅ impede sobrescrita por ID no body
        return ResponseEntity.status(201).body(produtoService.salvar(produto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizar(@PathVariable Long id, @RequestBody Produto dadosNovos, HttpSession session) {
        sessionUtil.exigirAdmin(session);
        return ResponseEntity.ok(produtoService.atualizar(id, dadosNovos));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id, HttpSession session) {
        sessionUtil.exigirAdmin(session);
        produtoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
