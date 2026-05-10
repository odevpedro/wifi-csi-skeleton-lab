package com.example.wificsiskeleton.api;

import com.example.wificsiskeleton.csi.model.CsiSample;
import com.example.wificsiskeleton.csi.validation.CsiSampleValidator;
import com.example.wificsiskeleton.debug.SimulatorStatusHolder;
import com.example.wificsiskeleton.ingestion.http.CsiPipelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/csi")
@Tag(name = "CSI")
public class CsiController {

    private final CsiSampleValidator validator;
    private final CsiPipelineService pipeline;
    private final SimulatorStatusHolder statusHolder;

    public CsiController(CsiSampleValidator validator,
                         CsiPipelineService pipeline,
                         SimulatorStatusHolder statusHolder) {
        this.validator = validator;
        this.pipeline = pipeline;
        this.statusHolder = statusHolder;
    }

    @Operation(
            summary = "Ingerir amostra CSI",
            description = """
                    Recebe uma amostra CSI e a processa no pipeline completo:
                    janela temporal -> deteccao de MotionState -> classificacao de PostureState
                    -> geracao de SkeletonFrame -> publicacao de RoomStateEvent via WebSocket.

                    O campo `scenario` e opcional e nunca causa rejeicao quando ausente.
                    Em hardware real (ESP32), omitir o campo `scenario`.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Amostra aceita e processada"),
            @ApiResponse(responseCode = "400", description = "Payload invalido — lista de erros retornada no corpo")
    })
    @PostMapping("/samples")
    public ResponseEntity<?> ingest(@RequestBody CsiSample sample) {
        List<String> errors = validator.validate(sample);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }
        pipeline.ingest(sample);
        statusHolder.update(sample.scenario(), sample.deviceId(), sample.roomId(), 20);
        return ResponseEntity.ok(Map.of("status", "accepted"));
    }
}
