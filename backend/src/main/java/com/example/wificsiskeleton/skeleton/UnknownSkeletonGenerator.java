package com.example.wificsiskeleton.skeleton;

import com.example.wificsiskeleton.classification.PostureClassification;
import com.example.wificsiskeleton.detection.MotionState;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Fallback generator for PostureState.UNKNOWN.
 * Behavior varies per MotionState per spec section 11.
 */
@Component
public class UnknownSkeletonGenerator {

    public SkeletonFrame generate(PostureClassification classification, MotionState motionState, String roomId) {
        return switch (motionState) {
            case NO_PRESENCE -> new SkeletonFrame(
                    System.currentTimeMillis(), roomId, SkeletonMode.STATE_BASED,
                    applyOpacity(Keypoints.standing(0.1), 0.1)
            );
            case STRONG_MOTION -> new SkeletonFrame(
                    System.currentTimeMillis(), roomId, SkeletonMode.STATE_BASED,
                    applyOpacity(Keypoints.standing(0.4), 0.4)
            );
            default -> new SkeletonFrame(
                    System.currentTimeMillis(), roomId, SkeletonMode.STATE_BASED,
                    applyOpacity(Keypoints.standing(0.3), 0.3)
            );
        };
    }

    private List<BodyKeypoint> applyOpacity(List<BodyKeypoint> keypoints, double conf) {
        return keypoints.stream()
                .map(k -> new BodyKeypoint(k.name(), k.x(), k.y(), conf))
                .toList();
    }
}
