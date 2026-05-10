import { useEffect, useRef, useState } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import type { RoomStateEvent } from '../types'

const BACKEND = 'http://localhost:8080'

export function useWebSocket(roomId: string) {
  const [event, setEvent] = useState<RoomStateEvent | null>(null)
  const [connected, setConnected] = useState(false)
  const clientRef = useRef<Client | null>(null)

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS(`${BACKEND}/ws`),
      reconnectDelay: 3000,
      onConnect: () => {
        setConnected(true)
        client.subscribe(`/topic/rooms/${roomId}/events`, (msg) => {
          try {
            setEvent(JSON.parse(msg.body) as RoomStateEvent)
          } catch (_) {}
        })
      },
      onDisconnect: () => setConnected(false),
    })
    client.activate()
    clientRef.current = client
    return () => { client.deactivate() }
  }, [roomId])

  return { event, connected }
}
