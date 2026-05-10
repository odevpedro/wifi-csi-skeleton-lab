package com.example.wificsiskeleton;

import com.example.wificsiskeleton.classification.PostureState;
import com.example.wificsiskeleton.classification.ScenarioBasedPostureClassifier;
import com.example.wificsiskeleton.csi.model.CsiSample;
import com.example.wificsiskeleton.csi.model.CsiWindow;
import com.example.wificsiskeleton.csi.model.RoomBaseline;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScenarioBasedPostureClassifierTest {

    private final ScenarioBasedPostureClassifier classifier = new ScenarioBasedPostureClassifier();
    private final RoomBaseline baseline = new RoomBaseline("room", "dev", 10.0, 0.3, 0.5, 0);

    private CsiWindow windowWithScenario(String scenario) {
        double[] amps = new double[30];
        double[] phases = new double[30];
        CsiSample sample = new CsiSample(0, "dev", "room", scenario, -50, amps, phases);
        return new CsiWindow(0, 0, "dev", "room", List.of(sample));
    }

    @ParameterizedTest
    @CsvSource({
            "person_standing, STANDING",
            "person_walking, WALKING",
            "person_sitting, SITTING",
            "person_lying_down, LYING_DOWN",
            "empty_room, UNKNOWN",
            "strong_motion, UNKNOWN",
            "noise_interference, UNKNOWN"
    })
    void mapsScenarioToCorrectPosture(String scenario, String expected) {
        var result = classifier.classify(windowWithScenario(scenario), baseline);
        assertEquals(PostureState.valueOf(expected), result.postureState());
    }

    @Test
    void nullScenarioReturnsUnknown() {
        var result = classifier.classify(windowWithScenario(null), baseline);
        assertEquals(PostureState.UNKNOWN, result.postureState());
    }

    @Test
    void emptyWindowReturnsUnknown() {
        CsiWindow window = new CsiWindow(0, 0, "dev", "room", List.of());
        var result = classifier.classify(window, baseline);
        assertEquals(PostureState.UNKNOWN, result.postureState());
    }

    @Test
    void unknownScenarioValueReturnsUnknown() {
        var result = classifier.classify(windowWithScenario("something_else"), baseline);
        assertEquals(PostureState.UNKNOWN, result.postureState());
    }

    @Test
    void scenarioAbsentConfidenceIsLow() {
        var result = classifier.classify(windowWithScenario(null), baseline);
        assertTrue(result.confidence() < 0.5);
    }

    @Test
    void scenarioPresentConfidenceIsHigh() {
        var result = classifier.classify(windowWithScenario("person_standing"), baseline);
        assertTrue(result.confidence() >= 0.8);
    }
}
