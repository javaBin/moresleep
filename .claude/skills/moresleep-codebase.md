# Moresleep Codebase Architecture

**Context**: Session management system for JavaZone conference talks. Replaces legacy SleepingPillCore. Handles talk submission, review, and public API for conference schedules.

**Tech Stack**: Kotlin, Jetty, PostgreSQL, Flyway, JsonBuddy, Maven

---

## High-Level Goals

1. **Public API**: Read-only access to published JavaZone talks (no auth required)
2. **Private API**: Authenticated talk submission, review workflow, conference management
3. **Audit Trail**: Track all changes to talks and speakers
4. **Multi-tenant**: Support multiple conferences, multiple organizer domains (SystemId)
5. **Gradual Publishing**: Draft → Submitted → Approved → Published workflow

---

## Package Structure

```
no.java.moresleep/
├── Root: Application.kt, ApiServlet, HttpMethod, ServiceExecutor (HTTP layer)
├── conference/: Conference, Speaker, Session management
├── talk/: Talk commands (CRUD, publish, statistics)
└── util/: Setup, database migrations, Flyway
```

---

## Architecture Layers

### 1. Entry Point
**Application.kt**:
- `main()`: Initializes Jetty on port 8082 (configurable via SERVER_PORT)
- Mounts `/data/*` (private API) and `/public/*` (public API) to ApiServlet
- Loads config via Setup.readSetupFromEnv() or file
- Runs Flyway migrations if DO_FLYWAY_MIGRATION=true

### 2. HTTP Layer
**ApiServlet.kt**:
- Servlet dispatching GET/POST/PUT/DELETE to ServiceExecutor
- Extracts request body as JsonObject
- Returns JSON responses

**HttpMethod.kt**:
- Pattern-matches request paths to Command classes
- Examples:
  - `GET /public/allSessions/:slug` → ReadAllPublicTalks
  - `POST /data/conference/:id/session` → CreateNewSession
  - `PUT /data/session/:id` → UpdateSession
  - `POST /data/session/:id/publish` → PublishUpdates

**ServiceExecutor.kt**:
- Central request processor:
  - Parses HTTP request → Command
  - Handles Basic Auth (credentialsWithBasicAuthentication)
  - Determines UserType (ANONYMOUS, READ_ONLY, FULLACCESS, SUPERACCESS)
  - Manages per-request DB connection (thread-local, auto-commit on success)
  - Enforces command-level access control (requiredAccess)
  - Returns JsonObject response or RequestError

### 3. Command Layer (Business Logic)
All commands extend `Command` interface, execute within DB transaction. Key commands:

**Conference**:
- CreateNewConference, ReadAllConferences, UpdateConference

**Talk/Session**:
- CreateNewSession: POST talk with speakers
- ReadOneTalk: Single talk (private or public data based on auth)
- ReadAllTalks: All talks in conference (private API)
- UpdateSession: PUT talk/speaker data, change status
- PublishUpdates: Publish/unpublish (copies data → publicdata)
- ReadAllPublicTalks: Public API (cached 15min, only APPROVED/HISTORIC talks)
- ReadTalksBySubmitter: Speaker's submitted talks
- TalkUpdatesWithPayload: Audit trail

**Utilities**:
- ReadConfigCommand: Public config endpoint
- ReadLinkCommand: Shareable links

### 4. Repository Layer (Data Access)
**TalkRepo.kt**:
- addNewTalk(), aTalk(), allTalksInForConference(), publicTalksFromConference()
- updateTalk(), publishTalk(), unpublishTalk()
- registerTalkUpdate(), updatesOnTalk(), updatesWithPayloadOnTalk()

**SpeakerRepo.kt**:
- addNewSpeaker(), speakersForTalk(), updateSpeakerData()

**ConferenceRepo.kt**:
- addNewConference(), allConferences(), oneConference()
- updateSlottimes()

### 5. Domain Models

**Conference**:
- Fields: id, name, slug, slottimes (comma-separated time slots)

**Talk**:
- TalkInDb: id, conferenceid, status (SessionStatus), data (JsonObject), publicdata, publishedAt, postedBy
- SessionStatus: DRAFT, SUBMITTED, APPROVED, REJECTED, HISTORIC (only APPROVED/HISTORIC are public)
- PublicTalk: Sanitized public representation

**Speaker**:
- Speaker: id, name, email, data (Map<String, DataValue>)
- DataValue: JSON field with privateData flag (controls public visibility)

**Audit**:
- TalkUpdates, TalkUpdateWithPayload: Audit log (who, when, payload)

---

## Key Technical Patterns

### Authentication & Authorization
- **Basic Auth**: credentialsWithBasicAuthentication() extracts base64 credentials from header
- **UserType Hierarchy**: ANONYMOUS < READ_ONLY < FULLACCESS < SUPERACCESS
- **Per-Command Access**: Each Command declares requiredAccess level
- **SystemId**: Enum for different organizer domains (JAVAZONE, JAVASKOLEN)
- **Dev Mode**: ALL_OPEN_MODE=true bypasses auth (local development)

### Database & Transactions
- **Connection Management**: Thread-local Connection in ServiceExecutor, auto-commit per request
- **JSON Storage**: Talks/speakers store data as JsonObject in `data` column
- **Public/Private Fields**: DataValue.privateData flag controls field visibility
- **Publishing**: publishTalk() copies data → publicdata (sanitized)
- **Migrations**: Flyway in src/main/resources/db/migration/

### Caching
- **Public API Cache**: 15-minute TTL on ReadAllPublicTalks (reduces DB load)
- **Conditional Requests**: If-Unmodified-Since support (304 responses)

### Audit Trail
- **STORE_UPDATES flag**: Controls whether to log payloads to talkupdate table
- **registerTalkUpdate()**: Records who, when, payload (JSON diff optional)

### Slot Times
- **Conference.slottimes**: Comma-separated time slots for scheduling
- **startSlot Calculation**: For lightning talks, finds slot start time (multiple talks per slot)
- **CONFIG_SLOTS flag**: Disables slot calculations if false

---

## Database Schema

**Tables** (via Flyway):
```sql
conference (id, name, slug, slottimes)
talk (id, conferenceid, data, publicdata, status, postedby, lastupdated, publishedat, created, slottimes)
speaker (id, talkid, conferenceid, name, email, data)
talkupdate (talkid, conferenceid, updatedby, updatedat, payload)
```

**Migrations**: src/main/resources/db/migration/V*.sql

---

## API Endpoint Map

### Public (ANONYMOUS, CORS enabled)
```
GET  /public/allSessions                      → All conferences with public talks
GET  /public/allSessions/:slug                → Public talks for conference (by slug)
GET  /public/conference/:id/session           → Public talks for conference (by id)
GET  /public/config                           → Configuration
GET  /public/link/:linkkey                    → Shareable link
```

### Private (READ_ONLY or FULLACCESS required)
```
GET  /data/conference                         → All conferences
POST /data/conference                         → Create conference (FULLACCESS)
PUT  /data/conference/:id                     → Update conference (FULLACCESS)

POST /data/conference/:conferenceId/session   → Create talk (FULLACCESS)
GET  /data/session/:id                        → Single talk (private data)
GET  /data/conference/:conferenceId/session   → All talks in conference
GET  /data/conference/:conferenceId/substatistics → Submission statistics
PUT  /data/session/:id                        → Update talk (FULLACCESS)
POST /data/session/:id/publish                → Publish/unpublish (FULLACCESS)

GET  /data/submitter/:email/session           → Submitter's talks
GET  /data/fullTalkUpdate/:id                 → Audit history
```

---

## Configuration (Setup.kt)

**Environment Variables** (or file-based config):
```
SERVER_PORT: 8082
DATABASE_TYPE: POSTGRES | SQLLITE | PGINMEM
DBHOST: localhost
DBPORT: 5432
DATASOURCENAME: moresleeplocal
DBUSER / DBPASSWORD: DB credentials
ALL_OPEN_MODE: true (dev) / false (prod) - bypasses auth
ALLACCESS_USER: user:password or SystemId=password,SystemId=password
READ_USER: read-only credentials
RUN_FROM_JAR: false (dev) / true (prod)
DO_FLYWAY_MIGRATION: true
STORE_UPDATES: true (enable audit trail)
CONFIG_SLOTS: false (disable slot time calculations)
```

**Loading**: Setup.readSetupFromEnv() or pass file path as program argument

---

## Testing Patterns

**BaseTestClass.kt**:
- Sets up in-memory DB (PGINMEM or SQLite)
- Runs Flyway migrations before each test
- Creates test users (FULLACCESS, ANONYMOUS)
- Provides `doCommandForTest()` helper (simulates HTTP requests)

**Test Structure**:
- src/test/kotlin/no/java/moresleep/
  - ConferenceTest.kt
  - TalkTest.kt
  - IntegrationTest.kt
  - CompareWithSP.kt (legacy system comparison)

---

## Build & Run

**Build**: `mvn clean install` or `mvn package`
**Test**: `mvn test`
**Run Locally**:
```bash
# With Docker Postgres:
cd docker/postgres && ./build.sh && ./run.sh

# Start app:
java -jar target/moresleep-0.0.1-jar-with-dependencies.jar
# or run Application.main() from IDE
```

**Docker**: `docker-compose up` (postgres + app)

---

## Deployment

- **ElasticBeanstalk**: .ebextensions/ config
- **GitHub Actions**: .github/workflows/cdk-deploy.yml
- **Dockerfile**: Containerized app

---

## Common Tasks (LLM Decision Tree)

### Adding a New Endpoint
1. Create Command class in appropriate package (extends Command)
2. Implement `requiredAccess`, `validate()`, `execute()`
3. Add route pattern in HttpMethod.kt
4. Add test in *Test.kt using doCommandForTest()

### Adding a New Conference Field
1. Update Conference domain model
2. Add Flyway migration (src/main/resources/db/migration/)
3. Update ConferenceRepo queries
4. Update CreateNewConference / UpdateConference commands

### Adding a New Talk Field
1. Add field to talk's JSON `data` column (no schema change needed)
2. Tag as public/private via DataValue.privateData
3. Update publishTalk() sanitization logic if field is public
4. Update frontend submission form (not in this repo)

### Debugging Auth Issues
1. Check Setup.ALL_OPEN_MODE (should be false in prod)
2. Verify ALLACCESS_USER / READ_USER env vars
3. Check command's requiredAccess level
4. Test with curl: `curl -u username:password https://...`

### Performance Tuning
1. Public API: Already cached (15min TTL)
2. Private API: Add indexes on conference.slug, talk.conferenceid, talk.status
3. Audit trail: Set STORE_UPDATES=false if payload storage is too slow

---

## Gotchas & Constraints

1. **JSON Field Limits**: Talk data is JsonObject - no schema enforcement. Validate in Command.validate()
2. **Status Transition**: Only APPROVED/HISTORIC talks are public. PublishUpdates enforces this.
3. **Speaker Email as Key**: Speaker identity tied to email (case-sensitive). Normalizing emails not implemented.
4. **Slot Times**: slottimes string parsing is fragile. Format: "09:00,09:20,09:40,..."
5. **Basic Auth**: No OAuth/JWT. For production, consider API gateway with proper auth.
6. **CORS**: Public API has CORS enabled. Private API does not.
7. **Migration Reversibility**: Flyway migrations are forward-only. Test in staging first.

---

## Divergence Alerts

**If you see these patterns, alert the user**:
- Adding endpoints outside Command pattern (violates architecture)
- Direct SQL in Commands (should use Repo layer)
- Bypassing requiredAccess checks (security issue)
- Modifying publicdata directly (should use publishTalk())
- Skipping Flyway for schema changes (breaks migration tracking)
- Hardcoding credentials (should use Setup env vars)

**Tests as Gold Standard**:
- If test fails after change, fix the code OR prove test is wrong
- Never skip tests (set @Disabled with justification)
- Never mock Repository layer in integration tests (use in-memory DB)
