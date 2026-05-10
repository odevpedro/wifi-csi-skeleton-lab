package com.example.wificsiskeleton.api;

import com.example.wificsiskeleton.csi.model.CsiSample;
import com.example.wificsiskeleton.csi.validation.CsiSampleValidator;
import com.example.wificsiskeleton.debug.SimulatorStatusHolder;
import com.example.wificsiskeleton.ingestion.http.CsiPipelineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/csi")
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
