# Relative Boulder Scoring

Wettkampf-Auswertung für Boulder-Sessions mit relativem Punktesystem: Ein Boulder ist
umso mehr wert, je weniger Teilnehmer ihn geschafft haben.

Nachfolger von `boulderauswertung` (Angular + Airtable) — diesmal mit echtem Backend
und echter Datenbank.

## Stack

| Teil       | Technologie                                      |
|------------|--------------------------------------------------|
| Frontend   | Angular 22 (standalone, zoneless, Signals, SCSS) |
| Backend    | Spring Boot 4.1 auf Java 25 (LTS), Maven         |
| Persistenz | Spring Data JPA / Hibernate, Flyway-Migrationen  |
| Datenbank  | PostgreSQL 18 (Docker)                           |
| Tests      | JUnit 5 + Testcontainers, Vitest                 |

## Struktur

```
relative-boulder-scoring/
├── compose.yaml     # PostgreSQL für lokale Entwicklung
├── backend/         # Spring-Boot-API (Port 8080)
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

## Tests

```bash
cd backend && ./mvnw test     # startet Postgres per Testcontainers
cd frontend && npm test       # Vitest
```

## Konventionen

- **Schema-Änderungen ausschließlich über Flyway** in
  `backend/src/main/resources/db/migration` (`V<n>__beschreibung.sql`).
  Hibernate steht auf `ddl-auto: validate` und erzeugt selbst nichts.
- Zeiten werden in UTC persistiert.
- `open-in-view` ist aus: Entities werden im Service-Layer vollständig geladen,
  Lazy-Loading im Controller schlägt bewusst fehl.
