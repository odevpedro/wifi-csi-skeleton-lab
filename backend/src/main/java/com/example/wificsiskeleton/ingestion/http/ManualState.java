package com.example.wificsiskeleton.ingestion.http;

import com.example.wificsiskeleton.classification.PostureState;
import com.example.wificsiskeleton.detection.MotionState;

public record ManualState(
        String roomId,
        PostureState postureState,
        MotionState motionState
) {}
