package com.example.wificsiskeleton.skeleton;

public record BodyKeypoint(
        String name,
        double x,
        double y,
        double confidence
) {}
