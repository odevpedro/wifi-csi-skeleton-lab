import { useState } from 'react'
import type { ManualStateRequest, MotionState, PostureState } from '../types'

const POSTURES: PostureState[] = ['STANDING', 'WALKING', 'SITTING', 'LYING_DOWN', 'ARMS_UP', 'CROUCHING', 'UNKNOWN']
const MOTIONS: MotionState[] = ['NO_PRESENCE', 'PRESENCE_DETECTED', 'NO_MOTION', 'LIGHT_MOTION', 'MEDIUM_MOTION', 'STRONG_MOTION']

const BACKEND = 'http://localhost:8080'

interface Props {
  roomId: string
  onActivate: () => void
  onDeactivate: () => void
}

export default function ManualControl({ roomId, onActivate, onDeactivate }: Props) {
  const [posture, setPosture] = useState<PostureState>('STANDING')
  const [motion, setMotion] = useState<MotionState>('NO_MOTION')

  const activate = async () => {
    const body: ManualStateRequest = { roomId, postureState: posture, motionState: motion }
    await fetch(`${BACKEND}/api/simulation/state`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })
    onActivate()
  }

  const deactivate = async () => {
    await fetch(`${BACKEND}/api/simulation/state`, { method: 'DELETE' })
    onDeactivate()
  }

  return (
    <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', alignItems: 'center' }}>
      <select value={posture} onChange={(e) => setPosture(e.target.value as PostureState)}
        style={{ background: '#222', color: '#fff', border: '1px solid #444', padding: '4px 8px' }}>
        {POSTURES.map((p) => <option key={p} value={p}>{p}</option>)}
      </select>
      <select value={motion} onChange={(e) => setMotion(e.target.value as MotionState)}
        style={{ background: '#222', color: '#fff', border: '1px solid #444', padding: '4px 8px' }}>
        {MOTIONS.map((m) => <option key={m} value={m}>{m}</option>)}
      </select>
      <button onClick={activate}
        style={{ background: '#fa4', color: '#000', border: 'none', padding: '4px 12px', cursor: 'pointer' }}>
        Ativar Manual
      </button>
      <button onClick={deactivate}
        style={{ background: '#444', color: '#fff', border: 'none', padding: '4px 12px', cursor: 'pointer' }}>
        Desativar
      </button>
    </div>
  )
}
