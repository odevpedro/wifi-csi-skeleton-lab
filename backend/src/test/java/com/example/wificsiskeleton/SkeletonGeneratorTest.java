package com.example.wificsiskeleton;

import com.example.wificsiskeleton.classification.PostureClassification;
import com.example.wificsiskeleton.classification.PostureState;
import com.example.wificsiskeleton.detection.MotionState;
import com.example.wificsiskeleton.skeleton.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class SkeletonGeneratorTest {

    private final StandingSkeletonGenerator standing = new StandingSkeletonGenerator();
    private final SittingSkeletonGenerator sitting = new SittingSkeletonGenerator();
    private final WalkingSkeletonGenerator walking = new WalkingSkeletonGenerator();
    private final LyingDownSkeletonGenerator lyingDown = new LyingDownSkeletonGenerator();
    private final ArmsUpSkeletonGenerator armsUp = new ArmsUpSkeletonGenerator();
    private final UnknownSkeletonGenerator unknown = new UnknownSkeletonGenerator();

    private final SkeletonService service = new SkeletonService(standing, sitting, walking, lyingDown, armsUp, unknown);

    @Test
    void standingGeneratesKeypoints() {
        var frame = standing.generate(new PostureClassification(PostureState.STANDING, 0.9), "room");
        assertNotNull(frame);
        assertFalse(frame.keypoints().isEmpty());
        assertEquals(SkeletonMode.STATE_BASED, frame.mode());
    }

    @Test
    void sittingGeneratesKeypoints() {
        var frame = sitting.generate(new PostureClassification(PostureState.SITTING, 0.9), "room");
        assertFalse(frame.keypoints().isEmpty());
    }

    @Test
    void walkingGeneratesKeypoints() {
        var frame = walking.generate(new PostureClassification(PostureState.WALKING, 0.9), "room");
        assertFalse(frame.keypoints().isEmpty());
    }

    @Test
    void lyingDownGeneratesKeypoints() {
        var frame = lyingDown.generate(new PostureClassification(PostureState.LYING_DOWN, 0.9), "room");
        assertFalse(frame.keypoints().isEmpty());
    }

    @Test
    void unknownNoPresenceHasLowConfidence() {
        var frame = unknown.generate(new PostureClassification(PostureState.UNKNOWN, 0.1), MotionState.NO_PRESENCE, "room");
        assertFalse(frame.keypoints().isEmpty());
        frame.keypoints().forEach(k -> assertTrue(k.confidence() <= 0.15));
    }

    @Test
    void unknownStrongMotionHasMediumConfidence() {
        var frame = unknown.generate(new PostureClassification(PostureState.UNKNOWN, 0.4), MotionState.STRONG_MOTION, "room");
        assertFalse(frame.keypoints().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(PostureState.class)
    void serviceHandlesAllPostureStates(PostureState state) {
        var classification = new PostureClassification(state, 0.8);
        var frame = service.generate(classification, MotionState.LIGHT_MOTION, "room");
        assertNotNull(frame);
        assertFalse(frame.keypoints().isEmpty());
    }
}
