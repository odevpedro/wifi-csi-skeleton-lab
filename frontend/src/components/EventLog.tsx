import type { RoomStateEvent } from '../types'

interface Props {
  events: RoomStateEvent[]
}

export default function EventLog({ events }: Props) {
  return (
    <div style={{ height: 120, overflowY: 'auto', background: '#0a0a0a', padding: 8, borderRadius: 4, fontFamily: 'monospace', fontSize: 12 }}>
      {[...events].reverse().map((e, i) => (
        <div key={i} style={{ color: '#aaa', marginBottom: 2 }}>
          [{new Date(e.timestamp).toLocaleTimeString()}]{' '}
          <span style={{ color: '#4af' }}>{e.motionState}</span>{' / '}
          <span style={{ color: '#4fa' }}>{e.postureState}</span>{' '}
          <span style={{ color: '#888' }}>conf:{(e.confidence * 100).toFixed(0)}%</span>
        </div>
      ))}
    </div>
  )
}
