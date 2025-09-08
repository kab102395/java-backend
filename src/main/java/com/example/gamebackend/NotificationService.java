package com.example.gamebackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class NotificationService {
    private final GameWebSocketHandler ws;
    private final EventLog events;
    private static final ObjectMapper OM = new ObjectMapper();

    public NotificationService(GameWebSocketHandler ws, EventLog events) {
        this.ws = ws; this.events = events;
    }

    @Async
    public void broadcastServerAsync(Map<String, Object> payload) {
        var out = new LinkedHashMap<String, Object>();
        out.put("type", "server");
        out.put("payload", payload);
        try { ws.broadcast(OM.writeValueAsString(out)); } catch (Exception ignored) {}
        events.add(out);
    }

    @Async
    public void broadcastRawAsync(Map<String, Object> message) {
        try { ws.broadcast(OM.writeValueAsString(message)); } catch (Exception ignored) {}
        events.add(message);
    }
}
