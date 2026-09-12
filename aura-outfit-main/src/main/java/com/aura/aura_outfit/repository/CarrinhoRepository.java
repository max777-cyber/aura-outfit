package com.aura.aura_outfit.repository;

import com.aura.aura_outfit.model.Carrinho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CarrinhoRepository extends JpaRepository<Carrinho, Long> {

    Optional<Carrinho> findByUsuarioId(Long usuarioId);

    @Query("SELECT DISTINCT c FROM Carrinho c " +
           "LEFT JOIN FETCH c.itens i " +
           "LEFT JOIN FETCH i.produto " +
           "WHERE c.usuario.id = :usuarioId")
    Optional<Carrinho> findByUsuarioIdComItens(@Param("usuarioId") Long usuarioId);
}