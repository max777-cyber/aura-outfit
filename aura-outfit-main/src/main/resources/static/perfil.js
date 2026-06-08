const API = "";
let usuarioLogado = null;
let perfilAtual = null;
let pedidosAtuais = [];

window.addEventListener("DOMContentLoaded", iniciarPerfil);

async function iniciarPerfil() {
  document.getElementById("inputFotoPerfil")?.addEventListener("change", selecionarFotoPerfil);
  try {
    const res = await fetch(`${API}/api/usuarios/sessao`, { credentials: "include" });
    if (!res.ok) {
      window.location.href = "/login";
      return;
    }
    usuarioLogado = await res.json();
    await carregarPerfil();
    await carregarPedidos();
    mostrarPainel(window.location.hash === "#pedidos" ? "pedidos" : null);
  } catch {
    window.location.href = "/login";
  }
}

window.addEventListener("hashchange", () => {
  if (window.location.hash === "#pedidos") mostrarPainel("pedidos");
});

async function carregarPerfil() {
  const res = await fetch(`${API}/api/usuarios/${usuarioLogado.id}/perfil`, { credentials: "include" });
  if (!res.ok) throw new Error("Erro ao carregar perfil");
  perfilAtual = await res.json();
  preencherCabecalho(perfilAtual);
  preencherFormulario(perfilAtual);
  verificarCadastroIncompleto(perfilAtual);
}

function preencherCabecalho(perfil) {
  document.getElementById("nomeHeader").textContent = perfil.nome || "Meu perfil";
  document.getElementById("emailHeader").textContent = perfil.email || "";
  document.getElementById("segEmail").textContent = perfil.email || "--";
  renderizarAvatar(perfil);
}

function renderizarAvatar(perfil) {
  const avatar = document.getElementById("avatar");
  const remover = document.getElementById("btnRemoverFoto");
  if (!avatar) return;
  if (perfil.fotoPerfil) {
    const img = document.createElement("img");
    img.src = perfil.fotoPerfil;
    img.alt = "Foto de perfil";
    avatar.replaceChildren(img);
    if (remover) remover.style.display = "inline-flex";
    return;
  }
  const nome = perfil.nome || perfil.email || "--";
  const iniciais = nome.split(" ").filter(Boolean).slice(0, 2).map(parte => parte[0]).join("").toUpperCase() || "--";
  avatar.textContent = iniciais;
  if (remover) remover.style.display = "none";
}

function preencherFormulario(perfil) {
  document.getElementById("inputNome").value = perfil.nome || "";
  document.getElementById("inputEmail").value = perfil.email || "";
  document.getElementById("inputCpf").value = perfil.documento || "";
  document.getElementById("inputTelefone").value = perfil.telefone || "";
  preencherEndereco(perfil.endereco);
  atualizarResumoEndereco(perfil.endereco);
}

function preencherEndereco(endereco) {
  const dados = parseEndereco(endereco);
  setValue("inputCep", dados.cep || "");
  setValue("inputLogradouro", dados.logradouro || dados.rua || "");
  setValue("inputNumero", dados.numero || "");
  setValue("inputBairro", dados.bairro || "");
  setValue("inputComplemento", dados.complemento || "");
  setValue("inputCidade", dados.cidade || "");
  setValue("inputUf", dados.uf || dados.estado || "");
}

function parseEndereco(endereco) {
  if (!endereco) return {};
  try { return JSON.parse(endereco); } catch { return { logradouro: endereco }; }
}

function atualizarResumoEndereco(endereco) {
  const el = document.getElementById("enderecoResumo");
  if (!el) return;
  const dados = parseEndereco(endereco);
  const rua = dados.logradouro || dados.rua || "";
  const numero = dados.numero ? `, ${dados.numero}` : "";
  const bairro = dados.bairro || "";
  const cidadeUf = [dados.cidade, dados.uf || dados.estado].filter(Boolean).join(" - ");
  const cep = dados.cep ? `CEP ${dados.cep}` : "";

  if (!rua && !bairro && !cidadeUf && !cep) {
    el.innerHTML = "<strong>Nenhum endereco cadastrado</strong><span>Adicione um endereco para agilizar sua compra.</span>";
    return;
  }

  const strong = document.createElement("strong");
  strong.textContent = rua + numero;
  const span = document.createElement("span");
  span.textContent = [bairro, cidadeUf, cep].filter(Boolean).join(" · ");
  el.replaceChildren(strong, span);
}

function montarEndereco() {
  return JSON.stringify({
    cep: getValue("inputCep"),
    logradouro: getValue("inputLogradouro"),
    numero: getValue("inputNumero"),
    bairro: getValue("inputBairro"),
    complemento: getValue("inputComplemento"),
    cidade: getValue("inputCidade"),
    uf: getValue("inputUf").toUpperCase()
  });
}

async function selecionarFotoPerfil(event) {
  const arquivo = event.target.files?.[0];
  if (!arquivo) return;
  if (!arquivo.type.startsWith("image/")) {
    alert("Escolha uma imagem valida.");
    return;
  }
  if (arquivo.size > 5 * 1024 * 1024) {
    alert("Escolha uma imagem de ate 5 MB.");
    return;
  }
  try {
    const fotoPerfil = await redimensionarImagem(arquivo, 512, 0.86);
    await salvarFotoPerfil(fotoPerfil);
  } catch {
    alert("Nao foi possivel salvar a foto.");
  } finally {
    event.target.value = "";
  }
}

function redimensionarImagem(arquivo, tamanhoMaximo, qualidade) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => {
      const img = new Image();
      img.onload = () => {
        const canvas = document.createElement("canvas");
        const lado = tamanhoMaximo;
        const menorLado = Math.min(img.width, img.height);
        const sx = (img.width - menorLado) / 2;
        const sy = (img.height - menorLado) / 2;
        canvas.width = lado;
        canvas.height = lado;
        const ctx = canvas.getContext("2d");
        ctx.fillStyle = "#111";
        ctx.fillRect(0, 0, lado, lado);
        ctx.drawImage(img, sx, sy, menorLado, menorLado, 0, 0, lado, lado);
        resolve(canvas.toDataURL("image/jpeg", qualidade));
      };
      img.onerror = reject;
      img.src = reader.result;
    };
    reader.onerror = reject;
    reader.readAsDataURL(arquivo);
  });
}

async function salvarFotoPerfil(fotoPerfil) {
  const res = await fetch(`${API}/api/usuarios/${usuarioLogado.id}/perfil/foto`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: JSON.stringify({ fotoPerfil })
  });
  if (!res.ok) throw new Error("Erro ao salvar foto");
  perfilAtual = await res.json();
  preencherCabecalho(perfilAtual);
}

async function removerFotoPerfil() {
  try { await salvarFotoPerfil(""); } catch { alert("Nao foi possivel remover a foto."); }
}

function mostrarPainel(nome) {
  document.querySelectorAll(".painel").forEach(painel => painel.classList.remove("ativo"));
  document.querySelectorAll(".card-item").forEach(card => card.classList.remove("ativo"));
  if (!nome) return;
  document.getElementById(`painel-${nome}`)?.classList.add("ativo");
  document.getElementById(`card-${nome}`)?.classList.add("ativo");
}

function cancelarEdicao() {
  if (perfilAtual) preencherFormulario(perfilAtual);
  esconderMensagens();
}

async function salvarDadosPessoais() {
  esconderMensagens();
  try {
    const res = await fetch(`${API}/api/usuarios/${usuarioLogado.id}/perfil`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify({
        nome: getValue("inputNome"),
        telefone: getValue("inputTelefone"),
        documento: getValue("inputCpf"),
        endereco: perfilAtual?.endereco || ""
      })
    });
    if (!res.ok) throw new Error(await res.text());
    perfilAtual = await res.json();
    preencherCabecalho(perfilAtual);
    preencherFormulario(perfilAtual);
    verificarCadastroIncompleto(perfilAtual);
    mostrarMsg("msgOkInfo");
  } catch {
    mostrarMsg("msgErrInfo", "Erro ao salvar dados pessoais.");
  }
}

async function salvarEndereco() {
  esconderMensagens();
  try {
    const res = await fetch(`${API}/api/usuarios/${usuarioLogado.id}/perfil`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify({
        nome: getValue("inputNome") || perfilAtual.nome,
        telefone: getValue("inputTelefone") || perfilAtual.telefone || "",
        documento: getValue("inputCpf") || perfilAtual.documento || "",
        endereco: montarEndereco()
      })
    });
    if (!res.ok) throw new Error(await res.text());
    perfilAtual = await res.json();
    preencherFormulario(perfilAtual);
    verificarCadastroIncompleto(perfilAtual);
    mostrarMsg("msgOkEnd");
  } catch {
    mostrarMsg("msgErrEnd", "Erro ao salvar endereco.");
  }
}

async function salvarSenha() {
  esconderMensagens();
  const senha = getValue("inputSenha");
  const confirmacao = getValue("inputSenhaConfirm");
  if (!senha) return mostrarMsg("msgErrSeg", "Digite uma nova senha.");
  if (senha.length < 6) return mostrarMsg("msgErrSeg", "A senha deve ter pelo menos 6 caracteres.");
  if (senha !== confirmacao) return mostrarMsg("msgErrSeg", "As senhas nao conferem.");
  try {
    const res = await fetch(`${API}/api/usuarios/${usuarioLogado.id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify({ senha })
    });
    if (!res.ok) throw new Error(await res.text());
    setValue("inputSenha", "");
    setValue("inputSenhaConfirm", "");
    mostrarMsg("msgOkSeg");
  } catch {
    mostrarMsg("msgErrSeg", "Erro ao atualizar senha.");
  }
}

async function buscarCep() {
  const cep = getValue("inputCep").replace(/\D/g, "");
  if (cep.length !== 8) return;
  const btn = document.getElementById("btnBuscarCep");
  if (btn) btn.textContent = "...";
  try {
    const res = await fetch(`https://viacep.com.br/ws/${cep}/json/`);
    const data = await res.json();
    if (!data.erro) {
      setValue("inputLogradouro", data.logradouro || "");
      setValue("inputBairro", data.bairro || "");
      setValue("inputCidade", data.localidade || "");
      setValue("inputUf", data.uf || "");
    }
  } catch {
  } finally {
    if (btn) btn.textContent = "Buscar";
  }
}

async function carregarPedidos() {
  if (!usuarioLogado?.id) return;
  try {
    const res = await fetch(`${API}/api/usuarios/${usuarioLogado.id}/pedidos`, { credentials: "include" });
    if (!res.ok) throw new Error("Erro pedidos");
    renderizarPedidos(await res.json() || []);
  } catch {
    document.getElementById("listaPedidos").innerHTML = '<div class="vazio">Nao foi possivel carregar pedidos.</div>';
  }
}

function renderizarPedidos(pedidos) {
  pedidosAtuais = pedidos || [];
  document.getElementById("totalPedidos").textContent = pedidos.length;
  document.getElementById("totalPedidosResumo").textContent = pedidos.length;
  document.getElementById("pedidosAndamento").textContent = pedidos.filter(pedido => normalizarStatus(pedido.status) === "andamento").length;
  document.getElementById("pedidosEntregues").textContent = pedidos.filter(pedido => normalizarStatus(pedido.status) === "entregue").length;
  document.getElementById("pedidosCancelados").textContent = pedidos.filter(pedido => normalizarStatus(pedido.status) === "cancelado").length;
  const total = pedidos.reduce((soma, pedido) => soma + Number(pedido.valorTotal || pedido.total || 0), 0);
  if (!pedidos.length) {
    document.getElementById("listaPedidos").innerHTML = '<div class="vazio">Nenhum pedido encontrado.</div>';
    document.getElementById("ultimoPedidoResumo").innerHTML = '<div class="vazio compacto">Nenhum pedido encontrado.</div>';
    return;
  }
  const ultimo = pedidos[0];
  const ultimoTotal = Number(ultimo.valorTotal || ultimo.total || 0).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
  document.getElementById("ultimoPedidoResumo").innerHTML = `
    <div class="pedido-resumo">
      <div>
        <strong>Pedido #${ultimo.id}</strong>
        <span>${ultimo.status || "Em andamento"}</span>
      </div>
      <div>
        <div class="pedido-status">${ultimo.status || "Aberto"}</div>
        <span>${ultimoTotal}</span>
      </div>
    </div>
  `;
  renderizarListaPedidos(pedidos);
}

function normalizarStatus(status) {
  const texto = String(status || "").toLowerCase();
  if (texto.includes("cancel")) return "cancelado";
  if (texto.includes("entreg") || texto.includes("aprov") || texto.includes("confirm")) return "entregue";
  return "andamento";
}

function formatarDataPedido(valor) {
  const data = valor ? new Date(valor) : null;
  if (!data || Number.isNaN(data.getTime())) return "Data nao informada";
  return data.toLocaleString("pt-BR", {
    day: "2-digit",
    month: "long",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit"
  });
}

function formatarDataCurtaPedido(valor) {
  const data = valor ? new Date(valor) : null;
  if (!data || Number.isNaN(data.getTime())) return "Data nao informada";
  return data.toLocaleDateString("pt-BR", {
    day: "2-digit",
    month: "long",
    year: "numeric"
  });
}

function obterDataPedido(pedido) {
  return pedido.dataCriacao || pedido.dataPedido || pedido.criadoEm || pedido.createdAt || pedido.data || pedido.dataHora;
}

function contarItensPedido(pedido) {
  const itens = pedido.itens || pedido.items || [];
  if (Array.isArray(itens) && itens.length) {
    return itens.reduce((total, item) => total + Number(item.quantidade || 1), 0);
  }
  return Number(pedido.quantidadeItens || pedido.totalItens || 1);
}

function imagemPedido(pedido) {
  const itens = pedido.itens || pedido.items || [];
  const produto = Array.isArray(itens) ? (itens[0]?.produto || itens[0]) : null;
  return produto?.imagemUrl || produto?.imagem || pedido.imagemUrl || "";
}

function renderizarListaPedidos(pedidos) {
  const lista = document.getElementById("listaPedidos");
  if (!pedidos.length) {
    lista.innerHTML = '<div class="vazio">Nenhum pedido encontrado.</div>';
    return;
  }

  lista.innerHTML = pedidos.map(pedido => {
    const statusTipo = normalizarStatus(pedido.status);
    const statusTexto = pedido.status || (statusTipo === "entregue" ? "Entregue" : statusTipo === "cancelado" ? "Cancelado" : "Em andamento");
    const total = Number(pedido.valorTotal || pedido.total || 0).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
    const itens = contarItensPedido(pedido);
    const img = imagemPedido(pedido);
    const detalheUrl = `/pedido?pedidoId=${pedido.id}`;

    return `
      <article class="pedido-card pedido-${statusTipo}" data-status="${statusTipo}" data-pedido="#${pedido.id}">
        <div class="pedido-thumb">
          ${img ? `<img src="${img}" alt="Pedido #${pedido.id}" onerror="this.style.display='none'">` : `<span>A</span>`}
        </div>

        <div class="pedido-main">
          <div class="pedido-title-row">
            <h3>#AUR${String(pedido.id).padStart(6, "0")}</h3>
            <span class="pedido-badge">${statusTexto}</span>
          </div>
          <p>${formatarDataPedido(obterDataPedido(pedido))}</p>
          <div class="pedido-meta-grid">
            <div><small>Itens</small><strong>${itens} ${itens === 1 ? "produto" : "produtos"}</strong></div>
            <div><small>Total</small><strong>${total}</strong></div>
          </div>
        </div>

        <div class="pedido-side">
          <small>${statusTipo === "entregue" ? "Entregue em" : statusTipo === "cancelado" ? "Cancelado em" : "Previsao de entrega"}</small>
          <strong>${statusTipo === "andamento" ? "Acompanhe no detalhe" : formatarDataCurtaPedido(obterDataPedido(pedido))}</strong>
          <a href="${detalheUrl}">Ver detalhes</a>
        </div>
      </article>
    `;
  }).join("");
}

function filtrarPedidos() {
  const busca = getValue("buscaPedido").toLowerCase();
  const status = getValue("filtroStatusPedido");
  const filtrados = pedidosAtuais.filter(pedido => {
    const id = String(pedido.id || "").toLowerCase();
    const statusPedido = normalizarStatus(pedido.status);
    const bateBusca = !busca || id.includes(busca) || `aur${id.padStart(6, "0")}`.includes(busca);
    const bateStatus = !status || statusPedido === status;
    return bateBusca && bateStatus;
  });
  renderizarListaPedidos(filtrados);
}

function verificarCadastroIncompleto(perfil) {
  const incompleto = !perfil.telefone || !perfil.documento || !perfil.endereco;
  document.getElementById("alertaIncompleto").style.display = incompleto ? "block" : "none";
  document.getElementById("badgeEndereco").style.display = !perfil.endereco ? "block" : "none";
}

async function sair() {
  await fetch(`${API}/api/usuarios/logout`, { method: "POST", credentials: "include" }).catch(() => {});
  window.location.href = "/login";
}

function mascaraCpf(input) {
  input.value = input.value.replace(/\D/g, "").replace(/(\d{3})(\d)/, "$1.$2").replace(/(\d{3})(\d)/, "$1.$2").replace(/(\d{3})(\d{1,2})$/, "$1-$2");
}
function mascaraTelefone(input) {
  input.value = input.value.replace(/\D/g, "").replace(/(\d{2})(\d)/, "($1) $2").replace(/(\d{5})(\d)/, "$1-$2").slice(0, 15);
}
function mascaraCep(input) {
  input.value = input.value.replace(/\D/g, "").replace(/(\d{5})(\d)/, "$1-$2").slice(0, 9);
}
function getValue(id) { return document.getElementById(id)?.value.trim() || ""; }
function setValue(id, valor) { const el = document.getElementById(id); if (el) el.value = valor || ""; }
function mostrarMsg(id, texto) { const el = document.getElementById(id); if (!el) return; if (texto) el.textContent = texto; el.style.display = "block"; }
function esconderMensagens() { document.querySelectorAll(".msg").forEach(msg => msg.style.display = "none"); }
