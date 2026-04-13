package com.aura.aura_outfit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.aura.aura_outfit.model.Produto;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // Buscar produtos pelo nome
    List<Produto> findByNomeContainingIgnoreCase(String nome);

}