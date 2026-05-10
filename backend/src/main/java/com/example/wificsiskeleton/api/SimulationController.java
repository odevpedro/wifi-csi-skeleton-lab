package com.example.wificsiskeleton.api;

import com.example.wificsiskeleton.ingestion.http.CsiPipelineService;
import com.example.wificsiskeleton.ingestion.http.ManualState;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/simulation")
@Tag(name = "Simulation")
public class SimulationController {

    private final CsiPipelineService pipeline;

    public SimulationController(CsiPipelineService pipeline) {
        this.pipeline = pipeline;
    }

    @Operation(summary = "Ativar modo manual", description = "Forca o pipeline a emitir um estado especifico (MotionState + PostureState) pelos proximos 10 segundos, ignorando amostras CSI reais. Util para testes sem hardware.")
    @ApiResponse(responseCode = "200", description = "Modo manual ativado")
    @PostMapping("/state")
    public ResponseEntity<?> setManualState(@RequestBody ManualState state) {
        pipeline.activateManualMode(state);
        return ResponseEntity.ok(Map.of("status", "manual mode activated", "expiresInSeconds", 10));
    }

    @Operation(summary = "Desativar modo manual", description = "Cancela o modo manual antes do expiry de 10 segundos e retorna o pipeline ao processamento normal de amostras CSI.")
    @ApiResponse(responseCode = "200", description = "Modo manual desativado")
    @DeleteMapping("/state")
    public ResponseEntity<?> clearManualState() {
        pipeline.deactivateManualMode();
        return ResponseEntity.ok(Map.of("status", "manual mode deactivated"));
    }
}
