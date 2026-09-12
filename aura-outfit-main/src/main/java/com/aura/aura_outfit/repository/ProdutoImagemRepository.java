package com.aura.aura_outfit.repository;

import com.aura.aura_outfit.model.ProdutoImagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoImagemRepository extends JpaRepository<ProdutoImagem, Long> {
    List<ProdutoImagem> findByProdutoIdOrderByOrdemAsc(Long produtoId);
}
