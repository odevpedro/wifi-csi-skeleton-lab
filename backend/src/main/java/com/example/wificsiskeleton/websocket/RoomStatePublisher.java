package com.example.wificsiskeleton.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RoomStatePublisher {

    private final SimpMessagingTemplate messaging;

    public RoomStatePublisher(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    public void publish(RoomStateEvent event) {
        messaging.convertAndSend("/topic/rooms/" + event.roomId() + "/events", event);
    }
}
