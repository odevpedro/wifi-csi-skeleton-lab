package com.example.wificsiskeleton.debug;

import com.example.wificsiskeleton.ingestion.http.CsiPipelineService;
import com.example.wificsiskeleton.ingestion.http.ManualState;
import com.example.wificsiskeleton.websocket.RoomStateEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    private final CsiPipelineService pipeline;
    private final SimulatorStatusHolder statusHolder;

    public DebugController(CsiPipelineService pipeline, SimulatorStatusHolder statusHolder) {
        this.pipeline = pipeline;
        this.statusHolder = statusHolder;
    }

    @GetMapping("/latest-samples")
    public ResponseEntity<?> latestSamples(@RequestParam(defaultValue = "bedroom") String roomId) {
        return ResponseEntity.ok(Map.of(
                "roomId", roomId,
                "samples", pipeline.getLatestSamples(roomId)
        ));
    }

    @GetMapping("/latest-event")
    public ResponseEntity<?> latestEvent(@RequestParam(defaultValue = "bedroom") String roomId) {
        RoomStateEvent event = pipeline.getLatestEvent(roomId);
        if (event == null) {
            return ResponseEntity.ok(Map.of("message", "no events yet for room " + roomId));
        }
        return ResponseEntity.ok(event);
    }

    @GetMapping("/simulator-status")
    public ResponseEntity<?> simulatorStatus() {
        return ResponseEntity.ok(statusHolder.getStatus());
    }
}
