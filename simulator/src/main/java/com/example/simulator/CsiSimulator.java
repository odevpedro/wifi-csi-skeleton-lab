package com.example.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class CsiSimulator {

    private static final Logger log = LoggerFactory.getLogger(CsiSimulator.class);

    @Value("${simulator.roomId}")
    private String roomId;

    @Value("${simulator.deviceId}")
    private String deviceId;

    @Value("${simulator.scenario}")
    private String scenario;

    @Value("${simulator.endpoint}")
    private String endpoint;

    private final ScenarioGenerator generator = new ScenarioGenerator();
    private final RestClient restClient = RestClient.create();
    private long sentCount = 0;

    @Scheduled(fixedRateString = "#{1000 / ${simulator.frequencyHz:20}}")
    public void sendSample() {
        ScenarioGenerator.SyntheticSample synthetic = generator.generate(scenario);

        Map<String, Object> payload = new HashMap<>();
        payload.put("timestamp", System.currentTimeMillis());
        payload.put("deviceId", deviceId);
        payload.put("roomId", roomId);
        payload.put("scenario", scenario);
        payload.put("rssi", synthetic.rssi());
        payload.put("amplitudes", synthetic.amplitudes());
        payload.put("phases", synthetic.phases());

        try {
            restClient.post()
                    .uri(endpoint)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            sentCount++;
            if (sentCount % 100 == 0) {
                log.info("Sent {} samples | scenario={} room={}", sentCount, scenario, roomId);
            }
        } catch (Exception e) {
            log.warn("Failed to send sample to backend: {}", e.getMessage());
        }
    }
}
