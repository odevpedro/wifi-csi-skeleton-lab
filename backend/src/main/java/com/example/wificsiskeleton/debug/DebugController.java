package com.example.wificsiskeleton.debug;

import com.example.wificsiskeleton.ingestion.http.CsiPipelineService;
import com.example.wificsiskeleton.websocket.RoomStateEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@Tag(name = "Debug")
public class DebugController {

    private final CsiPipelineService pipeline;
    private final SimulatorStatusHolder statusHolder;

    public DebugController(CsiPipelineService pipeline, SimulatorStatusHolder statusHolder) {
        this.pipeline = pipeline;
        this.statusHolder = statusHolder;
    }

    @Operation(summary = "Ultimas amostras brutas", description = "Retorna as ultimas amostras CSI armazenadas no SampleBuffer para a sala informada. Util para verificar se o simulador ou hardware esta enviando dados.")
    @ApiResponse(responseCode = "200", description = "Lista de amostras brutas")
    @GetMapping("/latest-samples")
    public ResponseEntity<?> latestSamples(@Parameter(description = "ID da sala") @RequestParam(defaultValue = "bedroom") String roomId) {
        return ResponseEntity.ok(Map.of(
                "roomId", roomId,
                "samples", pipeline.getLatestSamples(roomId)
        ));
    }

    @Operation(summary = "Ultimo evento processado", description = "Retorna o RoomStateEvent mais recente emitido pelo pipeline para a sala informada, incluindo MotionState, PostureState, confidence e SkeletonFrame.")
    @ApiResponse(responseCode = "200", description = "Ultimo RoomStateEvent ou mensagem indicando ausencia de dados")
    @GetMapping("/latest-event")
    public ResponseEntity<?> latestEvent(@Parameter(description = "ID da sala") @RequestParam(defaultValue = "bedroom") String roomId) {
        RoomStateEvent event = pipeline.getLatestEvent(roomId);
        if (event == null) {
            return ResponseEntity.ok(Map.of("message", "no events yet for room " + roomId));
        }
        return ResponseEntity.ok(event);
    }

    @Operation(summary = "Status do simulador", description = "Retorna o cenario ativo, deviceId, roomId e timestamp da ultima amostra enviada pelo simulador autonomo Java.")
    @ApiResponse(responseCode = "200", description = "Status atual do simulador")
    @GetMapping("/simulator-status")
    public ResponseEntity<?> simulatorStatus() {
        return ResponseEntity.ok(statusHolder.getStatus());
    }
}
