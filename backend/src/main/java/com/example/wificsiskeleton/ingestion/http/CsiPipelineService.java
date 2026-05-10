package com.example.wificsiskeleton.ingestion.http;

import com.example.wificsiskeleton.calibration.CalibrationService;
import com.example.wificsiskeleton.classification.PostureClassification;
import com.example.wificsiskeleton.classification.ScenarioBasedPostureClassifier;
import com.example.wificsiskeleton.csi.model.CsiSample;
import com.example.wificsiskeleton.csi.model.CsiWindow;
import com.example.wificsiskeleton.csi.model.RoomBaseline;
import com.example.wificsiskeleton.detection.DetectionResult;
import com.example.wificsiskeleton.detection.MotionDetector;
import com.example.wificsiskeleton.processing.window.SampleBuffer;
import com.example.wificsiskeleton.skeleton.SkeletonFrame;
import com.example.wificsiskeleton.skeleton.SkeletonService;
import com.example.wificsiskeleton.websocket.RoomStateEvent;
import com.example.wificsiskeleton.websocket.RoomStatePublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CsiPipelineService {

    @Value("${csi.window.samples-per-window:20}")
    private int samplesPerWindow;

    private final CalibrationService calibration;
    private final MotionDetector motionDetector;
    private final ScenarioBasedPostureClassifier postureClassifier;
    private final SkeletonService skeletonService;
    private final RoomStatePublisher publisher;

    private final Map<String, SampleBuffer> buffers = new ConcurrentHashMap<>();
    private final Map<String, RoomStateEvent> latestEvents = new ConcurrentHashMap<>();
    private final Map<String, List<CsiSample>> latestSamples = new ConcurrentHashMap<>();

    // manual mode state
    private volatile ManualState manualState = null;
    private volatile long manualExpiresAt = 0;

    @Value("${csi.manual-mode.expiry-seconds:10}")
    private int manualExpirySeconds;

    public CsiPipelineService(CalibrationService calibration,
                              MotionDetector motionDetector,
                              ScenarioBasedPostureClassifier postureClassifier,
                              SkeletonService skeletonService,
                              RoomStatePublisher publisher) {
        this.calibration = calibration;
        this.motionDetector = motionDetector;
        this.postureClassifier = postureClassifier;
        this.skeletonService = skeletonService;
        this.publisher = publisher;
    }

    public void ingest(CsiSample sample) {
        calibration.addCalibrationSample(sample.roomId(), sample);

        String key = sample.roomId() + ":" + sample.deviceId();
        SampleBuffer buffer = buffers.computeIfAbsent(key,
                k -> new SampleBuffer(sample.roomId(), sample.deviceId(), samplesPerWindow * 2));
        buffer.add(sample);

        latestSamples.put(sample.roomId(), buffer.getLatest(20));

        CsiWindow window = buffer.toWindow();
        RoomBaseline baseline = calibration.getBaseline(sample.roomId());

        DetectionResult detection = motionDetector.detect(window, baseline);
        PostureClassification posture = resolvePosture(window, baseline, detection);
        SkeletonFrame skeleton = skeletonService.generate(posture, detection.motionState(), sample.roomId());

        RoomStateEvent event = new RoomStateEvent(
                System.currentTimeMillis(),
                sample.roomId(),
                detection.motionState(),
                posture.postureState(),
                posture.confidence(),
                detection.signalSummary(),
                skeleton
        );

        latestEvents.put(sample.roomId(), event);
        publisher.publish(event);
    }

    private PostureClassification resolvePosture(CsiWindow window, RoomBaseline baseline, DetectionResult detection) {
        if (isManualActive()) {
            ManualState ms = manualState;
            return new PostureClassification(ms.postureState(), 1.0);
        }
        return postureClassifier.classify(window, baseline);
    }

    public void activateManualMode(ManualState state) {
        this.manualState = state;
        this.manualExpiresAt = System.currentTimeMillis() + manualExpirySeconds * 1000L;
    }

    public void deactivateManualMode() {
        this.manualState = null;
        this.manualExpiresAt = 0;
    }

    public boolean isManualActive() {
        return manualState != null && System.currentTimeMillis() < manualExpiresAt;
    }

    public RoomStateEvent getLatestEvent(String roomId) {
        return latestEvents.get(roomId);
    }

    public List<CsiSample> getLatestSamples(String roomId) {
        return latestSamples.getOrDefault(roomId, List.of());
    }

    public ManualState getManualState() {
        return isManualActive() ? manualState : null;
    }
}
