package com.example.wificsiskeleton;

import com.example.wificsiskeleton.csi.model.CsiSample;
import com.example.wificsiskeleton.csi.model.CsiWindow;
import com.example.wificsiskeleton.csi.model.RoomBaseline;
import com.example.wificsiskeleton.detection.DetectionResult;
import com.example.wificsiskeleton.detection.MotionDetector;
import com.example.wificsiskeleton.detection.MotionState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MotionDetectorTest {

    private final MotionDetector detector = new MotionDetector();
    private final RoomBaseline defaultBaseline = new RoomBaseline("room", "dev", 10.0, 0.3, 0.5, 0);

    private double[] amplitudes(double value, int count) {
        double[] arr = new double[count];
        for (int i = 0; i < count; i++) arr[i] = value;
        return arr;
    }

    private double[] phases(int count) {
        double[] arr = new double[count];
        return arr;
    }

    private CsiSample sample(double amp) {
        return new CsiSample(System.currentTimeMillis(), "dev", "room", null, -50,
                amplitudes(amp, 30), phases(30));
    }

    @Test
    void emptyWindowReturnsNoPresence() {
        CsiWindow window = new CsiWindow(0, 0, "dev", "room", List.of());
        DetectionResult result = detector.detect(window, defaultBaseline);
        assertEquals(MotionState.NO_PRESENCE, result.motionState());
    }

    @Test
    void stableSignalReturnsNoMotion() {
        List<CsiSample> samples = List.of(sample(10.0), sample(10.1), sample(9.9));
        CsiWindow window = new CsiWindow(0, 0, "dev", "room", samples);
        DetectionResult result = detector.detect(window, defaultBaseline);
        assertNotEquals(MotionState.STRONG_MOTION, result.motionState());
    }

    @Test
    void highEnergyReturnsStrongMotion() {
        List<CsiSample> samples = List.of(sample(30.0), sample(28.0), sample(32.0));
        CsiWindow window = new CsiWindow(0, 0, "dev", "room", samples);
        DetectionResult result = detector.detect(window, defaultBaseline);
        assertEquals(MotionState.STRONG_MOTION, result.motionState());
    }

    @Test
    void signalSummaryIsCorrect() {
        List<CsiSample> samples = List.of(sample(20.0));
        CsiWindow window = new CsiWindow(0, 0, "dev", "room", samples);
        DetectionResult result = detector.detect(window, defaultBaseline);
        assertTrue(result.signalSummary().rmsEnergy() > 0);
        assertTrue(result.signalSummary().baselineDifference() > 0);
    }

    @Test
    void rmsEnergyCalculation() {
        double[] amps = amplitudes(3.0, 4);
        double rms = detector.computeRmsEnergy(List.of(
                new CsiSample(0, "d", "r", null, 0, amps, phases(4))
        ));
        assertEquals(3.0, rms, 0.001);
    }

    @Test
    void varianceCalculation() {
        double[] amps = {10.0, 10.0, 10.0, 10.0};
        double variance = detector.computeVariance(
                List.of(new CsiSample(0, "d", "r", null, 0, amps, phases(4))),
                10.0
        );
        assertEquals(0.0, variance, 0.001);
    }
}
