package com.aura.aura_outfit.controller;

import com.aura.aura_outfit.model.Comentario;
import com.aura.aura_outfit.security.SessionUtil;
import com.aura.aura_outfit.service.ComentarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comentarios")
public class ComentarioController {

    private final ComentarioService comentarioService;
    private final SessionUtil sessionUtil;

    public ComentarioController(ComentarioService comentarioService, SessionUtil sessionUtil) {
        this.comentarioService = comentarioService;
        this.sessionUtil = sessionUtil;
    }

    @GetMapping("/{produtoId}")
    public ResponseEntity<List<Comentario>> listar(@PathVariable Long produtoId) {
        return ResponseEntity.ok(comentarioService.listarPorProduto(produtoId));
    }

    @GetMapping("/{produtoId}/media")
    public ResponseEntity<Map<String, Double>> media(@PathVariable Long produtoId) {
        return ResponseEntity.ok(Map.of("media", comentarioService.mediaNota(produtoId)));
    }

    @PostMapping("/{produtoId}")
    public ResponseEntity<?> adicionar(
            @PathVariable Long produtoId,
            @RequestBody Map<String, Object> body,
            HttpSession session) {

        Long usuarioId = sessionUtil.exigirLogado(session);

        String texto = (String) body.get("texto");
        Integer nota = (Integer) body.get("nota");

        if (texto == null || texto.isBlank() || texto.length() > 500) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Texto inválido (1 a 500 caracteres)"));
        }
        if (nota == null || nota < 1 || nota > 5) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Nota deve ser entre 1 e 5"));
        }

        // ✅ Sanitização básica: remove tags HTML pra prevenir XSS armazenado.
        //    O frontend também DEVE escapar antes de renderizar.
        String textoLimpo = texto.replaceAll("<[^>]*>", "").trim();

        Comentario comentario = comentarioService.adicionar(produtoId, usuarioId, textoLimpo, nota);
        return ResponseEntity.status(201).body(comentario);
    }

    /**
     * Deletar: só o dono do comentário ou admin.
     * Antes qualquer pessoa deletava qualquer comentário (!).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id, HttpSession session) {
        Comentario c = comentarioService.buscarPorId(id);
        sessionUtil.exigirDonoOuAdmin(session, c.getUsuario().getId());
        comentarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}