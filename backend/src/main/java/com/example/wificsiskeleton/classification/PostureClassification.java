package com.example.wificsiskeleton.classification;

public record PostureClassification(
        PostureState postureState,
        double confidence
) {}
