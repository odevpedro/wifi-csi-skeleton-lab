package com.example.wificsiskeleton.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Health")
public class HealthController {

    @Operation(summary = "Verificar saude do servico", description = "Retorna status UP se o backend esta rodando. Usado pelo Docker healthcheck e por monitoramento externo.")
    @ApiResponse(responseCode = "200", description = "Servico operacional")
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
