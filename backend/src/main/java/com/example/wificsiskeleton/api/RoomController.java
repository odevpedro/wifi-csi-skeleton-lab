package com.example.wificsiskeleton.api;

import com.example.wificsiskeleton.calibration.CalibrationService;
import com.example.wificsiskeleton.csi.model.RoomBaseline;
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
@RequestMapping("/api/rooms")
@Tag(name = "Rooms")
public class RoomController {

    private final CalibrationService calibration;
    private final CsiPipelineService pipeline;

    public RoomController(CalibrationService calibration, CsiPipelineService pipeline) {
        this.calibration = calibration;
        this.pipeline = pipeline;
    }

    @Operation(summary = "Iniciar calibracao", description = "Inicia a coleta de amostras para calibrar o baseline da sala. Deixe o ambiente vazio durante a calibracao.")
    @ApiResponse(responseCode = "200", description = "Calibracao iniciada")
    @PostMapping("/{roomId}/calibration/start")
    public ResponseEntity<?> startCalibration(@Parameter(description = "ID da sala") @PathVariable String roomId) {
        calibration.startCalibration(roomId);
        return ResponseEntity.ok(Map.of("status", "calibration started", "roomId", roomId));
    }

    @Operation(summary = "Finalizar calibracao", description = "Calcula o baseline a partir das amostras coletadas desde o start e o aplica ao detector de movimento.")
    @ApiResponse(responseCode = "200", description = "Baseline calculado e aplicado")
    @PostMapping("/{roomId}/calibration/finish")
    public ResponseEntity<?> finishCalibration(@Parameter(description = "ID da sala") @PathVariable String roomId) {
        RoomBaseline baseline = calibration.finishCalibration(roomId);
        return ResponseEntity.ok(baseline);
    }

    @Operation(summary = "Consultar baseline atual", description = "Retorna o baseline em uso para a sala. Se nao calibrado, retorna o baseline padrao (meanEnergy=10.0, stdDev=0.3, noiseFloor=0.5).")
    @GetMapping("/{roomId}/baseline")
    public ResponseEntity<?> getBaseline(@Parameter(description = "ID da sala") @PathVariable String roomId) {
        return ResponseEntity.ok(calibration.getBaseline(roomId));
    }

    @Operation(summary = "Estado atual da sala", description = "Retorna o ultimo RoomStateEvent processado para a sala, incluindo MotionState, PostureState e confidence.")
    @GetMapping("/{roomId}/state")
    public ResponseEntity<?> getState(@Parameter(description = "ID da sala") @PathVariable String roomId) {
        RoomStateEvent event = pipeline.getLatestEvent(roomId);
        if (event == null) {
            return ResponseEntity.ok(Map.of(
                    "roomId", roomId,
                    "motionState", "NO_PRESENCE",
                    "postureState", "UNKNOWN",
                    "confidence", 0.0,
                    "lastUpdate", 0
            ));
        }
        return ResponseEntity.ok(Map.of(
                "roomId", event.roomId(),
                "motionState", event.motionState(),
                "postureState", event.postureState(),
                "confidence", event.confidence(),
                "lastUpdate", event.timestamp()
        ));
    }
}
