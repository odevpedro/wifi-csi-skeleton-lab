package com.example.wificsiskeleton.api;

import com.example.wificsiskeleton.ingestion.http.CsiPipelineService;
import com.example.wificsiskeleton.ingestion.http.ManualState;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/simulation")
public class SimulationController {

    private final CsiPipelineService pipeline;

    public SimulationController(CsiPipelineService pipeline) {
        this.pipeline = pipeline;
    }

    @PostMapping("/state")
    public ResponseEntity<?> setManualState(@RequestBody ManualState state) {
        pipeline.activateManualMode(state);
        return ResponseEntity.ok(Map.of("status", "manual mode activated", "expiresInSeconds", 10));
    }

    @DeleteMapping("/state")
    public ResponseEntity<?> clearManualState() {
        pipeline.deactivateManualMode();
        return ResponseEntity.ok(Map.of("status", "manual mode deactivated"));
    }
}
