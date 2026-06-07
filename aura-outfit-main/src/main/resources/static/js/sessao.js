// sessao.js — verifica sessão do servidor em todas as páginas
const API = "http://localhost:8080";

async function verificarSessao() {
    try {
        const res = await fetch(`${API}/api/usuarios/sessao`, {
            credentials: "include" // ✅ envia cookie de sessão
        });
        const data = await res.json();
        return data.logado ? data : null;
    } catch {
        return null;
    }
}

async function sair() {
    await fetch(`${API}/api/usuarios/logout`, {
        method: "POST",
        credentials: "include"
    });
    window.location.href = "/login";
}