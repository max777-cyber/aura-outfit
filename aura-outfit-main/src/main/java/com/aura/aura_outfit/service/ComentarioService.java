package com.aura.aura_outfit.service;

import com.aura.aura_outfit.model.Comentario;
import com.aura.aura_outfit.model.Produto;
import com.aura.aura_outfit.model.Usuario;
import com.aura.aura_outfit.repository.ComentarioRepository;
import com.aura.aura_outfit.repository.ProdutoRepository;
import com.aura.aura_outfit.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;

    public ComentarioService(ComentarioRepository comentarioRepository,
                             ProdutoRepository produtoRepository,
                             UsuarioRepository usuarioRepository) {
        this.comentarioRepository = comentarioRepository;
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Comentario> listarPorProduto(Long produtoId) {
        return comentarioRepository.findByProdutoIdOrderByDataCriacaoDesc(produtoId);
    }

    public Double mediaNota(Long produtoId) {
        Double media = comentarioRepository.mediaNotasPorProduto(produtoId);
        return media != null ? Math.round(media * 10.0) / 10.0 : 0.0;
    }

    public Comentario buscarPorId(Long id) {
        return comentarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comentário não encontrado"));
    }

    public Comentario adicionar(Long produtoId, Long usuarioId, String texto, Integer nota) {
        if (nota == null || nota < 1 || nota > 5) {
            throw new RuntimeException("Nota deve ser entre 1 e 5!");
        }

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (comentarioRepository.existsByProdutoIdAndUsuarioId(produtoId, usuarioId)) {
            throw new RuntimeException("Você já avaliou este produto!");
        }

        Comentario comentario = new Comentario();
        comentario.setProduto(produto);
        comentario.setUsuario(usuario);
        comentario.setTexto(texto);
        comentario.setNota(nota);

        return comentarioRepository.save(comentario);
    }

    public void deletar(Long id) {
        comentarioRepository.deleteById(id);
    }
}