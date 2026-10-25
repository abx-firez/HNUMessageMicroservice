# About

Dieses Repo ist ein Beispiel für eine Microservice-Architektur, die mit Spring Boot, Spring Data und einer SQLite Datenbank beispielhaft umgesetzt wurde.

Das eigentliche Lab hierzu ist [hier zu finden.](Lab-Microservice.md)

## Lösung

Der Code unter `src/` ist die Musterlösung für das gesamte Lab: den Basisteil (Schritte 1–9) und alle Aufgaben aus *Lab - extended* (1–8). Die optionalen Anhänge (H2, PostgreSQL) sind nicht enthalten.

Alles, was aus dem erweiterten Teil stammt, ist im Code mit `// EXTENDED LAB - <Nr>` markiert, die Nummer entspricht der Aufgabe im Lab. Code ohne Markierung gehört zum Basisteil. Einen Überblick über alle Stellen liefert zum Beispiel:

```bash
grep -rn "EXTENDED LAB" src/
```

### Starten

```bash
./mvnw spring-boot:run
```

Unter Windows: `mvnw.cmd spring-boot:run`. Die Anwendung läuft danach auf `http://localhost:8080`.

### Endpunkte

| Methode | Pfad | Antwort | Teil |
|---|---|---|---|
| POST | `/api/messages` | 201, Body `{ "content": "...", "userId": 1 }` (`userId` optional) | Basis |
| GET | `/api/messages` | 200, alle Nachrichten | Basis |
| GET | `/api/messages?q=text` | 200, Suche ohne Beachtung der Groß-/Kleinschreibung | Extended 6 |
| PUT | `/api/messages/{id}` | 200, 404 | Extended 1 |
| DELETE | `/api/messages/{id}` | 204, 404 | Extended 2 |
| POST | `/api/users` | 201, Body `{ "name": "..." }` | Extended 5 |
| GET | `/api/users`, `/api/users/{id}` | 200, 404 | Extended 5 |
| GET | `/api/users/{id}/messages` | 200, 404 | Extended 5 |
| PUT | `/api/users/{id}` | 200, 404 | Extended 5 |
| DELETE | `/api/users/{id}` | 204, 404, löscht auch die Nachrichten des Benutzers | Extended 5 |

Ungültige Eingaben werden mit 400 beantwortet, unerwartete Fehler mit 500. Alle Fehlerantworten haben das Format *Problem Details* (RFC 9457), siehe `GlobalExceptionHandler`.

## API testen

Im Ordner [HNUMessageMicroserviceAPI-Testing](HNUMessageMicroserviceAPI-Testing) befindet sich eine Bruno-Collection, die zum Testen der API verwendet werden kann. Sie ist in die Ordner `Basic` und `Extended` aufgeteilt, jeder Request prüft den erwarteten Statuscode. Der Port steht zentral in der Variable `baseUrl` (`collection.bru`).
