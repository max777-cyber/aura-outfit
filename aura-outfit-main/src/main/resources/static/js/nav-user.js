(function () {
  const API = "http://localhost:8080";

  function firstName(name) {
    return (name || "Perfil").trim().split(/\s+/)[0] || "Perfil";
  }

  function initials(user) {
    const base = (user.nome || user.email || "A").trim();
    return base.split(/\s+/).filter(Boolean).slice(0, 2).map(part => part[0]).join("").toUpperCase() || "A";
  }

  function avatarMarkup(user) {
    if (user.fotoPerfil) {
      return `<span class="aura-user-avatar"><img src="${user.fotoPerfil}" alt=""></span>`;
    }
    return `<span class="aura-user-avatar">${initials(user)}</span>`;
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

    actions.innerHTML = `
      <div class="aura-user-menu">
        <button type="button" class="aura-user-trigger" data-aura-user-trigger>
          ${avatarMarkup(user)}
          <span class="aura-user-name">${firstName(user.nome)}</span>
        </button>
        <div class="aura-user-dropdown" id="auraUserDropdown">
          <a href="/perfil">Ver perfil</a>
          <a href="/meus-pedidos.html">Meus pedidos</a>
          <a href="/carrinho">Carrinho</a>
          <button type="button" data-aura-logout>Sair</button>
        </div>
      </div>
    `;

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
