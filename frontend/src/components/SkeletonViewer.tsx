import type { BodyKeypoint, MotionState } from '../types'

const BONES: [string, string][] = [
  ['head', 'neck'],
  ['neck', 'leftShoulder'],
  ['leftShoulder', 'leftElbow'],
  ['leftElbow', 'leftHand'],
  ['neck', 'rightShoulder'],
  ['rightShoulder', 'rightElbow'],
  ['rightElbow', 'rightHand'],
  ['neck', 'hip'],
  ['hip', 'leftKnee'],
  ['leftKnee', 'leftFoot'],
  ['hip', 'rightKnee'],
  ['rightKnee', 'rightFoot'],
]

const MOTION_COLORS: Record<MotionState, string> = {
  NO_PRESENCE: '#555',
  PRESENCE_DETECTED: '#888',
  NO_MOTION: '#4af',
  LIGHT_MOTION: '#4fa',
  MEDIUM_MOTION: '#fa4',
  STRONG_MOTION: '#f44',
}

interface Props {
  keypoints: BodyKeypoint[]
  motionState: MotionState
  confidence: number
}

export default function SkeletonViewer({ keypoints, motionState, confidence }: Props) {
  const kpMap = Object.fromEntries(keypoints.map((k) => [k.name, k]))
  const color = MOTION_COLORS[motionState] ?? '#4af'
  const opacity = Math.max(0.1, confidence)

  return (
    <svg viewBox="0 0 640 560" width="320" height="280" style={{ background: '#111', borderRadius: 8 }}>
      {BONES.map(([a, b]) => {
        const ka = kpMap[a]
        const kb = kpMap[b]
        if (!ka || !kb) return null
        return (
          <line
            key={`${a}-${b}`}
            x1={ka.x} y1={ka.y}
            x2={kb.x} y2={kb.y}
            stroke={color}
            strokeWidth={3}
            opacity={opacity}
          />
        )
      })}
      {keypoints.map((k) => (
        <circle
          key={k.name}
          cx={k.x} cy={k.y} r={6}
          fill={color}
          opacity={Math.max(0.1, k.confidence)}
        />
      ))}
    </svg>
  )
}
