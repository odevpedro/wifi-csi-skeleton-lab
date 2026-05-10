package com.example.wificsiskeleton.skeleton;

import com.example.wificsiskeleton.classification.PostureClassification;
import com.example.wificsiskeleton.classification.PostureState;
import com.example.wificsiskeleton.detection.MotionState;
import org.springframework.stereotype.Service;

@Service
public class SkeletonService {

    private final StandingSkeletonGenerator standing;
    private final SittingSkeletonGenerator sitting;
    private final WalkingSkeletonGenerator walking;
    private final LyingDownSkeletonGenerator lyingDown;
    private final ArmsUpSkeletonGenerator armsUp;
    private final UnknownSkeletonGenerator unknown;

    public SkeletonService(StandingSkeletonGenerator standing,
                           SittingSkeletonGenerator sitting,
                           WalkingSkeletonGenerator walking,
                           LyingDownSkeletonGenerator lyingDown,
                           ArmsUpSkeletonGenerator armsUp,
                           UnknownSkeletonGenerator unknown) {
        this.standing = standing;
        this.sitting = sitting;
        this.walking = walking;
        this.lyingDown = lyingDown;
        this.armsUp = armsUp;
        this.unknown = unknown;
    }

    public SkeletonFrame generate(PostureClassification classification, MotionState motionState, String roomId) {
        PostureState state = classification.postureState();
        return switch (state) {
            case STANDING   -> standing.generate(classification, roomId);
            case SITTING    -> sitting.generate(classification, roomId);
            case WALKING    -> walking.generate(classification, roomId);
            case LYING_DOWN -> lyingDown.generate(classification, roomId);
            case ARMS_UP    -> armsUp.generate(classification, roomId);
            case CROUCHING  -> sitting.generate(classification, roomId);
            case UNKNOWN    -> unknown.generate(classification, motionState, roomId);
        };
    }
}
