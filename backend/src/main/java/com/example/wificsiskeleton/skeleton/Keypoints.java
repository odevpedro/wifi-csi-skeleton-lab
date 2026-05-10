package com.example.wificsiskeleton.skeleton;

import java.util.List;

/**
 * Shared factory for standard keypoint poses.
 */
class Keypoints {

    static List<BodyKeypoint> standing(double conf) {
        return List.of(
                new BodyKeypoint("head",          320, 90,  conf),
                new BodyKeypoint("neck",          315, 140, conf),
                new BodyKeypoint("leftShoulder",  270, 150, conf),
                new BodyKeypoint("rightShoulder", 360, 150, conf),
                new BodyKeypoint("leftElbow",     240, 210, conf),
                new BodyKeypoint("rightElbow",    390, 210, conf),
                new BodyKeypoint("leftHand",      230, 270, conf),
                new BodyKeypoint("rightHand",     410, 260, conf),
                new BodyKeypoint("hip",           315, 260, conf),
                new BodyKeypoint("leftKnee",      280, 360, conf),
                new BodyKeypoint("rightKnee",     350, 360, conf),
                new BodyKeypoint("leftFoot",      270, 460, conf),
                new BodyKeypoint("rightFoot",     360, 460, conf)
        );
    }

    static List<BodyKeypoint> sitting(double conf) {
        return List.of(
                new BodyKeypoint("head",          320, 90,  conf),
                new BodyKeypoint("neck",          315, 140, conf),
                new BodyKeypoint("leftShoulder",  270, 150, conf),
                new BodyKeypoint("rightShoulder", 360, 150, conf),
                new BodyKeypoint("leftElbow",     240, 210, conf),
                new BodyKeypoint("rightElbow",    390, 210, conf),
                new BodyKeypoint("leftHand",      230, 270, conf),
                new BodyKeypoint("rightHand",     410, 270, conf),
                new BodyKeypoint("hip",           315, 280, conf),
                new BodyKeypoint("leftKnee",      240, 320, conf),
                new BodyKeypoint("rightKnee",     390, 320, conf),
                new BodyKeypoint("leftFoot",      220, 420, conf),
                new BodyKeypoint("rightFoot",     410, 420, conf)
        );
    }

    static List<BodyKeypoint> walking(double conf, boolean leftStepForward) {
        if (leftStepForward) {
            return List.of(
                    new BodyKeypoint("head",          320, 90,  conf),
                    new BodyKeypoint("neck",          315, 140, conf),
                    new BodyKeypoint("leftShoulder",  260, 155, conf),
                    new BodyKeypoint("rightShoulder", 370, 145, conf),
                    new BodyKeypoint("leftElbow",     220, 220, conf),
                    new BodyKeypoint("rightElbow",    410, 200, conf),
                    new BodyKeypoint("leftHand",      200, 280, conf),
                    new BodyKeypoint("rightHand",     430, 260, conf),
                    new BodyKeypoint("hip",           315, 265, conf),
                    new BodyKeypoint("leftKnee",      270, 370, conf),
                    new BodyKeypoint("rightKnee",     360, 340, conf),
                    new BodyKeypoint("leftFoot",      250, 470, conf),
                    new BodyKeypoint("rightFoot",     370, 450, conf)
            );
        } else {
            return List.of(
                    new BodyKeypoint("head",          320, 90,  conf),
                    new BodyKeypoint("neck",          315, 140, conf),
                    new BodyKeypoint("leftShoulder",  265, 145, conf),
                    new BodyKeypoint("rightShoulder", 365, 155, conf),
                    new BodyKeypoint("leftElbow",     225, 200, conf),
                    new BodyKeypoint("rightElbow",    405, 220, conf),
                    new BodyKeypoint("leftHand",      210, 260, conf),
                    new BodyKeypoint("rightHand",     420, 280, conf),
                    new BodyKeypoint("hip",           315, 265, conf),
                    new BodyKeypoint("leftKnee",      275, 340, conf),
                    new BodyKeypoint("rightKnee",     355, 370, conf),
                    new BodyKeypoint("leftFoot",      265, 450, conf),
                    new BodyKeypoint("rightFoot",     345, 470, conf)
            );
        }
    }

    static List<BodyKeypoint> lyingDown(double conf) {
        return List.of(
                new BodyKeypoint("head",          120, 280, conf),
                new BodyKeypoint("neck",          175, 280, conf),
                new BodyKeypoint("leftShoulder",  190, 255, conf),
                new BodyKeypoint("rightShoulder", 190, 305, conf),
                new BodyKeypoint("leftElbow",     240, 245, conf),
                new BodyKeypoint("rightElbow",    240, 315, conf),
                new BodyKeypoint("leftHand",      285, 240, conf),
                new BodyKeypoint("rightHand",     285, 320, conf),
                new BodyKeypoint("hip",           310, 280, conf),
                new BodyKeypoint("leftKnee",      410, 260, conf),
                new BodyKeypoint("rightKnee",     410, 300, conf),
                new BodyKeypoint("leftFoot",      510, 255, conf),
                new BodyKeypoint("rightFoot",     510, 305, conf)
        );
    }

    static List<BodyKeypoint> armsUp(double conf) {
        return List.of(
                new BodyKeypoint("head",          320, 90,  conf),
                new BodyKeypoint("neck",          315, 140, conf),
                new BodyKeypoint("leftShoulder",  270, 150, conf),
                new BodyKeypoint("rightShoulder", 360, 150, conf),
                new BodyKeypoint("leftElbow",     230, 90,  conf),
                new BodyKeypoint("rightElbow",    400, 90,  conf),
                new BodyKeypoint("leftHand",      210, 40,  conf),
                new BodyKeypoint("rightHand",     420, 40,  conf),
                new BodyKeypoint("hip",           315, 260, conf),
                new BodyKeypoint("leftKnee",      280, 360, conf),
                new BodyKeypoint("rightKnee",     350, 360, conf),
                new BodyKeypoint("leftFoot",      270, 460, conf),
                new BodyKeypoint("rightFoot",     360, 460, conf)
        );
    }
}
