package com.example.wificsiskeleton.csi.validation;

import com.example.wificsiskeleton.csi.model.CsiSample;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CsiSampleValidator {

    private static final int EXPECTED_SUBCARRIERS = 30;

    public List<String> validate(CsiSample sample) {
        List<String> errors = new ArrayList<>();

        if (sample.amplitudes() == null || sample.amplitudes().length == 0) {
            errors.add("amplitudes is required");
        }
        if (sample.phases() == null || sample.phases().length == 0) {
            errors.add("phases is required");
        }

        if (errors.isEmpty()) {
            if (sample.amplitudes().length != EXPECTED_SUBCARRIERS) {
                errors.add("amplitudes must have exactly " + EXPECTED_SUBCARRIERS + " elements");
            }
            if (sample.phases().length != EXPECTED_SUBCARRIERS) {
                errors.add("phases must have exactly " + EXPECTED_SUBCARRIERS + " elements");
            }
            if (sample.amplitudes().length != sample.phases().length) {
                errors.add("amplitudes.length must equal phases.length");
            }
            checkFinite(sample.amplitudes(), "amplitudes", errors);
            checkFinite(sample.phases(), "phases", errors);
        }

        return errors;
    }

    private void checkFinite(double[] values, String field, List<String> errors) {
        for (double v : values) {
            if (Double.isNaN(v) || Double.isInfinite(v)) {
                errors.add(field + " must not contain NaN or Infinity");
                return;
            }
        }
    }
}
