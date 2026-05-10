package com.example.wificsiskeleton.detection;

public record SignalSummary(
        double rmsEnergy,
        double variance,
        double baselineDifference
) {}
