package com.example.wificsiskeleton;

import com.example.wificsiskeleton.csi.model.CsiSample;
import com.example.wificsiskeleton.csi.validation.CsiSampleValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsiSampleValidatorTest {

    private final CsiSampleValidator validator = new CsiSampleValidator();

    private double[] amps(int size) {
        double[] a = new double[size];
        for (int i = 0; i < size; i++) a[i] = 10.0 + i * 0.1;
        return a;
    }

    private CsiSample valid() {
        return new CsiSample(System.currentTimeMillis(), "dev", "room", "person_walking", -50, amps(30), amps(30));
    }

    @Test
    void validSamplePassesWithScenario() {
        assertTrue(validator.validate(valid()).isEmpty());
    }

    @Test
    void validSamplePassesWithoutScenario() {
        CsiSample s = new CsiSample(System.currentTimeMillis(), "dev", "room", null, -50, amps(30), amps(30));
        assertTrue(validator.validate(s).isEmpty());
    }

    @Test
    void rejectsAmplitudesLengthNot30() {
        CsiSample s = new CsiSample(System.currentTimeMillis(), "dev", "room", null, -50, amps(10), amps(30));
        List<String> errors = validator.validate(s);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("amplitudes")));
    }

    @Test
    void rejectsPhasesLengthNot30() {
        CsiSample s = new CsiSample(System.currentTimeMillis(), "dev", "room", null, -50, amps(30), amps(5));
        List<String> errors = validator.validate(s);
        assertFalse(errors.isEmpty());
    }

    @Test
    void rejectsNaNInAmplitudes() {
        double[] a = amps(30);
        a[5] = Double.NaN;
        CsiSample s = new CsiSample(System.currentTimeMillis(), "dev", "room", null, -50, a, amps(30));
        List<String> errors = validator.validate(s);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("NaN")));
    }

    @Test
    void rejectsInfinityInPhases() {
        double[] p = amps(30);
        p[0] = Double.POSITIVE_INFINITY;
        CsiSample s = new CsiSample(System.currentTimeMillis(), "dev", "room", null, -50, amps(30), p);
        List<String> errors = validator.validate(s);
        assertFalse(errors.isEmpty());
    }

    @Test
    void rejectsNullAmplitudes() {
        CsiSample s = new CsiSample(System.currentTimeMillis(), "dev", "room", null, -50, null, amps(30));
        List<String> errors = validator.validate(s);
        assertFalse(errors.isEmpty());
    }
}
