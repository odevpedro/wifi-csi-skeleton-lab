import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts'
import type { SignalSummary } from '../types'

interface DataPoint extends SignalSummary {
  time: string
}

interface Props {
  history: DataPoint[]
}

export default function SignalChart({ history }: Props) {
  return (
    <ResponsiveContainer width="100%" height={200}>
      <LineChart data={history}>
        <CartesianGrid strokeDasharray="3 3" stroke="#333" />
        <XAxis dataKey="time" tick={{ fill: '#aaa', fontSize: 10 }} />
        <YAxis tick={{ fill: '#aaa', fontSize: 10 }} />
        <Tooltip contentStyle={{ background: '#222', border: '1px solid #444' }} />
        <Legend />
        <Line type="monotone" dataKey="rmsEnergy" stroke="#4af" dot={false} name="RMS Energy" />
        <Line type="monotone" dataKey="variance" stroke="#fa4" dot={false} name="Variance" />
        <Line type="monotone" dataKey="baselineDifference" stroke="#f4a" dot={false} name="Delta Baseline" />
      </LineChart>
    </ResponsiveContainer>
  )
}
