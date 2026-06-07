package com.aura.aura_outfit.config;

import com.aura.aura_outfit.model.EstoqueProduto;
import com.aura.aura_outfit.model.Produto;
import com.aura.aura_outfit.model.ProdutoImagem;
import com.aura.aura_outfit.repository.EstoqueRepository;
import com.aura.aura_outfit.repository.ProdutoImagemRepository;
import com.aura.aura_outfit.repository.ProdutoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProdutoRepository produtoRepository;
    private final EstoqueRepository estoqueRepository;
    private final ProdutoImagemRepository produtoImagemRepository;

    public DataInitializer(ProdutoRepository produtoRepository, EstoqueRepository estoqueRepository, ProdutoImagemRepository produtoImagemRepository) {
        this.produtoRepository = produtoRepository;
        this.estoqueRepository = estoqueRepository;
        this.produtoImagemRepository = produtoImagemRepository;
    }

    @Override
    public void run(String... args) {
        removerProdutosTemporarios();
        migrarBoneEclipseGenerico();
        migrarCortaVentoGenerico();
        migrarTenisAuraRunnerGenerico();

        Produto bone = salvarProduto("Bone Aura Outfit", "Aura", "Preto", "Unissex", 59.90, "/imagens/bone/1.png");
        Produto boneEclipseVermelho = salvarProduto("Eclipse Vermelho", "Aura", "Vermelho/Branco", "Unissex", 69.90, "/imagens/bone Aura Eclipse/vermelho e branco/1.png");
        Produto boneEclipsePretoRoxo = salvarProduto("Eclipse Preto Roxo", "Aura", "Preto/Roxo", "Unissex", 69.90, "/imagens/bone Aura Eclipse/preto e roxo/1.png");
        Produto boneEclipseBrancoRoxo = salvarProduto("Eclipse Branco Roxo", "Aura", "Branco/Roxo", "Unissex", 69.90, "/imagens/bone Aura Eclipse/branco e roxo/1.png");
        Produto calcaCargo = salvarProduto("Calca Cargo Preta", "Aura", "Preto", "Masculino", 159.90, "/imagens/calca-cargo/1.png");
        Produto calcaJeans = salvarProduto("Calca Cargo Jeans", "Aura", "Jeans", "Masculino", 189.90, "/imagens/calca-cargo-jeans/1.png");
        Produto oversizedDark = salvarProduto("Oversized Aura Dark", "Aura", "Branco", "Unissex", 89.90, "/imagens/camisa-1/1.png");
        Produto oversizedShadow = salvarProduto("Oversized Aura Shadow", "Aura", "Preto", "Unissex", 89.90, "/imagens/camisa-2/1.png");
        Produto camisaOversized = salvarProduto("Camisa Oversized Aura", "Aura", "Verde", "Unissex", 89.90, "/imagens/camisa-3/1.png");
        Produto jaquetaColegial = salvarProduto("Jaqueta Colegial", "Aura", "Preto", "Unissex", 249.90, "/imagens/jaqueta-colegial/1.png");
        Produto jaquetaJeans = salvarProduto("Jaqueta Jeans", "Aura", "Jeans", "Unissex", 279.90, "/imagens/jaqueta-jeans/1.png");
        Produto moletom = salvarProduto("Moletom com Toca", "Aura", "Cinza", "Unissex", 219.90, "/imagens/moletom-toca/1.png");
        Produto cortaVentoRoxo = salvarProduto("Corta Vento Aura Roxo", "Aura", "Roxo", "Unissex", 199.90, "/imagens/corta-vento-aura/roxo/1.png");
        Produto cortaVentoPreto = salvarProduto("Corta Vento Aura Preto", "Aura", "Preto", "Unissex", 199.90, "/imagens/corta-vento-aura/preto/1.png");
        Produto cortaVentoBranco = salvarProduto("Corta Vento Aura Branco", "Aura", "Branco", "Unissex", 199.90, "/imagens/corta-vento-aura/branco/1.png");
        Produto cortaVentoVermelho = salvarProduto("Corta Vento Aura Vermelho", "Aura", "Vermelho", "Unissex", 199.90, "/imagens/corta-vento-aura/vermelho/1.png");
        Produto tenisRoxo = salvarProduto("Tenis Aura Runner Roxo", "Aura", "Roxo", "Unissex", 299.90, "/imagens/tenis-aura-runner/roxo/1.png");
        Produto tenisBranco = salvarProduto("Tenis Aura Runner Branco", "Aura", "Branco", "Unissex", 299.90, "/imagens/tenis-aura-runner/branco/1.png");
        Produto tenisVermelho = salvarProduto("Tenis Aura Runner Vermelho", "Aura", "Vermelho", "Unissex", 299.90, "/imagens/tenis-aura-runner/vermelho/1.png");
        Produto blusa = salvarProduto("Blusa Roxa Aura", "Aura", "Roxo", "Unissex", 189.90, "/imagens/blusa-roxa-aura/1.png");

        salvarImagens(bone, List.of("/imagens/bone/1.png", "/imagens/bone/2.png"));
        salvarImagens(boneEclipseVermelho, List.of("/imagens/bone Aura Eclipse/vermelho e branco/1.png", "/imagens/bone Aura Eclipse/vermelho e branco/2.jpg"));
        salvarImagens(boneEclipsePretoRoxo, List.of("/imagens/bone Aura Eclipse/preto e roxo/1.png", "/imagens/bone Aura Eclipse/preto e roxo/2.jpg"));
        salvarImagens(boneEclipseBrancoRoxo, List.of("/imagens/bone Aura Eclipse/branco e roxo/1.png", "/imagens/bone Aura Eclipse/branco e roxo/2.png"));
        salvarImagens(calcaCargo, List.of("/imagens/calca-cargo/1.png", "/imagens/calca-cargo/2.png", "/imagens/calca-cargo/3.png"));
        salvarImagens(calcaJeans, List.of("/imagens/calca-cargo-jeans/1.png", "/imagens/calca-cargo-jeans/2.png", "/imagens/calca-cargo-jeans/3.png"));
        salvarImagens(oversizedDark, List.of("/imagens/camisa-1/1.png", "/imagens/camisa-1/2.png", "/imagens/camisa-1/3.png"));
        salvarImagens(oversizedShadow, List.of("/imagens/camisa-2/1.png", "/imagens/camisa-2/2.png", "/imagens/camisa-2/3.png", "/imagens/camisa-2/4.png"));
        salvarImagens(camisaOversized, List.of("/imagens/camisa-3/1.png", "/imagens/camisa-3/2.png", "/imagens/camisa-3/3.png"));
        salvarImagens(jaquetaColegial, List.of("/imagens/jaqueta-colegial/1.png", "/imagens/jaqueta-colegial/2.png", "/imagens/jaqueta-colegial/3.png", "/imagens/jaqueta-colegial/4.png"));
        salvarImagens(jaquetaJeans, List.of("/imagens/jaqueta-jeans/1.png", "/imagens/jaqueta-jeans/2.png", "/imagens/jaqueta-jeans/3.png"));
        salvarImagens(moletom, List.of("/imagens/moletom-toca/1.png", "/imagens/moletom-toca/2.png", "/imagens/moletom-toca/3.png"));
        salvarImagens(cortaVentoRoxo, List.of("/imagens/corta-vento-aura/roxo/1.png", "/imagens/corta-vento-aura/roxo/2.png", "/imagens/corta-vento-aura/roxo/3.png", "/imagens/corta-vento-aura/roxo/4.png"));
        salvarImagens(cortaVentoPreto, List.of("/imagens/corta-vento-aura/preto/1.png", "/imagens/corta-vento-aura/preto/2.png"));
        salvarImagens(cortaVentoBranco, List.of("/imagens/corta-vento-aura/branco/1.png", "/imagens/corta-vento-aura/branco/2.png"));
        salvarImagens(cortaVentoVermelho, List.of("/imagens/corta-vento-aura/vermelho/1.png", "/imagens/corta-vento-aura/vermelho/2.png"));
        salvarImagens(tenisRoxo, List.of("/imagens/tenis-aura-runner/roxo/1.png", "/imagens/tenis-aura-runner/roxo/2.png", "/imagens/tenis-aura-runner/roxo/3.png", "/imagens/tenis-aura-runner/roxo/4.png"));
        salvarImagens(tenisBranco, List.of("/imagens/tenis-aura-runner/branco/1.png"));
        salvarImagens(tenisVermelho, List.of("/imagens/tenis-aura-runner/vermelho/1.png"));
        salvarImagens(blusa, List.of("/imagens/blusa-roxa-aura/1.png"));

        salvarEstoque(bone, Map.of("Unico", 50));
        salvarEstoque(boneEclipseVermelho, Map.of("Unico", 40));
        salvarEstoque(boneEclipsePretoRoxo, Map.of("Unico", 40));
        salvarEstoque(boneEclipseBrancoRoxo, Map.of("Unico", 40));
        salvarEstoque(calcaCargo, Map.of("P", 10, "M", 15, "G", 12, "GG", 8));
        salvarEstoque(calcaJeans, Map.of("P", 8, "M", 12, "G", 10, "GG", 6));
        salvarEstoque(oversizedDark, Map.of("PP", 10, "P", 20, "M", 25, "G", 20, "GG", 10));
        salvarEstoque(oversizedShadow, Map.of("PP", 8, "P", 18, "M", 22, "G", 18, "GG", 8));
        salvarEstoque(camisaOversized, Map.of("PP", 6, "P", 15, "M", 20, "G", 15, "GG", 6));
        salvarEstoque(jaquetaColegial, Map.of("P", 10, "M", 12, "G", 10, "GG", 5));
        salvarEstoque(jaquetaJeans, Map.of("P", 8, "M", 10, "G", 8, "GG", 4));
        salvarEstoque(moletom, Map.of("P", 12, "M", 18, "G", 15, "GG", 8));
        salvarEstoque(cortaVentoRoxo, Map.of("P", 8, "M", 12, "G", 10, "GG", 5));
        salvarEstoque(cortaVentoPreto, Map.of("P", 8, "M", 12, "G", 10, "GG", 5));
        salvarEstoque(cortaVentoBranco, Map.of("P", 8, "M", 12, "G", 10, "GG", 5));
        salvarEstoque(cortaVentoVermelho, Map.of("P", 8, "M", 12, "G", 10, "GG", 5));
        salvarEstoque(tenisRoxo, Map.of("38", 7, "39", 9, "40", 12, "41", 10, "42", 8, "43", 5));
        salvarEstoque(tenisBranco, Map.of("38", 7, "39", 9, "40", 12, "41", 10, "42", 8, "43", 5));
        salvarEstoque(tenisVermelho, Map.of("38", 7, "39", 9, "40", 12, "41", 10, "42", 8, "43", 5));
        salvarEstoque(blusa, Map.of("PP", 8, "P", 16, "M", 18, "G", 12, "GG", 6));
    }

    private Produto salvarProduto(String nome, String marca, String cor, String genero, Double preco, String imagemUrl) {
        Produto produto = produtoRepository.findByNomeContainingIgnoreCase(nome).stream()
                .filter(p -> nome.equalsIgnoreCase(p.getNome()))
                .findFirst()
                .orElseGet(Produto::new);
        produto.setNome(nome);
        produto.setMarca(marca);
        produto.setCor(cor);
        produto.setGenero(genero);
        produto.setPreco(preco);
        produto.setImagemUrl(imagemUrl);
        if (produto.getQuantidade() == null) {
            produto.setQuantidade(0);
        }
        return produtoRepository.save(produto);
    }

    private void salvarImagens(Produto produto, List<String> urls) {
        List<ProdutoImagem> imagensExistentes = produtoImagemRepository.findByProdutoIdOrderByOrdemAsc(produto.getId());
        List<String> urlsExistentes = imagensExistentes.stream()
                .map(ProdutoImagem::getUrl)
                .toList();

        if (urlsExistentes.equals(urls)) {
            return;
        }

        produtoImagemRepository.deleteAll(imagensExistentes);

        for (int i = 0; i < urls.size(); i++) {
            produtoImagemRepository.save(new ProdutoImagem(produto, urls.get(i), i));
        }
    }

    private void salvarEstoque(Produto produto, Map<String, Integer> estoquePorTamanho) {
        int quantidadeTotal = 0;

        for (Map.Entry<String, Integer> item : estoquePorTamanho.entrySet()) {
            String tamanho = item.getKey();
            Integer quantidade = item.getValue();
            quantidadeTotal += quantidade;

            Optional<EstoqueProduto> existente = estoqueRepository
                    .findByProdutoIdAndTamanho(produto.getId(), tamanho);

            EstoqueProduto estoque = existente.orElseGet(() -> new EstoqueProduto(produto, tamanho, 0));
            estoque.setQuantidade(quantidade);
            estoqueRepository.save(estoque);
        }

        produto.setQuantidade(quantidadeTotal);
        produtoRepository.save(produto);
    }

    private void removerProdutosTemporarios() {
        List<String> nomesTemporarios = List.of(
                "Camiseta Aura Basic",
                "Moletom Oversized",
                "Calca Wide Leg",
                "Jaqueta Street",
                "Cropped Essential",
                "Camisa Relax Fit"
        );

        for (String nome : nomesTemporarios) {
            produtoRepository.findByNomeContainingIgnoreCase(nome).stream()
                    .filter(p -> nome.equalsIgnoreCase(p.getNome()))
                    .forEach(produto -> {
                        estoqueRepository.findByProdutoId(produto.getId())
                                .forEach(estoqueRepository::delete);
                        produtoImagemRepository.findByProdutoIdOrderByOrdemAsc(produto.getId())
                                .forEach(produtoImagemRepository::delete);
                        produtoRepository.delete(produto);
                    });
        }
    }

    private void migrarBoneEclipseGenerico() {
        produtoRepository.findByNomeContainingIgnoreCase("Bone Aura Eclipse").stream()
                .forEach(produto -> {
                    String nome = produto.getNome();

                    if ("Bone Aura Eclipse".equalsIgnoreCase(nome)
                            || "Bone Aura Eclipse Vermelho".equalsIgnoreCase(nome)) {
                        produto.setNome("Eclipse Vermelho");
                        produto.setCor("Vermelho/Branco");
                        produto.setImagemUrl("/imagens/bone Aura Eclipse/vermelho e branco/1.png");

                    } else if ("Bone Aura Eclipse Preto Roxo".equalsIgnoreCase(nome)) {
                        produto.setNome("Eclipse Preto Roxo");
                        produto.setCor("Preto/Roxo");
                        produto.setImagemUrl("/imagens/bone Aura Eclipse/preto e roxo/1.png");

                    } else if ("Bone Aura Eclipse Branco Roxo".equalsIgnoreCase(nome)) {
                        produto.setNome("Eclipse Branco Roxo");
                        produto.setCor("Branco/Roxo");
                        produto.setImagemUrl("/imagens/bone Aura Eclipse/branco e roxo/1.png");
                    }

                    produtoRepository.save(produto);
                });
    }

    private void migrarCortaVentoGenerico() {
        produtoRepository.findByNomeContainingIgnoreCase("Corta Vento Aura").stream()
                .forEach(produto -> {
                    String nome = produto.getNome();

                    if ("Corta Vento Aura".equalsIgnoreCase(nome)) {
                        produto.setNome("Corta Vento Aura Roxo");
                        produto.setCor("Roxo");
                        produto.setImagemUrl("/imagens/corta-vento-aura/roxo/1.png");
                    }

                    produtoRepository.save(produto);
                });
    }

    private void migrarTenisAuraRunnerGenerico() {
        produtoRepository.findByNomeContainingIgnoreCase("Tenis Aura Runner").stream()
                .forEach(produto -> {
                    String nome = produto.getNome();

                    if ("Tenis Aura Runner".equalsIgnoreCase(nome)) {
                        produto.setNome("Tenis Aura Runner Roxo");
                        produto.setCor("Roxo");
                        produto.setImagemUrl("/imagens/tenis-aura-runner/roxo/1.png");
                    }

                    produtoRepository.save(produto);
                });
    }
}

