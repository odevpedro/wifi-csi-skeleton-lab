package com.example.wificsiskeleton.classification;

import com.example.wificsiskeleton.csi.model.CsiSample;
import com.example.wificsiskeleton.csi.model.CsiWindow;
import com.example.wificsiskeleton.csi.model.RoomBaseline;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MVP 0 classifier: derives PostureState from the optional scenario field in CsiSample.
 * When scenario is absent (as in real hardware), returns UNKNOWN.
 */
@Component
public class ScenarioBasedPostureClassifier implements PostureClassifier {

    @Override
    public PostureClassification classify(CsiWindow window, RoomBaseline baseline) {
        String scenario = extractScenario(window.samples());
        PostureState state = mapScenarioToPosture(scenario);
        double confidence = state == PostureState.UNKNOWN ? 0.3 : 0.9;
        return new PostureClassification(state, confidence);
    }

    private String extractScenario(List<CsiSample> samples) {
        if (samples == null || samples.isEmpty()) return null;
        for (int i = samples.size() - 1; i >= 0; i--) {
            String s = samples.get(i).scenario();
            if (s != null && !s.isBlank()) return s;
        }
        return null;
    }

    PostureState mapScenarioToPosture(String scenario) {
        if (scenario == null) return PostureState.UNKNOWN;
        return switch (scenario.trim().toLowerCase()) {
            case "person_standing"   -> PostureState.STANDING;
            case "person_walking"    -> PostureState.WALKING;
            case "person_sitting"    -> PostureState.SITTING;
            case "person_lying_down" -> PostureState.LYING_DOWN;
            case "empty_room", "strong_motion", "noise_interference" -> PostureState.UNKNOWN;
            default -> PostureState.UNKNOWN;
        };
    }
}
