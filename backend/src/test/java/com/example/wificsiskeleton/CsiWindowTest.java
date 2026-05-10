package com.example.wificsiskeleton;

import com.example.wificsiskeleton.csi.model.CsiSample;
import com.example.wificsiskeleton.csi.model.CsiWindow;
import com.example.wificsiskeleton.processing.window.SampleBuffer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsiWindowTest {

    private CsiSample makeSample(long ts, String scenario) {
        double[] a = new double[30];
        for (int i = 0; i < 30; i++) a[i] = 10.0;
        return new CsiSample(ts, "dev", "room", scenario, -50, a, a.clone());
    }

    @Test
    void bufferFillsCorrectly() {
        SampleBuffer buffer = new SampleBuffer("room", "dev", 20);
        for (int i = 0; i < 20; i++) {
            buffer.add(makeSample(i * 50L, "person_walking"));
        }
        CsiWindow window = buffer.toWindow();
        assertEquals(20, window.samples().size());
        assertEquals("room", window.roomId());
    }

    @Test
    void bufferEvictsOldestWhenFull() {
        SampleBuffer buffer = new SampleBuffer("room", "dev", 5);
        for (int i = 0; i < 10; i++) {
            buffer.add(makeSample(i * 50L, null));
        }
        CsiWindow window = buffer.toWindow();
        assertEquals(5, window.samples().size());
    }

    @Test
    void emptyBufferProducesEmptyWindow() {
        SampleBuffer buffer = new SampleBuffer("room", "dev", 20);
        CsiWindow window = buffer.toWindow();
        assertTrue(window.samples().isEmpty());
    }

    @Test
    void windowTimestampsAreCorrect() {
        SampleBuffer buffer = new SampleBuffer("room", "dev", 20);
        buffer.add(makeSample(1000L, null));
        buffer.add(makeSample(2000L, null));
        buffer.add(makeSample(3000L, null));
        CsiWindow window = buffer.toWindow();
        assertEquals(1000L, window.startTimestamp());
        assertEquals(3000L, window.endTimestamp());
    }

    @Test
    void getLatestReturnsCorrectCount() {
        SampleBuffer buffer = new SampleBuffer("room", "dev", 20);
        for (int i = 0; i < 15; i++) {
            buffer.add(makeSample(i * 50L, null));
        }
        List<CsiSample> latest = buffer.getLatest(5);
        assertEquals(5, latest.size());
    }
}
