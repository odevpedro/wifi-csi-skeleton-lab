package com.example.wificsiskeleton.classification;

import com.example.wificsiskeleton.csi.model.CsiWindow;
import com.example.wificsiskeleton.csi.model.RoomBaseline;

public interface PostureClassifier {
    PostureClassification classify(CsiWindow window, RoomBaseline baseline);
}
