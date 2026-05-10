package com.example.wificsiskeleton.skeleton;

import java.util.List;

public record SkeletonFrame(
        long timestamp,
        String roomId,
        SkeletonMode mode,
        List<BodyKeypoint> keypoints
) {}
