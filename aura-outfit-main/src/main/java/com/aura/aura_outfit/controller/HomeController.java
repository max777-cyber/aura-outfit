package com.aura.aura_outfit.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    // "redirect:" manda o navegador ir para a URL do arquivo estático.
    // É o jeito mais seguro — funciona em qualquer ambiente Spring.

    @GetMapping("/index")
    public String index() {
        return "redirect:/index.html";
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String oauth2) {
        if (oauth2 != null) {
            return "redirect:/login.html?oauth2=" + oauth2;
        }
        return "redirect:/login.html";
    }

    @GetMapping("/carrinho")
    public String carrinho() {
        return "redirect:/carrinho.html";
    }

    @GetMapping("/pedido")
    public String pedido(@RequestParam(required = false) Long pedidoId,
                         @RequestParam(required = false) String status,
                         @RequestParam(required = false) String collection_status) {
        StringBuilder url = new StringBuilder("redirect:/pedido.html");
        boolean temParam = false;
        if (pedidoId != null) {
            url.append("?pedidoId=").append(pedidoId);
            temParam = true;
        }
        if (status != null) {
            url.append(temParam ? "&" : "?").append("status=").append(status);
            temParam = true;
        }
        if (collection_status != null) {
            url.append(temParam ? "&" : "?").append("collection_status=").append(collection_status);
        }
        return url.toString();
    }

    @GetMapping("/produto")
    public String produto(@RequestParam(required = false) String id) {
        // ✅ Propaga o ?id=... pra página estática. Sem isso, o redirect
        //    perde a query string e o produto.html devolve pra home.
        if (id != null && !id.isBlank()) {
            return "redirect:/produto.html?id=" + id;
        }
        return "redirect:/produto.html";
    }

    @GetMapping("/perfil")
    public String perfil() {
        return "redirect:/perfil.html";
    }

    @GetMapping("/favicon.ico")
    @ResponseBody
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.noContent().build();
    }
}
