import { useState, useEffect } from 'react'
import { useWebSocket } from './hooks/useWebSocket'
import SkeletonViewer from './components/SkeletonViewer'
import SignalChart from './components/SignalChart'
import ManualControl from './components/ManualControl'
import EventLog from './components/EventLog'
import type { RoomStateEvent, SignalSummary } from './types'

const ROOM_ID = 'bedroom'
const MAX_HISTORY = 60

interface ChartPoint extends SignalSummary { time: string }

export default function App() {
  const { event, connected } = useWebSocket(ROOM_ID)
  const [history, setHistory] = useState<RoomStateEvent[]>([])
  const [chartData, setChartData] = useState<ChartPoint[]>([])
  const [manualActive, setManualActive] = useState(false)

  useEffect(() => {
    if (!event) return
    setHistory((prev) => [...prev.slice(-(MAX_HISTORY - 1)), event])
    setChartData((prev) => [
      ...prev.slice(-(MAX_HISTORY - 1)),
      {
        ...event.signal,
        time: new Date(event.timestamp).toLocaleTimeString(),
      },
    ])
  }, [event])

  const latest = history[history.length - 1] ?? null

  return (
    <div style={{ minHeight: '100vh', background: '#0d0d0d', color: '#e0e0e0', fontFamily: 'monospace', padding: 16 }}>
      <h1 style={{ margin: '0 0 4px', fontSize: 20, letterSpacing: 2, color: '#4af' }}>
        Wi-Fi CSI Skeleton Lab
      </h1>

      <div style={{ display: 'flex', gap: 8, marginBottom: 12, flexWrap: 'wrap', fontSize: 13 }}>
        <span style={{ color: connected ? '#4fa' : '#f44' }}>
          {connected ? 'CONECTADO' : 'DESCONECTADO'}
        </span>
        {manualActive && (
          <span style={{ background: '#fa4', color: '#000', padding: '1px 8px', borderRadius: 4 }}>
            MODO MANUAL ATIVO
          </span>
        )}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16, marginBottom: 16 }}>
        <div style={{ background: '#161616', borderRadius: 8, padding: 12 }}>
          <div style={{ marginBottom: 8, fontSize: 13, color: '#888' }}>Sala: {ROOM_ID}</div>
          {latest ? (
            <>
              <Row label="Movimento" value={latest.motionState} color="#4af" />
              <Row label="Postura" value={latest.postureState} color="#4fa" />
              <Row label="Confianca" value={`${(latest.confidence * 100).toFixed(0)}%`} color="#fa4" />
              <Row label="RMS Energy" value={latest.signal.rmsEnergy.toFixed(2)} />
              <Row label="Variancia" value={latest.signal.variance.toFixed(2)} />
              <Row label="Delta Baseline" value={latest.signal.baselineDifference.toFixed(2)} />
            </>
          ) : (
            <div style={{ color: '#555' }}>Aguardando dados...</div>
          )}
          <div style={{ marginTop: 12 }}>
            <div style={{ fontSize: 11, color: '#666', marginBottom: 4 }}>Modo manual (expira em 10s):</div>
            <ManualControl
              roomId={ROOM_ID}
              onActivate={() => setManualActive(true)}
              onDeactivate={() => setManualActive(false)}
            />
          </div>
        </div>

        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', background: '#161616', borderRadius: 8, padding: 12 }}>
          {latest ? (
            <SkeletonViewer
              keypoints={latest.skeleton?.keypoints ?? []}
              motionState={latest.motionState}
              confidence={latest.confidence}
            />
          ) : (
            <div style={{ color: '#333' }}>Sem dados de skeleton</div>
          )}
        </div>
      </div>

      <div style={{ background: '#161616', borderRadius: 8, padding: 12, marginBottom: 16 }}>
        <div style={{ fontSize: 12, color: '#888', marginBottom: 8 }}>Sinal em tempo real</div>
        <SignalChart history={chartData} />
      </div>

      <div style={{ background: '#161616', borderRadius: 8, padding: 12 }}>
        <div style={{ fontSize: 12, color: '#888', marginBottom: 6 }}>Historico de eventos</div>
        <EventLog events={history} />
      </div>

      <div style={{ marginTop: 12, fontSize: 10, color: '#444' }}>
        AVISO: No MVP 0, o PostureState e derivado do campo scenario do simulador, nao inferido do sinal real.
        O boneco 2D e uma representacao baseada em estado, nao reconstrucao corporal.
      </div>
    </div>
  )
}

function Row({ label, value, color = '#e0e0e0' }: { label: string; value: string; color?: string }) {
  return (
    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4, fontSize: 13 }}>
      <span style={{ color: '#888' }}>{label}:</span>
      <span style={{ color }}>{value}</span>
    </div>
  )
}
