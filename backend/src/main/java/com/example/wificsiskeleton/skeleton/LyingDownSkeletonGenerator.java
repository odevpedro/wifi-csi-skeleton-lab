package com.example.wificsiskeleton.skeleton;

import com.example.wificsiskeleton.classification.PostureClassification;
import org.springframework.stereotype.Component;

@Component
public class LyingDownSkeletonGenerator implements SkeletonGenerator {

    @Override
    public SkeletonFrame generate(PostureClassification classification, String roomId) {
        return new SkeletonFrame(
                System.currentTimeMillis(),
                roomId,
                SkeletonMode.STATE_BASED,
                Keypoints.lyingDown(classification.confidence())
        );
    }
}
