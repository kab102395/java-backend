# Java Backend (Spring Boot)

## Prereqs
- JDK 17+
- Maven (`mvn -v`)

## Run
```bash
mvn spring-boot:run
# Server: http://localhost:8080
# WS:     ws://localhost:8080/ws
```

## Test
```bash
curl -X GET http://localhost:8080/api/health
curl -X POST http://localhost:8080/api/login -H "Content-Type: application/json" -d '{"email":"kyle@test.com","password":"x"}'
```

## Notes
- In-memory store only; restart resets data.
- WebSocket handler echoes messages and broadcasts progress updates.
