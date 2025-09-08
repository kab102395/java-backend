package com.example.gamebackend.controller;

import com.example.gamebackend.EventLog;
import com.example.gamebackend.NotificationService;
import com.example.gamebackend.dto.LoginRequest;
import com.example.gamebackend.dto.LoginResponse;
import com.example.gamebackend.model.Player;
import com.example.gamebackend.model.Progress;
import com.example.gamebackend.service.PlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*") // dev-only; tighten later
@RestController
@RequestMapping("/api")
public class ApiController {

    private final PlayerService service;
    private final NotificationService notifier;
    private final EventLog events;

    public ApiController(PlayerService service, NotificationService notifier, EventLog events) {
        this.service = service;
        this.notifier = notifier;
        this.events = events;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        return service.login(req);
    }

    @PostMapping("/progress/{playerId}")
    public Player save(@PathVariable String playerId, @RequestBody Progress pr) {
        Player p = service.saveProgress(playerId, pr);
        // fire-and-forget WS + log for HTTP polling fallback
        notifier.broadcastRawAsync(Map.of(
            "type", "progress",
            "playerId", p.id,
            "level", p.level,
            "xp", p.xp
        ));
        return p;
    }

    @PostMapping("/registerPush/{playerId}")
    public Player register(@PathVariable String playerId, @RequestParam String token) {
        return service.setFcmToken(playerId, token);
    }

    @PostMapping("/broadcast")
    public ResponseEntity<Void> broadcast(@RequestBody Map<String, Object> payload) {
        notifier.broadcastServerAsync(payload);   // async WS + log
        return ResponseEntity.accepted().build(); // 202
    }

    // HTTP polling fallback for clients where WS plugin is flaky
    @GetMapping("/events/poll")
    public Map<String, Object> poll(@RequestParam(name = "after", defaultValue = "0") long after) {
        return Map.of(
            "last", events.lastId(),
            "events", events.after(after)  // list of { id, data:{...} }
        );
    }
}
