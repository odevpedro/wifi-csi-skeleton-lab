package com.example.simulator;

import java.util.Random;

/**
 * Generates synthetic CSI amplitudes and phases for each scenario.
 * Values are synthetic and do NOT reflect real hardware measurements.
 */
class ScenarioGenerator {

    private final Random rng = new Random();
    private long tick = 0;

    SyntheticSample generate(String scenario) {
        tick++;
        return switch (scenario.toLowerCase()) {
            case "empty_room"         -> emptyRoom();
            case "person_standing"    -> personStanding();
            case "person_walking"     -> personWalking();
            case "person_sitting"     -> personSitting();
            case "person_lying_down"  -> personLyingDown();
            case "strong_motion"      -> strongMotion();
            case "noise_interference" -> noiseInterference();
            default -> emptyRoom();
        };
    }

    private SyntheticSample emptyRoom() {
        return new SyntheticSample(
                amplitudes(10.0, 0.3),
                phases(0.0, 0.02),
                -65.0 + rng.nextGaussian() * 1.0
        );
    }

    private SyntheticSample personStanding() {
        return new SyntheticSample(
                amplitudes(10.0, 0.8),
                phases(0.0, 0.05),
                -60.0 + rng.nextGaussian() * 1.5
        );
    }

    private SyntheticSample personWalking() {
        double phase = (tick % 16) * (Math.PI / 8.0);
        double cyclic = Math.sin(phase) * 2.5;
        return new SyntheticSample(
                amplitudes(10.0 + cyclic, 1.0),
                phases(cyclic * 0.05, 0.08),
                -55.0 + rng.nextGaussian() * 2.0
        );
    }

    private SyntheticSample personSitting() {
        // brief transition spike for first ~10 ticks, then stabilize
        double base = tick < 10 ? 10.0 + rng.nextGaussian() * 2.0 : 10.0;
        double noise = tick < 10 ? 1.5 : 0.5;
        return new SyntheticSample(
                amplitudes(base, noise),
                phases(0.0, 0.04),
                -61.0 + rng.nextGaussian() * 1.0
        );
    }

    private SyntheticSample personLyingDown() {
        double base = tick < 20 ? 10.0 + rng.nextGaussian() * 3.0 : 10.0;
        double noise = tick < 20 ? 2.0 : 0.2;
        return new SyntheticSample(
                amplitudes(base, noise),
                phases(0.0, 0.01),
                -63.0 + rng.nextGaussian() * 0.5
        );
    }

    private SyntheticSample strongMotion() {
        return new SyntheticSample(
                amplitudes(25.0, 6.0),
                phases(0.0, 0.4),
                -45.0 + rng.nextGaussian() * 5.0
        );
    }

    private SyntheticSample noiseInterference() {
        double[] amps = new double[30];
        double[] phases = new double[30];
        for (int i = 0; i < 30; i++) {
            amps[i] = Math.abs(10.0 + rng.nextGaussian() * 5.0);
            phases[i] = rng.nextGaussian() * 0.5;
        }
        return new SyntheticSample(amps, phases, -50.0 + rng.nextGaussian() * 8.0);
    }

    private double[] amplitudes(double base, double noise) {
        double[] a = new double[30];
        for (int i = 0; i < 30; i++) {
            a[i] = Math.max(0.1, base + rng.nextGaussian() * noise);
        }
        return a;
    }

    private double[] phases(double base, double noise) {
        double[] p = new double[30];
        for (int i = 0; i < 30; i++) {
            p[i] = base + rng.nextGaussian() * noise;
        }
        return p;
    }

    record SyntheticSample(double[] amplitudes, double[] phases, double rssi) {}
}
