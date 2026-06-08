package com.aura.aura_outfit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.aura.aura_outfit.model.Pedido;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuarioId(Long usuarioId);

    @Query("SELECT p FROM Pedido p LEFT JOIN FETCH p.usuario LEFT JOIN FETCH p.itens i LEFT JOIN FETCH i.produto WHERE p.id = :id")
    Optional<Pedido> findByIdComItens(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.usuario LEFT JOIN FETCH p.itens i LEFT JOIN FETCH i.produto WHERE p.usuario.id = :usuarioId")
    List<Pedido> findByUsuarioIdComItens(@Param("usuarioId") Long usuarioId);

}