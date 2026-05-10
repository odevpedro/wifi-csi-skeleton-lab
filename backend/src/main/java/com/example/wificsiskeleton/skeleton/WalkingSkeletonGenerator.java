package com.example.wificsiskeleton.skeleton;

import com.example.wificsiskeleton.classification.PostureClassification;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class WalkingSkeletonGenerator implements SkeletonGenerator {

    private final AtomicBoolean stepToggle = new AtomicBoolean(true);

    @Override
    public SkeletonFrame generate(PostureClassification classification, String roomId) {
        boolean leftStep = stepToggle.getAndSet(!stepToggle.get());
        return new SkeletonFrame(
                System.currentTimeMillis(),
                roomId,
                SkeletonMode.STATE_BASED,
                Keypoints.walking(classification.confidence(), leftStep)
        );
    }
}
