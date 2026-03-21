# Project Reconnaissance: sleeping_pill_moresleep

## 1. Codebase Statistics

| Category | Count |
|----------|-------|
| Kotlin source files (main) | 44 |
| Kotlin test files | 6 |
| SQL migration files | 5 |
| XML config files (pom.xml) | 1 |
| Total Kotlin files | 50 |
| Feature directories | 4 (core, conference, talk, util) |

## 2. Architecture Overview

| Layer | Key Files | Pattern |
|-------|-----------|---------|
| **Entry** | Application.kt, ApiServlet.kt | Jetty Server + Servlet routing to `/data` and `/public` endpoints |
| **Routing** | HttpMethod.kt, ServiceExecutor.kt | Enum-driven path pattern matching with parameter extraction |
| **Command Layer** | Command.kt interface, 14 command implementations | Command pattern: each endpoint = Command subclass |
| **Repository** | TalkRepo.kt, ConferenceRepo.kt, SpeakerRepo.kt | Direct SQL via ServiceExecutor.connection() |
| **Data** | Database.kt, Setup.kt | HikariCP pooling; Flyway migrations; PostgreSQL/SQLite/PgInMem support |
| **Auth** | ServiceExecutor.kt lines 38-123 | Basic HTTP Auth -> SystemUser (UserType, SystemId) -> role-based access check |
| **Response** | ServiceResult hierarchy, JsonBuddy | JsonObject serialization via JsonGenerator.generate() |

**Dependency flow:** Request -> ApiServlet -> ServiceExecutor.doStuff() -> HttpMethod route -> Command.execute() -> Repo -> DB

## 3. Module/Feature Inventory

| Module | Entry Point | Description |
|--------|-------------|-------------|
| **Conference Management** | CreateNewConference, ReadAllConferences, UpdateConference | Create/read/update JavaZone conference metadata (name, slug, slot times) |
| **Talk/Session Management** | CreateNewSession, UpdateSession, ReadOneTalk, ReadAllTalks | CRUD for presentation proposals; metadata stored in JSON; status enum (DRAFT, SUBMITTED, APPROVED, REJECTED, HISTORIC) |
| **Speaker Management** | Speaker, SpeakerRepo, SpeakerUpdate | Associate speakers with talks; stores name, email, bio data as Map<String, DataValue> |
| **Publication Workflow** | PublishTalk, PublishUpdates, ReadAllPublicTalks | Transition sessions to public; stores publicdata separately from private data |
| **Audit Trail** | TalkRepo.registerTalkUpdate, TalkUpdates | Logs all talk updates (by user, timestamp, optional payload) |
| **Public API** | ReadAllPublicTalks, ReadLinkCommand, ReadConfigCommand | Anonymous-accessible endpoints for conference schedule & configuration |
| **Data Versioning** | DataValue (privateData: Boolean) | Tracks which JSON fields are public vs. private; used during publish |
| **Database** | Database.kt, Flyway migrations | Connection pooling, schema versioning, multi-database support (PostgreSQL, SQLite, PgInMem) |
| **Configuration** | Setup.kt enum + loadFromFile/loadFromEnvironment | Server port, DB credentials, auth mode, Flyway control flags |
| **Testing Harness** | BaseTestClass, doCommandForTest | Mock HttpServletRequest/Response; per-test DB isolation via Flyway clean |

## 4. API / Endpoint Inventory

| Endpoint | Method | Handler File |
|----------|--------|--------------|
| `/data/conference` | GET | ReadAllConferences.kt |
| `/data/conference` | POST | CreateNewConference.kt |
| `/data/conference/:conferenceId` | PUT | UpdateConference.kt |
| `/data/conference/:conferenceId/session` | GET | ReadAllTalks.kt |
| `/data/conference/:conferenceId/session` | POST | CreateNewSession.kt |
| `/data/conference/:conferenceId/substatistics` | GET | TalkSubmissionStatistics.kt |
| `/data/session/:id` | GET | ReadOneTalk.kt |
| `/data/session/:id` | PUT | UpdateSession.kt |
| `/data/session/:id/publish` | POST | PublishUpdates.kt |
| `/data/fullTalkUpdate/:id` | GET | TalkUpdatesWithPayload.kt |
| `/data/submitter/:email/session` | GET | ReadTalksBySubmitter.kt |
| `/public/allSessions` | GET | ReadAllConferences.kt (reused) |
| `/public/allSessions/:slug` | GET | ReadAllPublicTalks.kt |
| `/public/conference/:id/session` | GET | ReadAllPublicTalks.kt |
| `/public/config` | GET | ReadConfigCommand.kt |
| `/public/link/:linkkey` | GET | ReadLinkCommand.kt |

## 5. Data & Integration Patterns

| Pattern | Where Used | Files |
|---------|-----------|-------|
| **JSON Storage** | Talk.data, Talk.publicdata, Speaker.data fields | TalkRepo.kt, SpeakerRepo.kt; JsonBuddy JsonObject |
| **JDBC PreparedStatements** | All repo queries | DbConnection.kt interface; ServiceExecutor connection pooling |
| **Thread-local Transactions** | Per-request isolation | ServiceExecutor.connectionsUsed ConcurrentHashMap keyed by Thread.currentThread().id |
| **Audit Log** | talkupdate table | TalkRepo.registerTalkUpdate() called on every mutation |
| **Caching** | PublicTalks read service | PublicTalkReadService.allConferences lazy load with manual clearCache() |
| **Migration Versioning** | Schema bootstrap & evolution | Flyway; V1 (base), V2 (created column), V4 (talkupdate), V6 (slottimes), V7 (payload) |

## 6. Dependency & Build Overview

| Aspect | Details |
|--------|---------|
| **Build Tool** | Maven 4.0.0; Kotlin 1.7.20; Java 8 target |
| **Web Framework** | Jetty 9.4.30 (servlet-based, not Spring) |
| **Database** | PostgreSQL 42.2.8 driver; HikariCP 3.4.5 pooling; Flyway 6.1.0 migrations |
| **JSON** | JsonBuddy 0.17 (custom serializer; PojoMapper for request payloads) |
| **Logging** | SLF4J 1.7.25 + Logback 1.2.1 |
| **Test Framework** | JUnit 5.3.2 + Mockito 2.23.0 + AssertJ 3.11.1 |
| **Test DB** | SQLite 3.16.1 (in-memory) for unit tests; supports PostgreSQL for integration |
| **Packaging** | maven-assembly-plugin (fat JAR with dependencies) |

## 7. Knowledge Gap Analysis

### Well-documented / easy to understand:
- HTTP routing & command dispatch (HttpMethod enum pattern is explicit)
- CRUD operations (all repos follow standard prepared statement pattern)
- Domain models (SessionStatus, DataValue, Speaker, Conference structures are clear)
- Test infrastructure (BaseTestClass provides good template)
- Configuration system (Setup enum + environment override is straightforward)

### Resolved (dedicated docs in kcp/, 2026-03-21):
- **Transaction semantics** → `kcp/transaction-model.md` — Thread-local isolation, commit/rollback semantics, gotchas
- **Authorization model** → `kcp/authorization-model.md` — UserType ordinal comparison, SystemUser resolution, response codes
- **PublicTalk serialization + Data privacy** → `kcp/publish-filtering.md` — toPublicMap() filtering, DataValue.privateData, publish flow
- **PublicTalkReadService caching** → `kcp/public-cache-invalidation.md` — 15-min TTL, clearCache(), If-Modified-Since

### Kept as source pointers (small, self-explanatory):
- **Error handling** → `RequestError.kt` (10 lines) — BadRequest(400) + ForbiddenRequest(403) hierarchy
- **Data privacy flag** → `DataValue.kt` (8 lines) — covered by publish-filtering doc

### Not documented (low value):
- **ReadLinkCommand** — Stub returning hardcoded "https://www.java.no". No real linkkey resolution implemented.

## 8. Project Quirks & Warnings

- **Basic Auth only** — No OAuth/JWT support; credentials baked into Authorization header
- **Thread-local transactions** — Risky; no automatic rollback on exception; relies on calling code to invoke closeConnection() in finally block
- **Path parameter parsing** — Custom regex-free character-at-a-time state machine (HttpMethod.mapFromPath) — works but fragile
- **No foreign key constraints** — Database tables have no explicit FK references; referential integrity enforced in code only
- **Ktor in pom but unused** — Ktor dependency declared (v1.4.1) but codebase uses Jetty servlets, not Ktor routing
- **Flyway clean() in tests** — BaseTestClass calls flyway.clean() which drops all tables; risky if test setup fails mid-execution
- **JsonBuddy PojoMapper** — Automatic JSON-to-Kotlin mapping; fragile to property name mismatches or type mismatches
- **No content negotiation** — Hardcoded `application/json`; no support for XML, CSV, etc.
- **Setup.isRunningJunit flag** — Tests set a static boolean to flag test mode; not thread-safe if tests run in parallel
- **Two talks named ReadOneTalk vs ReadOneSession** — File uses different name in path vs. class; confusing naming
- **Missing V3 and V5 migrations** — Flyway version sequence jumps (V1, V2, V4, V6, V7); V3 and V5 skipped or deleted
