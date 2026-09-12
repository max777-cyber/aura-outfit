package com.aura.aura_outfit.repository;

import com.aura.aura_outfit.model.EstoqueProduto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstoqueRepository extends JpaRepository<EstoqueProduto, Long> {

    List<EstoqueProduto> findByProdutoId(Long produtoId);

    Optional<EstoqueProduto> findByProdutoIdAndTamanho(Long produtoId, String tamanho);
}