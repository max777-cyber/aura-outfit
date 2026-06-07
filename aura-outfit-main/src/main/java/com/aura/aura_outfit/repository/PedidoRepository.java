package com.aura.aura_outfit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.aura.aura_outfit.model.Pedido;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuarioId(Long usuarioId);

}