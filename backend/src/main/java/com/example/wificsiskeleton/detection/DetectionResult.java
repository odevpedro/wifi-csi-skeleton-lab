package com.example.wificsiskeleton.detection;

public record DetectionResult(
        MotionState motionState,
        SignalSummary signalSummary
) {}
