package com.aura.aura_outfit.repository;

import com.aura.aura_outfit.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findByProdutoIdOrderByDataCriacaoDesc(Long produtoId);

    @Query("SELECT AVG(c.nota) FROM Comentario c WHERE c.produto.id = :produtoId")
    Double mediaNotasPorProduto(Long produtoId);

    boolean existsByProdutoIdAndUsuarioId(Long produtoId, Long usuarioId);
}