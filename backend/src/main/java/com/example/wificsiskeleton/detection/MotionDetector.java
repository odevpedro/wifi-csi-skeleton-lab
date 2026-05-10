package com.example.wificsiskeleton.detection;

import com.example.wificsiskeleton.csi.model.CsiSample;
import com.example.wificsiskeleton.csi.model.CsiWindow;
import com.example.wificsiskeleton.csi.model.RoomBaseline;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MotionDetector {

    public DetectionResult detect(CsiWindow window, RoomBaseline baseline) {
        if (window.samples().isEmpty()) {
            return new DetectionResult(MotionState.NO_PRESENCE, new SignalSummary(0, 0, 0));
        }

        double rmsEnergy = computeRmsEnergy(window.samples());
        double variance = computeVariance(window.samples(), rmsEnergy);
        double baselineDiff = Math.abs(rmsEnergy - baseline.meanEnergy());

        SignalSummary summary = new SignalSummary(rmsEnergy, variance, baselineDiff);
        MotionState state = classifyMotion(rmsEnergy, variance, baselineDiff, baseline);

        return new DetectionResult(state, summary);
    }

    private MotionState classifyMotion(double rms, double variance, double diff, RoomBaseline baseline) {
        double noiseFloor = baseline.noiseFloor();
        double stdDev = baseline.standardDeviation();

        if (rms < baseline.meanEnergy() - stdDev * 2) {
            return MotionState.NO_PRESENCE;
        }
        if (diff < noiseFloor) {
            return MotionState.NO_MOTION;
        }
        if (diff < noiseFloor + 2.0) {
            return MotionState.PRESENCE_DETECTED;
        }
        if (diff < 3.0) {
            return MotionState.LIGHT_MOTION;
        }
        if (diff < 8.0) {
            return MotionState.MEDIUM_MOTION;
        }
        return MotionState.STRONG_MOTION;
    }

    public double computeRmsEnergy(List<CsiSample> samples) {
        if (samples.isEmpty()) return 0.0;
        double sumSquares = 0.0;
        int count = 0;
        for (CsiSample s : samples) {
            for (double amp : s.amplitudes()) {
                sumSquares += amp * amp;
                count++;
            }
        }
        return count == 0 ? 0.0 : Math.sqrt(sumSquares / count);
    }

    public double computeVariance(List<CsiSample> samples, double mean) {
        if (samples.isEmpty()) return 0.0;
        double sumSqDiff = 0.0;
        int count = 0;
        for (CsiSample s : samples) {
            for (double amp : s.amplitudes()) {
                double diff = amp - mean;
                sumSqDiff += diff * diff;
                count++;
            }
        }
        return count == 0 ? 0.0 : sumSqDiff / count;
    }
}
