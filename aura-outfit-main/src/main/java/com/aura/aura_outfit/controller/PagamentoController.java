package com.aura.aura_outfit.controller;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import com.aura.aura_outfit.model.Pedido;
import com.aura.aura_outfit.model.ItemPedido;
import com.aura.aura_outfit.model.Usuario;
import com.aura.aura_outfit.security.SessionUtil;
import com.aura.aura_outfit.service.PedidoService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pagamento")
public class PagamentoController {

    private static final Logger log = LoggerFactory.getLogger(PagamentoController.class);

    private final PedidoService pedidoService;
    private final SessionUtil sessionUtil;

    @Value("${mercadopago.access-token:}")
    private String accessToken;

    @Value("${app.frontend.url:http://localhost:8080}")
    private String frontendUrl;

    public PagamentoController(PedidoService pedidoService, SessionUtil sessionUtil) {
        this.pedidoService = pedidoService;
        this.sessionUtil = sessionUtil;
    }

    @PostConstruct
    public void init() {
        if (accessToken == null || accessToken.isBlank()) {
            log.warn("⚠️  mercadopago.access-token não configurado no application.properties");
            log.warn("    A tela de pagamento NÃO vai funcionar até configurar.");
            return;
        }
        MercadoPagoConfig.setAccessToken(accessToken);
        boolean isTeste = accessToken.startsWith("TEST-");
        log.info("✅ Mercado Pago configurado (modo={})", isTeste ? "SANDBOX/TESTE" : "PRODUÇÃO");
    }

    @PostMapping("/criar/{pedidoId}")
    public ResponseEntity<Map<String, String>> criarPagamento(
            @PathVariable Long pedidoId,
            HttpSession session) {

        if (accessToken == null || accessToken.isBlank()) {
            log.error("Tentativa de pagamento sem access-token configurado");
            return ResponseEntity.status(503).body(Map.of(
                    "erro", "Pagamento indisponível: sistema não configurado"
            ));
        }

        try {
            Pedido pedido = pedidoService.buscarPorId(pedidoId);
            sessionUtil.exigirDonoOuAdmin(session, pedido.getUsuario().getId());

            if (pedido.getItens() == null || pedido.getItens().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Pedido sem itens"));
            }

            List<PreferenceItemRequest> itens = new ArrayList<>();
            for (ItemPedido item : pedido.getItens()) {
                PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                        .id(String.valueOf(item.getProduto().getId()))
                        .title(item.getProduto().getNome())
                        .description(item.getProduto().getMarca() != null
                                ? item.getProduto().getMarca()
                                : "Produto Aura Outfit")
                        .quantity(item.getQuantidade())
                        .unitPrice(BigDecimal.valueOf(item.getPrecoUnitario()))
                        .currencyId("BRL")
                        .build();
                itens.add(itemRequest);
            }

            Usuario usuario = pedido.getUsuario();
            String[] nomeCompleto = (usuario.getNome() != null ? usuario.getNome() : "Cliente Aura").split(" ", 2);
            String primeiroNome = nomeCompleto[0];
            String sobrenome = nomeCompleto.length > 1 ? nomeCompleto[1] : "Teste";

            PreferencePayerRequest.PreferencePayerRequestBuilder payerBuilder = PreferencePayerRequest.builder()
                    .name(primeiroNome)
                    .surname(sobrenome)
                    .email(usuario.getEmail());

            if (usuario.getDocumento() != null && !usuario.getDocumento().isBlank()) {
                String cpfLimpo = usuario.getDocumento().replaceAll("\\D", "");
                if (cpfLimpo.length() == 11) {
                    payerBuilder.identification(
                            IdentificationRequest.builder()
                                    .type("CPF")
                                    .number(cpfLimpo)
                                    .build()
                    );
                }
            }

            String baseUrl = frontendUrl.endsWith("/")
                    ? frontendUrl.substring(0, frontendUrl.length() - 1)
                    : frontendUrl;

            // ✅ Passa o pedidoId na query string pra página de confirmação saber
            //    qual pedido foi pago, mesmo que a sessionStorage tenha sido limpa
            //    (o MP abre em outra aba/contexto em alguns fluxos).
            String successUrl = baseUrl + "/pedido.html?pedidoId=" + pedido.getId() + "&status=aprovado";
            String pendingUrl = baseUrl + "/pedido.html?pedidoId=" + pedido.getId() + "&status=pendente";
            String failureUrl = baseUrl + "/carrinho.html?pedidoId=" + pedido.getId() + "&status=falhou";

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(successUrl)
                    .pending(pendingUrl)
                    .failure(failureUrl)
                    .build();

            PreferenceRequest.PreferenceRequestBuilder prefBuilder = PreferenceRequest.builder()
                    .items(itens)
                    .payer(payerBuilder.build())
                    .externalReference(String.valueOf(pedido.getId()))
                    .backUrls(backUrls);

            // ✅ auto_return só funciona com HTTPS público.
            //    Em localhost o MP exibe um botão "Voltar ao site" manual
            //    que aponta para success_url.
            if (baseUrl.startsWith("https://")) {
                prefBuilder.autoReturn("approved");
                prefBuilder.notificationUrl(baseUrl + "/api/pagamento/webhook");
            }

            PreferenceRequest preferenceRequest = prefBuilder.build();
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            log.info("Preferência MP criada: pedidoId={} preferenceId={}", pedido.getId(), preference.getId());

            // Token TEST- usa sandbox; APP_USR- usa produção
            boolean isSandbox = accessToken.startsWith("TEST-");
            String checkoutUrl = isSandbox
                    ? preference.getSandboxInitPoint()
                    : preference.getInitPoint();
            if (checkoutUrl == null || checkoutUrl.isBlank()) {
                checkoutUrl = isSandbox ? preference.getInitPoint() : preference.getSandboxInitPoint();
            }

            if (checkoutUrl == null || checkoutUrl.isBlank()) {
                log.error("MP criou preferência mas não retornou checkout URL. preferenceId={}", preference.getId());
                return ResponseEntity.status(500).body(Map.of(
                        "erro", "Não foi possível gerar o link de pagamento"
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "checkoutUrl", checkoutUrl,
                    "preferenceId", preference.getId()
            ));

        } catch (MPApiException e) {
            String detalheMP = e.getApiResponse() != null
                    ? e.getApiResponse().getContent()
                    : e.getMessage();
            log.error("❌ Erro na API do Mercado Pago (status={}): {}",
                    e.getApiResponse() != null ? e.getApiResponse().getStatusCode() : "?",
                    detalheMP);
            return ResponseEntity.status(502).body(Map.of(
                    "erro", "Falha ao se comunicar com o provedor de pagamento"
            ));
        } catch (SessionUtil.UnauthorizedException | SessionUtil.ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Erro ao criar pagamento", e);
            return ResponseEntity.status(500).body(Map.of("erro", "Erro ao processar pagamento"));
        }
    }

    /**
     * ✅ Webhook do Mercado Pago — agora FUNCIONAL.
     *
     * Quando o MP processa um pagamento (aprovado, rejeitado, etc.) ele chama
     * este endpoint com o ID do pagamento. A gente busca o pagamento no MP,
     * descobre de qual pedido é (via external_reference) e atualiza o status.
     *
     * É aqui que o estoque é baixado — só quando o MP confirma o pagamento.
     *
     * ⚠️ IMPORTANTE: em localhost (HTTP) o MP NÃO chama esse webhook automaticamente.
     *    Pra testar em dev, você pode:
     *    1. Usar ngrok pra expor o localhost via HTTPS
     *    2. Chamar esse endpoint manualmente pra simular (veja endpoint /simular-pagamento)
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody Map<String, Object> payload) {
        log.info("🔔 Webhook MP recebido");

        try {
            String type = (String) payload.get("type");

            if (!"payment".equalsIgnoreCase(type)) {
                log.info("   Webhook ignorado (type={})", type);
                return ResponseEntity.ok().build();
            }

            // Extrai o ID do pagamento do payload
            Object dataObj = payload.get("data");
            if (!(dataObj instanceof Map)) {
                log.warn("   Webhook sem campo 'data' válido");
                return ResponseEntity.ok().build();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) dataObj;
            Object paymentIdObj = data.get("id");
            if (paymentIdObj == null) {
                log.warn("   Webhook sem payment id");
                return ResponseEntity.ok().build();
            }

            Long paymentId = Long.parseLong(paymentIdObj.toString());

            // Busca o pagamento no MP pra descobrir status e external_reference
            PaymentClient paymentClient = new PaymentClient();
            Payment payment = paymentClient.get(paymentId);

            String status = payment.getStatus(); // approved, rejected, pending, etc.
            String externalRef = payment.getExternalReference();

            log.info("   Pagamento {} | status={} | pedidoId={}", paymentId, status, externalRef);

            if (externalRef == null || externalRef.isBlank()) {
                log.warn("   Pagamento sem external_reference — não sei qual pedido atualizar");
                return ResponseEntity.ok().build();
            }

            Long pedidoId = Long.parseLong(externalRef);

            // Atualiza o pedido conforme o status do pagamento
            switch (status.toLowerCase()) {
                case "approved" -> pedidoService.confirmarPagamento(pedidoId);
                case "rejected", "cancelled" -> pedidoService.cancelarPedido(pedidoId);
                case "pending", "in_process" -> log.info("   Pedido #{} ainda pendente — nada a fazer", pedidoId);
                default -> log.warn("   Status desconhecido: {}", status);
            }

        } catch (Exception e) {
            log.error("❌ Erro processando webhook", e);
            // Retorna 200 mesmo com erro pro MP não ficar reenviando infinitamente
        }

        return ResponseEntity.ok().build();
    }

    /**
     * ✅ Endpoint de simulação pra ambiente de desenvolvimento (HTTP/localhost).
     *
     * Como o MP não consegue chamar o webhook em localhost (precisa HTTPS público),
     * esse endpoint permite simular manualmente a confirmação de pagamento pra testar
     * se o fluxo de atualização de status + baixa de estoque funciona.
     *
     * ⚠️ Só use em DEV! Em produção deve ser removido ou protegido com admin.
     *
     * Uso (no navegador, Postman, ou via curl):
     *   POST http://localhost:8080/api/pagamento/simular-pagamento/{pedidoId}
     */
    @PostMapping("/simular-pagamento/{pedidoId}")
    public ResponseEntity<Map<String, Object>> simularPagamento(
            @PathVariable Long pedidoId,
            HttpSession session) {

        if (accessToken == null || !accessToken.startsWith("TEST-")) {
            return ResponseEntity.status(403).body(Map.of(
                    "erro", "Simulação disponível apenas em ambiente de teste"
            ));
        }

        Pedido pedido = pedidoService.buscarPorId(pedidoId);
        sessionUtil.exigirDonoOuAdmin(session, pedido.getUsuario().getId());

        log.info("🧪 Simulando pagamento aprovado do pedido #{}", pedidoId);
        Pedido atualizado = pedidoService.confirmarPagamento(pedidoId);

        return ResponseEntity.ok(Map.of(
                "pedidoId", atualizado.getId(),
                "status", atualizado.getStatus(),
                "mensagem", "Pagamento simulado com sucesso"
        ));
    }
}