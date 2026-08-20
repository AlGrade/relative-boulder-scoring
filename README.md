# Relative Boulder Scoring

Wettkampf-Auswertung für Boulder-Sessions mit relativem Punktesystem: Ein Boulder ist
umso mehr wert, je weniger Teilnehmer ihn geschafft haben.

Nachfolger von `boulderauswertung` (Angular + Airtable) - diesmal mit echtem Backend
und echter Datenbank.

Die Oberfläche ist durchgehend deutsch, Code und Kommentare sind englisch. Wer das
Projekt übernimmt und eine andere Sprache braucht, findet die Texte in den Angular-
Templates und in `ProblemDetail`-Meldungen im Backend - eine i18n-Bibliothek gibt es
bewusst nicht.

## Wertung

- Ein Boulder ist `1000 / Anzahl Begehungen` wert, `1000` wenn ihn noch niemand
  geschafft hat.
- Ein Flash zählt für den Kletternden ×1.2. Der Wert des Boulders selbst ändert sich
  dadurch nicht.
- Begehungen, Boulderwerte und Rangliste werden je Wertungsklasse (`MALE` / `FEMALE`)
  getrennt gerechnet - derselbe Boulder kann in beiden Klassen unterschiedlich viel
  zählen.
- Gleiche Punktzahl bedeutet gleicher Rang, der nächste Rang überspringt entsprechend
  (1, 1, 3).
- Ein Flash setzt eine Begehung voraus. Nimmt jemand die Begehung zurück, ist auch der
  Flash weg; ein Un-Flash lässt die Begehung stehen.

Die gesamte Rechnung steckt in `ScoringService` - im Frontend wird nur angezeigt.

## Wettkampf-Fenster

`comp.open-until` ist ein Zeitpunkt, kein Schalter: bis dahin werden Registrierungen
und Begehungen angenommen, danach nicht mehr (`403`). Lesen - Rangliste, Boulderwerte,
Boulderliste - bleibt immer öffentlich, und einloggen kann man sich auch danach noch.

Bewusst ein Ablaufdatum, weil die beiden Fehler unterschiedlich weh tun: das Fenster
nicht zu öffnen fällt binnen einer Minute auf, es nicht zu schliessen lässt monatelang
jeden mitregistrieren, der die URL kennt. Ohne gesetzten Wert ist zu - offen ist die
Anwendung nur mit Absicht. Lokal steht der Wert auf einem Datum weit in der Zukunft.

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
├── compose.prod.yaml # Produktions-Stack (nginx, Backend, Postgres, Cloudflare-Tunnel)
├── backend/         # Spring-Boot-API (Port 8080)
│   └── src/main/java/com/boulderscoring/
│       ├── controller/   REST-Endpunkte
│       ├── service/      Fachlogik, darunter die relative Wertung
│       ├── repository/   Spring-Data-JPA-Repositories
│       ├── model/        JPA-Entities: Competitor, Boulder, Ascent, Gender
│       ├── dto/          Request- und Response-Records
│       ├── security/     Principal und UserDetailsService
│       ├── exception/    Fachfehler und ihr Mapping auf ProblemDetail
│       └── config/       Spring-Security-Konfiguration
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
verdrahtet die Datasource selbst - es müssen keine DB-Zugangsdaten gesetzt werden.
Der Container läuft nach dem Beenden der App weiter (`lifecycle-management: start_only`).

Beim Start läuft `backend/src/main/resources/demo-data.sql` mit und legt eine
Beispiel-Runde an: 15 Boulder, 10 Teilnehmer und deren Begehungen. Damit zeigen
Rangliste und Boulderwerte sofort etwas Sinnvolles. Anmelden kann man sich mit jedem
der Namen aus dem Skript (z. B. `Chris Maier`) und dem Passwort `geheim123`.

Das Skript ist idempotent (`ON CONFLICT DO NOTHING`) und läuft bei jedem Start mit,
ohne vorhandene Daten anzufassen. Für einen echten Wettkampf in `application.yaml`
`spring.sql.init.mode` auf `never` stellen und die Boulder direkt anlegen - die
Anwendung liest sie nur, einen Endpunkt dafür gibt es bewusst nicht:

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
| GET     | `/api/competition`                | –    | `{open}` - nimmt der Wettkampf gerade Änderungen an? |
| GET     | `/api/boulders`                   | –    | alle Boulder nach Nummer                    |
| GET     | `/api/me/ascents`                 | ✓    | eigene Begehungen                           |
| PUT     | `/api/me/ascents/{nummer}`        | ✓    | `{flashed}` - legt die Begehung an, setzt den Flash |
| DELETE  | `/api/me/ascents/{nummer}`        | ✓    | Begehung samt Flash entfernen               |
| GET     | `/api/ranking?gender=MALE`        | –    | Rangliste                                   |
| GET     | `/api/boulder-points?gender=MALE` | –    | aktuelle Boulderwerte                       |

Rangliste und Boulderwerte sind bewusst öffentlich - die Landing Page zeigt sie auch
ohne Login. Fehler kommen als `ProblemDetail` (RFC 9457) zurück.

## Betrieb

Produktion läuft auf `https://comp.boulderbot.win`: Angular hinter nginx, Backend und
PostgreSQL daneben, veröffentlicht über einen Cloudflare-Tunnel. Nach aussen ist kein
Port offen - der Connector wählt sich hinaus, DNS zeigt nur auf Cloudflare, TLS endet
dort. Deployments laufen von Hand, es gibt bewusst keine CI und keinen Deploy-Key.

Serveradressen, Cloudflare-Konfiguration, Release-Ablauf und der Wettkampftag-Runbook
liegen absichtlich ausserhalb des Repos in `.local/hetzner-deployment.md`.

## Lizenz

MIT, siehe [LICENSE](LICENSE).

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
