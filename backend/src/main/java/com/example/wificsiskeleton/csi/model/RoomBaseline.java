package com.example.wificsiskeleton.csi.model;

public record RoomBaseline(
        String roomId,
        String deviceId,
        double meanEnergy,
        double standardDeviation,
        double noiseFloor,
        long calibratedAt
) {}
