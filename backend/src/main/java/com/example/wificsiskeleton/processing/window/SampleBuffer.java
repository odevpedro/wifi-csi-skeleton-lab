package com.example.wificsiskeleton.processing.window;

import com.example.wificsiskeleton.csi.model.CsiSample;
import com.example.wificsiskeleton.csi.model.CsiWindow;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Circular buffer of CsiSamples for a single roomId/deviceId pair.
 */
public class SampleBuffer {

    private final int maxSize;
    private final String roomId;
    private final String deviceId;
    private final Queue<CsiSample> buffer;

    public SampleBuffer(String roomId, String deviceId, int maxSize) {
        this.roomId = roomId;
        this.deviceId = deviceId;
        this.maxSize = maxSize;
        this.buffer = new LinkedList<>();
    }

    public synchronized void add(CsiSample sample) {
        if (buffer.size() >= maxSize) {
            buffer.poll();
        }
        buffer.add(sample);
    }

    public synchronized CsiWindow toWindow() {
        List<CsiSample> samples = new ArrayList<>(buffer);
        long start = samples.isEmpty() ? 0 : samples.get(0).timestamp();
        long end = samples.isEmpty() ? 0 : samples.get(samples.size() - 1).timestamp();
        return new CsiWindow(start, end, deviceId, roomId, samples);
    }

    public synchronized List<CsiSample> getLatest(int n) {
        List<CsiSample> all = new ArrayList<>(buffer);
        int from = Math.max(0, all.size() - n);
        return all.subList(from, all.size());
    }
}
