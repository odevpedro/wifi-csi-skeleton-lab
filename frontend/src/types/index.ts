export type MotionState =
  | 'NO_PRESENCE'
  | 'PRESENCE_DETECTED'
  | 'NO_MOTION'
  | 'LIGHT_MOTION'
  | 'MEDIUM_MOTION'
  | 'STRONG_MOTION'

export type PostureState =
  | 'UNKNOWN'
  | 'STANDING'
  | 'SITTING'
  | 'LYING_DOWN'
  | 'WALKING'
  | 'ARMS_UP'
  | 'CROUCHING'

export interface SignalSummary {
  rmsEnergy: number
  variance: number
  baselineDifference: number
}

export interface BodyKeypoint {
  name: string
  x: number
  y: number
  confidence: number
}

export interface SkeletonFrame {
  timestamp: number
  roomId: string
  mode: string
  keypoints: BodyKeypoint[]
}

export interface RoomStateEvent {
  timestamp: number
  roomId: string
  deviceId: string
  motionState: MotionState
  postureState: PostureState
  confidence: number
  signal: SignalSummary
  skeleton: SkeletonFrame
}

export interface ManualStateRequest {
  roomId: string
  postureState: PostureState
  motionState: MotionState
}
