# Moresleep

Session register for JavaZone conference talks. Replaces legacy SleepingPillCore.

**Type:** REST API (Public + Private)
**Language:** Kotlin
**Build:** `mvn clean install` / `mvn test`

---

## Quick Start

```bash
# Setup Postgres
cd docker/postgres && ./build.sh && ./run.sh

# Build & Test
mvn clean install

# Run locally (port 8082)
java -jar target/moresleep-0.0.1-jar-with-dependencies.jar
# or: Run Application.main() from IDE
```

---

## Key Conventions

**Architecture:**
- Command pattern: Each endpoint = one Command class
- Repository layer: All DB access through *Repo.kt classes
- JSON storage: Talk/speaker data in JsonObject columns
- Auth: Basic Auth (ANONYMOUS < READ_ONLY < FULLACCESS < SUPERACCESS)

**Naming:**
- Commands: VerbNounCommand.kt (e.g., CreateNewSession, ReadAllPublicTalks)
- Tests: *Test.kt, extend BaseTestClass

**API Structure:**
- Public API: `/public/*` (no auth, CORS enabled, cached 15min)
- Private API: `/data/*` (requires Basic Auth)

---

## What NOT To Do

- Never bypass Command pattern (add routes in HttpMethod.kt)
- Never put SQL in Commands (use Repository layer)
- Never skip requiredAccess checks
- Never modify publicdata directly (use publishTalk())
- Never skip Flyway for schema changes
- Never hardcode credentials (use Setup.kt env vars)
- Never mock Repo layer in tests (use in-memory DB via BaseTestClass)

---

## Skills

**Codebase Architecture**: `.claude/skills/moresleep-codebase.md`
→ Comprehensive package structure, API endpoints, database schema, technical patterns

**SDD Methodology** (global): `~/.claude/skills/common/`
- Entry point: `sdd-context`
- Methodology: `claude-md-guidelines`, `skill-routing-decisions`

---

## Related

- [README.md](./README.md): Public API examples, field descriptions
- [LEARNINGS.md](./LEARNINGS.md): Project-specific lessons and gotchas
- Production API: https://sleepingpill.javazone.no/public/allSessions
