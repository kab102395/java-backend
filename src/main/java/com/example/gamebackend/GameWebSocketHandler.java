package com.example.gamebackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {
  private static final ObjectMapper OM = new ObjectMapper();
  private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

  @Override public void afterConnectionEstablished(WebSocketSession session) {
    sessions.add(session);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage msg) throws Exception {
    var out = new java.util.LinkedHashMap<String,Object>();
    out.put("type", "echo");
    out.put("payload", msg.getPayload());
    out.put("ts", Instant.now().toString());
    session.sendMessage(new TextMessage(OM.writeValueAsString(out)));
  }

  @Override public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    sessions.remove(session);
  }

  /** Broadcast a JSON string to all connected clients. */
  public void broadcast(String json) {
    for (WebSocketSession s : sessions) {
      if (s.isOpen()) {
        try { s.sendMessage(new TextMessage(json)); } catch (IOException ignored) {}
      }
    }
  }
}
