package com.example.gamebackend;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class EventLog {
    public static class Event {
        public long id;
        public Map<String, Object> data;
        public Event(long id, Map<String,Object> data) { this.id = id; this.data = data; }
    }

    private final AtomicLong seq = new AtomicLong();
    private final Deque<Event> ring = new ArrayDeque<>(); // small ring buffer
    private static final int MAX = 1000;

    public synchronized Event add(Map<String,Object> msg) {
        long id = seq.incrementAndGet();
        var copy = new LinkedHashMap<String,Object>(msg);
        var ev = new Event(id, copy);
        ring.addLast(ev);
        while (ring.size() > MAX) ring.removeFirst();
        return ev;
    }

    public synchronized List<Event> after(long last) {
        List<Event> out = new ArrayList<>();
        for (Event e : ring) if (e.id > last) out.add(e);
        return out;
    }

    public long lastId() { return seq.get(); }
}
