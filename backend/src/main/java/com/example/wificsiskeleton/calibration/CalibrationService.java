package com.example.wificsiskeleton.calibration;

import com.example.wificsiskeleton.csi.model.CsiSample;
import com.example.wificsiskeleton.csi.model.RoomBaseline;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CalibrationService {

    @Value("${csi.baseline.default-mean-energy:10.0}")
    private double defaultMeanEnergy;

    @Value("${csi.baseline.default-std-dev:0.3}")
    private double defaultStdDev;

    @Value("${csi.baseline.default-noise-floor:0.5}")
    private double defaultNoiseFloor;

    private final Map<String, RoomBaseline> baselines = new ConcurrentHashMap<>();
    private final Map<String, List<CsiSample>> calibrationBuffers = new ConcurrentHashMap<>();

    public RoomBaseline getBaseline(String roomId) {
        return baselines.computeIfAbsent(roomId, id ->
                new RoomBaseline(id, "default", defaultMeanEnergy, defaultStdDev, defaultNoiseFloor, System.currentTimeMillis())
        );
    }

    public void startCalibration(String roomId) {
        calibrationBuffers.put(roomId, new ArrayList<>());
    }

    public void addCalibrationSample(String roomId, CsiSample sample) {
        List<CsiSample> buf = calibrationBuffers.get(roomId);
        if (buf != null) buf.add(sample);
    }

    public RoomBaseline finishCalibration(String roomId) {
        List<CsiSample> buf = calibrationBuffers.remove(roomId);
        if (buf == null || buf.isEmpty()) {
            return getBaseline(roomId);
        }

        double[] energies = buf.stream()
                .mapToDouble(s -> computeRms(s.amplitudes()))
                .toArray();

        double mean = mean(energies);
        double std = std(energies, mean);
        double noise = std * 0.5;

        RoomBaseline baseline = new RoomBaseline(
                roomId,
                buf.get(0).deviceId(),
                mean, std, noise,
                System.currentTimeMillis()
        );
        baselines.put(roomId, baseline);
        return baseline;
    }

    private double computeRms(double[] amps) {
        double sum = 0;
        for (double a : amps) sum += a * a;
        return Math.sqrt(sum / amps.length);
    }

    private double mean(double[] values) {
        double sum = 0;
        for (double v : values) sum += v;
        return sum / values.length;
    }

    private double std(double[] values, double mean) {
        double sum = 0;
        for (double v : values) sum += (v - mean) * (v - mean);
        return Math.sqrt(sum / values.length);
    }
}
