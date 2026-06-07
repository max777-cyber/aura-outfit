package com.aura.aura_outfit.controller;

import com.aura.aura_outfit.model.Pedido;
import com.aura.aura_outfit.security.SessionUtil;
import com.aura.aura_outfit.service.PedidoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final SessionUtil sessionUtil;

    public PedidoController(PedidoService pedidoService, SessionUtil sessionUtil) {
        this.pedidoService = pedidoService;
        this.sessionUtil = sessionUtil;
    }

    /** Listar todos — só admin */
    @GetMapping
    public ResponseEntity<List<Pedido>> listar(HttpSession session) {
        sessionUtil.exigirAdmin(session);
        return ResponseEntity.ok(pedidoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> buscarPorId(@PathVariable Long id, HttpSession session) {
        Pedido p = pedidoService.buscarPorId(id);
        // Só o dono do pedido ou um admin pode ver
        sessionUtil.exigirDonoOuAdmin(session, p.getUsuario().getId());
        return ResponseEntity.ok(p);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Pedido>> buscarPorUsuario(@PathVariable Long usuarioId, HttpSession session) {
        sessionUtil.exigirDonoOuAdmin(session, usuarioId);
        return ResponseEntity.ok(pedidoService.buscarPorUsuario(usuarioId));
    }

    @PostMapping("/finalizar/{usuarioId}")
    public ResponseEntity<Pedido> finalizar(@PathVariable Long usuarioId, HttpSession session) {
        sessionUtil.exigirDonoOuAdmin(session, usuarioId);
        return ResponseEntity.status(201).body(pedidoService.finalizarPedido(usuarioId));
    }

    /** Atualizar status — só admin (clientes não mudam status dos próprios pedidos) */
    @PutMapping("/{id}/status")
    public ResponseEntity<Pedido> atualizarStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpSession session) {

        sessionUtil.exigirAdmin(session);
        String status = body.get("status");
        // ✅ whitelist de status permitidos
        if (status == null || !List.of("Pendente", "Pago", "Enviado", "Entregue", "Cancelado").contains(status)) {
            throw new IllegalArgumentException("Status inválido");
        }
        return ResponseEntity.ok(pedidoService.atualizarStatus(id, status));
    }

    /** Deletar pedido — só admin */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id, HttpSession session) {
        sessionUtil.exigirAdmin(session);
        pedidoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}