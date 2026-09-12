(function () {
const API = window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1"
    ? ""
    : ""; // Deixar vazio em produção para usar caminhos relativos


  function firstName(name) {
    return (name || "Perfil").trim().split(/\s+/)[0] || "Perfil";
  }

  function initials(user) {
    const base = (user.nome || user.email || "A").trim();
    return base.split(/\s+/).filter(Boolean).slice(0, 2).map(part => part[0]).join("").toUpperCase() || "A";
  }

  function buildAvatar(user) {
    const span = document.createElement("span");
    span.className = "aura-user-avatar";
    if (user.fotoPerfil && user.fotoPerfil.startsWith("data:image/")) {
      const img = document.createElement("img");
      img.src = user.fotoPerfil;
      img.alt = "";
      span.appendChild(img);
    } else {
      span.textContent = initials(user);
    }
    return span;
  }

  function removeLegacyLinks(nav) {
    nav.querySelectorAll("ul a").forEach(link => {
      const list = link.closest("ul");
      if (list?.classList.contains("nav-menu")) return;

      const text = (link.textContent || "").trim().toLowerCase();
      if (["inicio", "carrinho", "pedidos", "login", "criar conta", "perfil", "sair"].includes(text)) {
        link.closest("li")?.remove();
      }
    });

    nav.querySelectorAll("ul").forEach(list => {
      if (list.classList.contains("nav-menu")) return;
      if (!list.querySelector("li")) list.remove();
    });
  }

  async function logout() {
    await fetch(`${API}/api/usuarios/logout`, { method: "POST", credentials: "include" }).catch(() => {});
    window.location.href = "/login";
  }

  function installMenu(user) {
    const nav = document.querySelector("nav");
    if (!nav) return;

    removeLegacyLinks(nav);
    nav.querySelector(".cart-icon")?.remove();

    let actions = nav.querySelector("#navActions, .nav-actions, .aura-nav-actions");
    if (!actions) {
      actions = document.createElement("div");
      nav.appendChild(actions);
    }

    actions.id = actions.id || "navActions";
    actions.className = "aura-nav-actions";

    const menu = document.createElement("div");
    menu.className = "aura-user-menu";

    const trigger = document.createElement("button");
    trigger.type = "button";
    trigger.className = "aura-user-trigger";
    trigger.dataset.auraUserTrigger = "";
    const nameSpan = document.createElement("span");
    nameSpan.className = "aura-user-name";
    nameSpan.textContent = firstName(user.nome);
    trigger.append(buildAvatar(user), nameSpan);

    const dropdown = document.createElement("div");
    dropdown.className = "aura-user-dropdown";
    dropdown.id = "auraUserDropdown";
    [
      { href: "/perfil", text: "Ver perfil" },
      { href: "/meus-pedidos.html", text: "Meus pedidos" },
      { href: "/carrinho", text: "Carrinho" },
    ].forEach(({ href, text }) => {
      const a = document.createElement("a");
      a.href = href;
      a.textContent = text;
      dropdown.appendChild(a);
    });
    const sairBtn = document.createElement("button");
    sairBtn.type = "button";
    sairBtn.dataset.auraLogout = "";
    sairBtn.textContent = "Sair";
    dropdown.appendChild(sairBtn);

    menu.append(trigger, dropdown);
    actions.replaceChildren(menu);

    actions.querySelector("[data-aura-user-trigger]")?.addEventListener("click", event => {
      event.stopPropagation();
      document.getElementById("auraUserDropdown")?.classList.toggle("aberto");
    });

    actions.querySelector("[data-aura-logout]")?.addEventListener("click", logout);
  }

  document.addEventListener("click", () => {
    document.getElementById("auraUserDropdown")?.classList.remove("aberto");
  });

  document.addEventListener("DOMContentLoaded", async () => {
    const nav = document.querySelector("nav");
    if (nav) removeLegacyLinks(nav);

    try {
      const res = await fetch(`${API}/api/usuarios/sessao`, { credentials: "include" });
      if (!res.ok) return;
      const user = await res.json();
      if (user.logado) installMenu(user);
    } catch {}
  });
})();
