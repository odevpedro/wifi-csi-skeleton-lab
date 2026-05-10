package com.example.wificsiskeleton.skeleton;

import com.example.wificsiskeleton.classification.PostureClassification;

public interface SkeletonGenerator {
    SkeletonFrame generate(PostureClassification classification, String roomId);
}
