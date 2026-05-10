package com.example.wificsiskeleton.csi.model;

import java.util.List;

public record CsiWindow(
        long startTimestamp,
        long endTimestamp,
        String deviceId,
        String roomId,
        List<CsiSample> samples
) {}
