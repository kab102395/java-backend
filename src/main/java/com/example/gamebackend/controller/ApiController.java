package com.example.gamebackend.controller;

import com.example.gamebackend.EventLog;
import com.example.gamebackend.NotificationService;
import com.example.gamebackend.dto.LoginRequest;
import com.example.gamebackend.dto.LoginResponse;
import com.example.gamebackend.model.Player;
import com.example.gamebackend.model.Progress;
import com.example.gamebackend.service.PlayerService;
import org.springframework.http.MediaType;
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

    @GetMapping(path = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @PostMapping(
        path = "/login",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        LoginResponse resp = service.login(req);
        return ResponseEntity.ok(resp);
    }

    @PostMapping(
        path = "/progress/{playerId}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Player> save(
        @PathVariable String playerId,
        @RequestBody Progress pr
    ) {
        Player p = service.saveProgress(playerId, pr);
        // fire-and-forget WS + log for HTTP polling fallback
        notifier.broadcastRawAsync(Map.of(
            "type", "progress",
            "playerId", p.id,
            "level", p.level,
            "xp", p.xp
        ));
        return ResponseEntity.ok(p);
    }

    @PostMapping(
        path = "/registerPush/{playerId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Player> register(
        @PathVariable String playerId,
        @RequestParam String token
    ) {
        Player p = service.setFcmToken(playerId, token);
        return ResponseEntity.ok(p);
    }

    @PostMapping(
        path = "/broadcast",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> broadcast(@RequestBody Map<String, Object> payload) {
        notifier.broadcastServerAsync(payload);   // async WS + log
        return ResponseEntity.accepted().build(); // 202
    }

    // HTTP polling fallback for clients where WS plugin is flaky
    @GetMapping(path = "/events/poll", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> poll(
        @RequestParam(name = "after", defaultValue = "0") long after
    ) {
        return Map.of(
            "last", events.lastId(),
            "events", events.after(after)  // list of { id, data:{...} }
        );
    }
}

/**
 * Extra root-level health endpoint so devices can test `http://<ip>/health`
 * even if they aren't calling `/api/health`. This is package-private (non-public)
 * so it can live in the same source file as ApiController.
 */
@CrossOrigin(origins = "*") // dev-only; tighten later
@RestController
class HealthAliasController {

    @GetMapping(path = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> rootHealth() {
        return Map.of("status", "ok");
    }
}
