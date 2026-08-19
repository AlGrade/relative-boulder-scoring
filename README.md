# Relative Boulder Scoring

Wettkampf-Auswertung für Boulder-Sessions mit relativem Punktesystem: Ein Boulder ist
umso mehr wert, je weniger Teilnehmer ihn geschafft haben.

Nachfolger von `boulderauswertung` (Angular + Airtable) — diesmal mit echtem Backend
und echter Datenbank.

## Wertung

- Ein Boulder ist `1000 / Anzahl Begehungen` wert, `1000` wenn ihn noch niemand
  geschafft hat.
- Ein Flash zählt für den Kletternden ×1.2. Der Wert des Boulders selbst ändert sich
  dadurch nicht.
- Begehungen, Boulderwerte und Rangliste werden je Wertungsklasse (`MALE` / `FEMALE`)
  getrennt gerechnet — derselbe Boulder kann in beiden Klassen unterschiedlich viel
  zählen.
- Gleiche Punktzahl bedeutet gleicher Rang, der nächste Rang überspringt entsprechend
  (1, 1, 3).
- Ein Flash setzt eine Begehung voraus. Nimmt jemand die Begehung zurück, ist auch der
  Flash weg; ein Un-Flash lässt die Begehung stehen.

Die gesamte Rechnung steckt in `ScoringService` — im Frontend wird nur angezeigt.

## Stack

| Teil       | Technologie                                        |
|------------|----------------------------------------------------|
| Frontend   | Angular 22 (standalone, zoneless, Signals, SCSS)   |
| Backend    | Spring Boot 4.1 auf Java 25 (LTS), Maven           |
| Persistenz | Spring Data JPA / Hibernate                        |
| Datenbank  | PostgreSQL 18 (Docker)                             |
| Auth       | Spring Security, BCrypt, HTTP-Session, CSRF-Cookie |

## Struktur

```
relative-boulder-scoring/
├── compose.yaml     # PostgreSQL für lokale Entwicklung
├── backend/         # Spring-Boot-API (Port 8080)
│   └── src/main/java/com/boulderscoring/
│       ├── competitor/   Teilnehmer, Registrierung, Login
│       ├── boulder/      Boulder der laufenden Runde (nur lesend)
│       ├── ascent/       Begehungen inkl. Flash-Flag
│       ├── scoring/      relative Wertung, Rangliste, Boulderwerte
│       ├── config/       Spring Security
│       └── web/          Fehler-Mapping auf ProblemDetail
└── frontend/        # Angular-App (Port 4200)
```

## Voraussetzungen

- JDK 25 (`java -version`)
- Node.js 22+ und npm
- Docker mit Compose

Maven wird über den mitgelieferten Wrapper (`./mvnw`) genutzt, eine lokale
Installation ist nicht nötig.

## Loslegen

**Backend** (startet den Postgres-Container automatisch mit):

```bash
cd backend
./mvnw spring-boot:run
```

Spring Boots Docker-Compose-Support fährt `compose.yaml` beim Start hoch und
verdrahtet die Datasource selbst — es müssen keine DB-Zugangsdaten gesetzt werden.
Der Container läuft nach dem Beenden der App weiter (`lifecycle-management: start_only`).

**Boulder anlegen.** Die Anwendung liest Boulder nur; angelegt werden sie direkt in der
Datenbank, nachdem das Backend einmal lief (Hibernate legt die Tabellen an):

```bash
docker exec -i rbs-postgres psql -U boulderscoring -d boulderscoring \
  -c "INSERT INTO boulder (number) SELECT generate_series(1, 30);"
```

**Frontend:**

```bash
cd frontend
npm start
```

Läuft auf http://localhost:4200 und proxied `/api/**` auf das Backend
(siehe `frontend/proxy.conf.json`), damit es im Dev-Betrieb kein CORS braucht.

**Datenbank separat starten** (falls mal ohne Backend gebraucht):

```bash
docker compose up -d
```

## API

| Methode | Pfad                              | Auth | Zweck                                       |
|---------|-----------------------------------|------|---------------------------------------------|
| POST    | `/api/auth/register`              | –    | `{name, gender, password}`, loggt direkt ein |
| POST    | `/api/auth/login`                 | –    | `{name, password}`                          |
| POST    | `/api/auth/logout`                | ✓    | beendet die Session                         |
| GET     | `/api/auth/me`                    | ✓    | aktueller Teilnehmer, sonst 401             |
| GET     | `/api/boulders`                   | –    | alle Boulder nach Nummer                    |
| GET     | `/api/me/ascents`                 | ✓    | eigene Begehungen                           |
| PUT     | `/api/me/ascents/{nummer}`        | ✓    | `{flashed}` — legt die Begehung an, setzt den Flash |
| DELETE  | `/api/me/ascents/{nummer}`        | ✓    | Begehung samt Flash entfernen               |
| GET     | `/api/ranking?gender=MALE`        | –    | Rangliste                                   |
| GET     | `/api/boulder-points?gender=MALE` | –    | aktuelle Boulderwerte                       |

Rangliste und Boulderwerte sind bewusst öffentlich — die Landing Page zeigt sie auch
ohne Login. Fehler kommen als `ProblemDetail` (RFC 9457) zurück.

## Konventionen

- Das Schema wird im Dev-Betrieb von Hibernate aus den Entities abgeleitet
  (`ddl-auto: update`). Sobald das Datenmodell steht, auf `validate` wechseln
  und Migrationen über ein Tool wie Flyway oder Liquibase fahren.
- Zeiten werden in UTC persistiert.
- `open-in-view` ist aus: Entities werden im Service-Layer vollständig geladen,
  Lazy-Loading im Controller schlägt bewusst fehl.
- Die Session steckt in einem `JSESSIONID`-Cookie, CSRF-Schutz läuft über das
  `XSRF-TOKEN`-Cookie, das Angulars `HttpClient` von sich aus als `X-XSRF-TOKEN`
  zurückschickt. Deshalb ist in `SecurityConfig` der einfache
  `CsrfTokenRequestAttributeHandler` gesetzt und nicht der verschlüsselnde Default.
- Tests gibt es bewusst keine.
