package com.aura.aura_outfit.repository;

import com.aura.aura_outfit.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByNomeContainingIgnoreCase(String nome);

    List<Produto> findByMarcaIgnoreCase(String marca);

    List<Produto> findByGeneroIgnoreCase(String genero);

    List<Produto> findByGeneroIgnoreCaseAndMarcaIgnoreCase(String genero, String marca);
}