package com.example.gamebackend;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
  private final GameWebSocketHandler handler;
  public WebSocketConfig(GameWebSocketHandler handler) { this.handler = handler; }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(handler, "/ws")
            .setAllowedOriginPatterns("*"); // dev-friendly; lock down for prod
  }
}
