package com.example.wificsiskeleton.debug;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SimulatorStatusHolder {

    private final Map<String, Object> status = new ConcurrentHashMap<>();

    public SimulatorStatusHolder() {
        status.put("scenario", "unknown");
        status.put("frequencyHz", 0);
        status.put("deviceId", "unknown");
        status.put("roomId", "unknown");
        status.put("lastSampleAt", 0);
        status.put("totalSamplesReceived", 0L);
    }

    public void update(String scenario, String deviceId, String roomId, int frequencyHz) {
        status.put("scenario", scenario != null ? scenario : "none");
        status.put("deviceId", deviceId);
        status.put("roomId", roomId);
        status.put("frequencyHz", frequencyHz);
        status.put("lastSampleAt", System.currentTimeMillis());
        status.merge("totalSamplesReceived", 1L, (a, b) -> (Long) a + (Long) b);
    }

    public Map<String, Object> getStatus() {
        return Map.copyOf(status);
    }
}
