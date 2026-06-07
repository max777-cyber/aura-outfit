const API = "http://localhost:8080";

let usuario = null;
let carrinhoAtual = null;
let subtotalAtual = 0;
let freteSelecionado = 0;

function moeda(valor) {
    return "R$ " + Number(valor || 0).toFixed(2).replace(".", ",");
}

function mostrarErro(msg) {
    const el = document.getElementById("msgErr");
    if (!el) return;
    el.textContent = msg;
    el.style.display = "block";
    document.getElementById("msgOk").style.display = "none";
}

function mostrarOk(msg) {
    const el = document.getElementById("msgOk");
    if (!el) return;
    el.textContent = msg;
    el.style.display = "block";
    document.getElementById("msgErr").style.display = "none";
}

async function verificarSessao() {
    const res = await fetch(`${API}/api/usuarios/sessao`, {
        credentials: "include"
    });
    const data = await res.json();

    if (!data.logado) {
        window.location.href = "/login";
        return null;
    }

    usuario = data;
    return data;
}

async function carregarCarrinho() {
    try {
        const sessao = await verificarSessao();
        if (!sessao) return;

        const res = await fetch(`${API}/api/carrinho/${sessao.id}`, {
            credentials: "include"
        });

        if (!res.ok) {
            throw new Error("Nao foi possivel carregar o carrinho");
        }

        carrinhoAtual = await res.json();
        renderizarCarrinho(carrinhoAtual);
    } catch {
        document.getElementById("itens").innerHTML =
            '<div class="vazio">Erro ao carregar carrinho.</div>';
    }
}

function renderizarCarrinho(carrinho) {
    const itensEl = document.getElementById("itens");
    const itens = carrinho?.itens || [];
    const cartCount = document.getElementById("cartCount");
    if (cartCount) {
        cartCount.textContent = itens.reduce((total, item) => total + Number(item.quantidade || 0), 0);
    }

    subtotalAtual = carrinho?.total || 0;

    if (!itens.length) {
        itensEl.innerHTML = '<div class="vazio"><a class="empty-action" href="/">Ver produtos</a></div>';
        document.getElementById("btnFinalizar").disabled = true;
        freteSelecionado = 0;
        limparFrete();
        atualizarResumo();
        return;
    }

    document.getElementById("btnFinalizar").disabled = false;

    itensEl.innerHTML = itens.map(item => {
        const produto = item.produto || {};
        const imagem = produto.imagemUrl
            ? `<img src="${produto.imagemUrl}" alt="${produto.nome}" onerror="this.style.display='none'">`
            : "";

        return `
            <div class="item">
                <div class="item-img">${imagem}</div>
                <div class="item-info">
                    <h3>${produto.nome || "Produto"}</h3>
                    <p>${produto.marca || ""}${item.tamanho ? " · Tam. " + item.tamanho : ""}</p>
                    <strong>${moeda(produto.preco)}</strong>
                </div>
                <div class="item-qtd">
                    <button onclick="alterarQuantidade(${produto.id}, ${item.quantidade - 1}, '${item.tamanho || ""}')">-</button>
                    <span>${item.quantidade}</span>
                    <button onclick="alterarQuantidade(${produto.id}, ${item.quantidade + 1}, '${item.tamanho || ""}')">+</button>
                </div>
                <button class="btn-remover" onclick="removerItem(${produto.id}, '${item.tamanho || ""}')">Remover</button>
            </div>
        `;
    }).join("");

    atualizarResumo();
}

async function alterarQuantidade(produtoId, quantidade, tamanho) {
    try {
        await fetch(
            `${API}/api/carrinho/${usuario.id}/produtos/${produtoId}?quantidade=${quantidade}&tamanho=${encodeURIComponent(tamanho)}`,
            {
                method: "PUT",
                credentials: "include"
            }
        );
        await carregarCarrinho();
    } catch {
        mostrarErro("Nao foi possivel atualizar a quantidade.");
    }
}

async function removerItem(produtoId, tamanho) {
    try {
        await fetch(
            `${API}/api/carrinho/${usuario.id}/produtos/${produtoId}?tamanho=${encodeURIComponent(tamanho)}`,
            {
                method: "DELETE",
                credentials: "include"
            }
        );
        await carregarCarrinho();
    } catch {
        mostrarErro("Nao foi possivel remover o item.");
    }
}

async function limpar() {
    try {
        await fetch(`${API}/api/carrinho/${usuario.id}/limpar`, {
            method: "DELETE",
            credentials: "include"
        });
        await carregarCarrinho();
    } catch {
        mostrarErro("Nao foi possivel limpar o carrinho.");
    }
}

function mascaraCep(input) {
    let v = input.value.replace(/\D/g, "").slice(0, 8);
    if (v.length > 5) {
        v = v.slice(0, 5) + "-" + v.slice(5);
    }
    input.value = v;
}

async function calcularFrete() {
    const erro = document.getElementById("freteErro");
    const resultado = document.getElementById("freteResultado");
    const endereco = document.getElementById("freteEndereco");
    const opcoes = document.getElementById("freteOpcoes");
    const btn = document.getElementById("btnCep");
    const cep = document.getElementById("cepInput").value.replace(/\D/g, "");

    erro.textContent = "";
    resultado.style.display = "none";
    opcoes.innerHTML = "";

    if (cep.length !== 8) {
        erro.textContent = "Digite um CEP valido com 8 numeros.";
        return;
    }

    btn.disabled = true;
    btn.textContent = "Calculando...";

    try {
        const dados = await buscarEnderecoPorCep(cep);

        if (!dados || dados.erro) {
            throw new Error("CEP nao encontrado");
        }

        const fretes = montarOpcoesFrete(dados.uf, subtotalAtual);

        endereco.textContent = `${dados.logradouro || "Endereco"} - ${dados.bairro || ""}, ${dados.localidade || ""}/${dados.uf || ""}`;
        opcoes.innerHTML = fretes.map((frete, index) => `
            <button
                class="frete-opcao"
                onclick="selecionarFrete(${frete.valor}, '${frete.nome}')"
                ${index === 0 ? "data-primeiro='true'" : ""}>
                <span>${frete.nome}</span>
                <strong>${frete.valor === 0 ? "Gratis" : moeda(frete.valor)}</strong>
                <small>${frete.prazo}</small>
            </button>
        `).join("");

        resultado.style.display = "block";
        selecionarFrete(fretes[0].valor, fretes[0].nome);
    } catch {
        const fretes = montarOpcoesFretePorCep(cep, subtotalAtual);
        endereco.textContent = "CEP informado: " + cep.replace(/^(\d{5})(\d{3})$/, "$1-$2");
        opcoes.innerHTML = fretes.map(frete => `
            <button class="frete-opcao" onclick="selecionarFrete(${frete.valor}, '${frete.nome}')">
                <span>${frete.nome}</span>
                <strong>${frete.valor === 0 ? "Gratis" : moeda(frete.valor)}</strong>
                <small>${frete.prazo}</small>
            </button>
        `).join("");
        resultado.style.display = "block";
        selecionarFrete(fretes[0].valor, fretes[0].nome);
        erro.textContent = "Nao consegui consultar o endereco, mas calculei o frete pelo CEP.";
    } finally {
        btn.disabled = false;
        btn.textContent = "Calcular";
    }
}

async function buscarEnderecoPorCep(cep) {
    const res = await fetch(`https://viacep.com.br/ws/${cep}/json/`);
    if (!res.ok) {
        throw new Error("Falha ao consultar CEP");
    }
    return res.json();
}

function montarOpcoesFrete(uf, subtotal) {
    const grupo = {
        SP: 12.90, RJ: 16.90, MG: 16.90, ES: 16.90,
        PR: 18.90, SC: 19.90, RS: 21.90,
        DF: 22.90, GO: 22.90, MT: 26.90, MS: 26.90,
        BA: 27.90, SE: 27.90, AL: 29.90, PE: 29.90, PB: 31.90, RN: 31.90, CE: 32.90, PI: 34.90, MA: 34.90,
        AC: 39.90, AM: 39.90, AP: 39.90, PA: 37.90, RO: 39.90, RR: 39.90, TO: 35.90
    };

    const base = grupo[(uf || "").toUpperCase()] || 29.90;
    return montarFretes(base, subtotal);
}

function montarOpcoesFretePorCep(cep, subtotal) {
    const primeiro = cep.charAt(0);
    const basePorPrimeiroDigito = {
        0: 12.90, 1: 12.90, 2: 16.90, 3: 16.90,
        4: 27.90, 5: 29.90, 6: 32.90, 7: 24.90,
        8: 18.90, 9: 21.90
    };
    return montarFretes(basePorPrimeiroDigito[primeiro] || 29.90, subtotal);
}

function montarFretes(base, subtotal) {
    const economico = subtotal >= 299 ? 0 : base;
    return [
        { nome: "Entrega economica", valor: economico, prazo: "5 a 9 dias uteis" },
        { nome: "Entrega expressa", valor: base + 12, prazo: "2 a 4 dias uteis" }
    ];
}

function selecionarFrete(valor, nome) {
    freteSelecionado = Number(valor || 0);
    document.getElementById("freteValor").textContent =
        freteSelecionado === 0 ? "Gratis" : moeda(freteSelecionado);
    atualizarResumo();

    document.querySelectorAll(".frete-opcao").forEach(btn => {
        btn.classList.toggle("selecionado", btn.textContent.includes(nome));
    });
}

function limparFrete() {
    document.getElementById("freteValor").textContent = "—";
    document.getElementById("freteErro").textContent = "";
    document.getElementById("freteResultado").style.display = "none";
    document.getElementById("freteOpcoes").innerHTML = "";
}

function atualizarResumo() {
    document.getElementById("subtotal").textContent = moeda(subtotalAtual);
    document.getElementById("total").textContent = moeda(subtotalAtual + freteSelecionado);
}

async function finalizar() {
    if (!usuario) {
        window.location.href = "/login";
        return;
    }

    if (!carrinhoAtual || !(carrinhoAtual.itens || []).length) {
        mostrarErro("Seu carrinho esta vazio.");
        return;
    }

    try {
        const res = await fetch(`${API}/api/pedidos/finalizar/${usuario.id}`, {
            method: "POST",
            credentials: "include"
        });

        const pedido = await res.json().catch(() => ({}));

        if (!res.ok) {
            throw new Error(pedido.erro || "Erro ao finalizar pedido");
        }

        sessionStorage.setItem("ultimoPedidoId", pedido.id);
        mostrarOk("Pedido criado com sucesso.");
        window.location.href = `/pedido?pedidoId=${pedido.id}`;
    } catch (e) {
        mostrarErro(e.message || "Nao foi possivel finalizar o pedido.");
    }
}

async function sair() {
    await fetch(`${API}/api/usuarios/logout`, {
        method: "POST",
        credentials: "include"
    });
    window.location.href = "/login";
}

carregarCarrinho();
