package com.example.wificsiskeleton.websocket;

import com.example.wificsiskeleton.classification.PostureState;
import com.example.wificsiskeleton.detection.MotionState;
import com.example.wificsiskeleton.detection.SignalSummary;
import com.example.wificsiskeleton.skeleton.SkeletonFrame;

public record RoomStateEvent(
        long timestamp,
        String roomId,
        MotionState motionState,
        PostureState postureState,
        double confidence,
        SignalSummary signal,
        SkeletonFrame skeleton
) {}
