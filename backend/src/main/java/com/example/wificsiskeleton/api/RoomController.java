package com.example.wificsiskeleton.api;

import com.example.wificsiskeleton.calibration.CalibrationService;
import com.example.wificsiskeleton.csi.model.RoomBaseline;
import com.example.wificsiskeleton.ingestion.http.CsiPipelineService;
import com.example.wificsiskeleton.websocket.RoomStateEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final CalibrationService calibration;
    private final CsiPipelineService pipeline;

    public RoomController(CalibrationService calibration, CsiPipelineService pipeline) {
        this.calibration = calibration;
        this.pipeline = pipeline;
    }

    @PostMapping("/{roomId}/calibration/start")
    public ResponseEntity<?> startCalibration(@PathVariable String roomId) {
        calibration.startCalibration(roomId);
        return ResponseEntity.ok(Map.of("status", "calibration started", "roomId", roomId));
    }

    @PostMapping("/{roomId}/calibration/finish")
    public ResponseEntity<?> finishCalibration(@PathVariable String roomId) {
        RoomBaseline baseline = calibration.finishCalibration(roomId);
        return ResponseEntity.ok(baseline);
    }

    @GetMapping("/{roomId}/baseline")
    public ResponseEntity<?> getBaseline(@PathVariable String roomId) {
        return ResponseEntity.ok(calibration.getBaseline(roomId));
    }

    @GetMapping("/{roomId}/state")
    public ResponseEntity<?> getState(@PathVariable String roomId) {
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
