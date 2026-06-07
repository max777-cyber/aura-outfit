package com.aura.aura_outfit.config;

import com.aura.aura_outfit.model.Usuario;
import com.aura.aura_outfit.service.UsuarioService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UsuarioService usuarioService;

    public OAuth2LoginSuccessHandler(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String email = oauthUser.getAttribute("email");
        String nome = oauthUser.getAttribute("name");
        String fotoPerfil = oauthUser.getAttribute("picture");

        Usuario usuario = usuarioService.loginComGoogle(email, nome, fotoPerfil);

        HttpSession antiga = request.getSession(false);
        if (antiga != null) {
            antiga.invalidate();
        }

        HttpSession nova = request.getSession(true);
        nova.setAttribute("usuarioId", usuario.getId());
        nova.setAttribute("usuarioNome", usuario.getNome());
        nova.setAttribute("usuarioEmail", usuario.getEmail());
        nova.setAttribute("usuarioRole", usuario.getRole());

        response.sendRedirect("/");
    }
}