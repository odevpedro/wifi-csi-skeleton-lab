package com.example.wificsiskeleton.csi.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * scenario is optional — present only in simulator payloads, never in real hardware.
 */
public record CsiSample(
        @NotNull long timestamp,
        @NotBlank String deviceId,
        @NotBlank String roomId,
        String scenario,
        double rssi,
        @NotNull double[] amplitudes,
        @NotNull double[] phases
) {}
