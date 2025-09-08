package com.example.gamebackend.service;

import com.example.gamebackend.model.Player;
import com.example.gamebackend.model.Progress;
import com.example.gamebackend.dto.LoginRequest;
import com.example.gamebackend.dto.LoginResponse;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlayerService {
    private final Map<String, Player> players = new ConcurrentHashMap<>();

    public LoginResponse login(LoginRequest req) {
        Player existing = players.values().stream()
                .filter(x -> x.email != null && x.email.equalsIgnoreCase(req.email))
                .findFirst().orElse(null);
        Player p = existing != null ? existing : create(req.email);

        LoginResponse lr = new LoginResponse();
        lr.playerId = p.id;
        lr.displayName = p.displayName;
        lr.token = Base64.getEncoder().encodeToString((p.id + ":" + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));
        return lr;
    }

    private Player create(String email) {
        Player np = new Player();
        np.id = UUID.randomUUID().toString();
        np.email = email;
        np.displayName = email;
        players.put(np.id, np);
        return np;
    }

    public Player saveProgress(String playerId, Progress pr) {
        Player p = players.get(playerId);
        if (p == null) throw new RuntimeException("no player");
        p.level = pr.level; p.xp = pr.xp;
        return p;
    }

    public Player setFcmToken(String playerId, String token) {
        Player p = players.get(playerId);
        if (p == null) throw new RuntimeException("no player");
        p.fcmToken = token; return p;
    }

    public Player get(String id) { return players.get(id); }
}
