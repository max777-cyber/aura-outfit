package com.aura.aura_outfit.exception;

import com.aura.aura_outfit.security.SessionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * ✅ Handler de validação Bean Validation (@Valid em DTOs).
     *    Devolve um mapa "campo → mensagem" pro front exibir erros lado a lado dos inputs.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validationErrors(MethodArgumentNotValidException e) {
        Map<String, String> erros = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        f -> f.getField(),
                        f -> f.getDefaultMessage() != null ? f.getDefaultMessage() : "Inválido",
                        (msg1, msg2) -> msg1  // se tiver múltiplos erros no mesmo campo, fica com o primeiro
                ));

        // Pega a primeira mensagem pra exibir como "erro" principal (fluxo antigo do front)
        String primeiraMsg = erros.values().stream().findFirst().orElse("Dados inválidos");

        Map<String, Object> resp = new HashMap<>();
        resp.put("erro", primeiraMsg);
        resp.put("campos", erros);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
    }

    /**
     * JSON malformado, tipos errados etc.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> badJson(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", "Requisição com formato inválido"));
    }

    @ExceptionHandler(SessionUtil.UnauthorizedException.class)
    public ResponseEntity<Map<String, String>> unauthorized(SessionUtil.UnauthorizedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(SessionUtil.ForbiddenException.class)
    public ResponseEntity<Map<String, String>> forbidden(SessionUtil.ForbiddenException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<Map<String, String>> emailJaCadastrado(EmailJaCadastradoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<Map<String, String>> credenciaisInvalidas(CredenciaisInvalidasException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> usuarioNaoEncontrado(UsuarioNaoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", e.getMessage()));
    }

    /**
     * RuntimeException genérica — log detalhado no servidor, mensagem
     * curta pro cliente. Antes vazávamos IDs internos do banco.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> runtimeGeneric(RuntimeException e) {
        log.warn("RuntimeException: {}", e.getMessage());
        String msg = e.getMessage() != null ? e.getMessage() : "Erro na requisição";
        // Só devolve mensagens "seguras" (as que a gente mesmo lança).
        // Qualquer coisa muito grande ou com dados internos vira mensagem genérica.
        if (msg.length() > 200) {
            msg = "Erro na requisição";
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> generic(Exception e) {
        log.error("Erro não tratado", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Erro interno do servidor"));
    }
}