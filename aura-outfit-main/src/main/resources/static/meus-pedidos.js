const API_PEDIDOS = "http://localhost:8080";
let usuarioPedidos = null;
let pedidosAtuais = [];

window.addEventListener("DOMContentLoaded", iniciarMeusPedidos);

async function iniciarMeusPedidos() {
  try {
    const res = await fetch(`${API_PEDIDOS}/api/usuarios/sessao`, { credentials: "include" });
    if (!res.ok) {
      window.location.href = "/login";
      return;
    }
    usuarioPedidos = await res.json();
    await carregarPedidos();
  } catch {
    window.location.href = "/login";
  }
}

async function carregarPedidos() {
  try {
    const res = await fetch(`${API_PEDIDOS}/api/usuarios/${usuarioPedidos.id}/pedidos`, { credentials: "include" });
    if (!res.ok) throw new Error("Erro ao carregar pedidos");
    renderizarPedidos(await res.json() || []);
  } catch {
    document.getElementById("listaPedidos").innerHTML = '<div class="vazio">Nao foi possivel carregar seus pedidos.</div>';
  }
}

function renderizarPedidos(pedidos) {
  pedidosAtuais = pedidos || [];
  atualizarStats(pedidosAtuais);
  renderizarListaPedidos(pedidosAtuais);
}

function atualizarStats(pedidos) {
  document.getElementById("totalPedidos").textContent = pedidos.length;
  document.getElementById("pedidosAndamento").textContent = pedidos.filter(p => normalizarStatus(p.status) === "andamento").length;
  document.getElementById("pedidosEntregues").textContent = pedidos.filter(p => normalizarStatus(p.status) === "entregue").length;
  document.getElementById("pedidosCancelados").textContent = pedidos.filter(p => normalizarStatus(p.status) === "cancelado").length;
}

function normalizarStatus(status) {
  const texto = String(status || "").toLowerCase();
  if (texto.includes("cancel")) return "cancelado";
  if (texto.includes("entreg") || texto.includes("aprov") || texto.includes("confirm")) return "entregue";
  return "andamento";
}

function statusTexto(status) {
  const tipo = normalizarStatus(status);
  if (status) return status;
  if (tipo === "entregue") return "Entregue";
  if (tipo === "cancelado") return "Cancelado";
  return "Em andamento";
}

function obterDataPedido(pedido) {
  return pedido.dataCriacao || pedido.dataPedido || pedido.criadoEm || pedido.createdAt || pedido.data || pedido.dataHora;
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

function formatarDataCurta(valor) {
  const data = valor ? new Date(valor) : null;
  if (!data || Number.isNaN(data.getTime())) return "Data nao informada";
  return data.toLocaleDateString("pt-BR", {
    day: "2-digit",
    month: "long",
    year: "numeric"
  });
}

function contarItensPedido(pedido) {
  const itens = pedido.itens || pedido.items || [];
  if (Array.isArray(itens) && itens.length) {
    return itens.reduce((total, item) => total + Number(item.quantidade || 1), 0);
  }
  return Number(pedido.quantidadeItens || pedido.quantidade || 1);
}

function imagemPedido(pedido) {
  const item = Array.isArray(pedido.itens || pedido.items) ? (pedido.itens || pedido.items)[0] : null;
  return pedido.imagem || pedido.imagemUrl || item?.imagem || item?.imagemUrl || item?.produto?.imagem || item?.produto?.imagemUrl || "";
}

function formatarTotal(pedido) {
  return Number(pedido.valorTotal || pedido.total || 0).toLocaleString("pt-BR", {
    style: "currency",
    currency: "BRL"
  });
}

function numeroPedido(pedido) {
  return `#AUR${String(pedido.id || 0).padStart(6, "0")}`;
}

function renderizarListaPedidos(pedidos) {
  const lista = document.getElementById("listaPedidos");
  if (!pedidos.length) {
    lista.innerHTML = '<div class="vazio">Nenhum pedido encontrado.</div>';
    return;
  }

  lista.innerHTML = pedidos.map(pedido => {
    const tipo = normalizarStatus(pedido.status);
    const itens = contarItensPedido(pedido);
    const imagem = imagemPedido(pedido);
    const data = obterDataPedido(pedido);
    const detalheUrl = `/pedido?pedidoId=${pedido.id}`;
    const botaoSecundario = tipo === "entregue" || tipo === "cancelado";

    return `
      <article class="pedido-card" data-status="${tipo}" data-pedido="${numeroPedido(pedido).toLowerCase()}">
        <div class="pedido-thumb">
          ${imagem ? `<img src="${imagem}" alt="Pedido ${numeroPedido(pedido)}" onerror="this.style.display='none'">` : "<span>A</span>"}
        </div>

        <div class="pedido-info">
          <div class="pedido-title-row">
            <h2>${numeroPedido(pedido)}</h2>
            <span class="pedido-badge ${tipo}">${statusTexto(pedido.status)}</span>
          </div>
          <p>${formatarDataPedido(data)}</p>

          <div class="pedido-meta">
            <div>
              <small>Itens</small>
              <strong>${itens} ${itens === 1 ? "produto" : "produtos"}</strong>
            </div>
            <div>
              <small>Total</small>
              <strong>${formatarTotal(pedido)}</strong>
            </div>
          </div>
        </div>

        <div class="pedido-side">
          <div>
            <small>${tipo === "entregue" ? "Entregue em" : tipo === "cancelado" ? "Cancelado em" : "Previsao de entrega"}</small>
            <strong>${tipo === "andamento" ? "Acompanhe no detalhe" : formatarDataCurta(data)}</strong>
          </div>
          <a class="${botaoSecundario ? "secundario" : ""}" href="${detalheUrl}">Ver detalhes</a>
        </div>
      </article>
    `;
  }).join("");
}

function filtrarPedidos() {
  const busca = (document.getElementById("buscaPedido")?.value || "").trim().toLowerCase();
  const status = document.getElementById("filtroStatusPedido")?.value || "";
  const filtrados = pedidosAtuais.filter(pedido => {
    const id = String(pedido.id || "").toLowerCase();
    const codigo = numeroPedido(pedido).toLowerCase();
    const statusPedido = normalizarStatus(pedido.status);
    return (!busca || id.includes(busca) || codigo.includes(busca)) && (!status || statusPedido === status);
  });
  renderizarListaPedidos(filtrados);
}

async function sairPedidos() {
  await fetch(`${API_PEDIDOS}/api/usuarios/logout`, { method: "POST", credentials: "include" }).catch(() => {});
  window.location.href = "/login";
}
