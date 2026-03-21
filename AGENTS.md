# AGENTS.md

Session register REST API for JavaZone conference talks. Kotlin / Jetty / PostgreSQL.

## Getting Started

This project uses [KCP (Knowledge Context Protocol)](https://github.com/Cantara/knowledge-context-protocol) for structured agent knowledge.

**Start here:** Read `knowledge.yaml` at project root. It indexes all project knowledge with intent, triggers, and load strategy.

```
knowledge.yaml          # KCP manifest — load this first
├── kcp/OVERVIEW.md     # Architecture, endpoints, modules (eager-load)
├── CLAUDE.md           # Conventions, constraints, quick-start (eager-load)
└── kcp/*.md            # Deep-dive docs on specific subsystems (lazy-load)
```

Eager-load units (~4100 tokens) give you full project orientation. Lazy units load on-demand via keyword triggers defined in the manifest.

## Dev Environment

```bash
# Postgres (required for local run)
cd docker/postgres && ./build.sh && ./run.sh

# Build & test
mvn clean install

# Run locally (port 8082)
java -jar target/moresleep-0.0.1-jar-with-dependencies.jar
```

## Testing

```bash
mvn test
```

Tests use in-memory DB (SQLite/PgInMem) with Flyway migrations. No external dependencies needed.

## Validation

```bash
make kcp-validate    # Verify all knowledge.yaml paths resolve
```

## Architecture (Quick Reference)

- **Command pattern**: Each endpoint = one Command class
- **Routing**: `HttpMethod.kt` maps URL patterns to Commands
- **Auth**: Basic Auth, `UserType` enum ordinal comparison (ANONYMOUS < READ_ONLY < FULLACCESS < SUPERACCESS)
- **Data**: JSON blobs in PostgreSQL, Flyway migrations
- **Caching**: Public API cached 15 min, manual `clearCache()`

For full architecture: load `codebase-architecture` unit from `knowledge.yaml` (triggers: architecture, command pattern, database, "add endpoint").

## Key Constraints

- Add routes in `HttpMethod.kt` only (Command pattern)
- SQL belongs in `*Repo.kt` classes, never in Commands
- Never bypass `requiredAccess` checks
- Never modify `publicdata` directly — use `publishTalk()`
- Schema changes require Flyway migrations
- Tests use real in-memory DB — never mock the Repository layer

## PR Guidelines

- Run `mvn test` before committing
- One logical change per commit
- Commit format: `type: description` (feat, fix, docs, refactor, test)

## Skills & Knowledge Docs

| Topic | Location | When to Load |
|-------|----------|-------------|
| Full architecture | `.claude/skills/moresleep-codebase.md` | Adding features, understanding patterns |
| Transaction model | `kcp/transaction-model.md` | Working with DB connections |
| Auth model | `kcp/authorization-model.md` | Auth issues, adding secured endpoints |
| Publish filtering | `kcp/publish-filtering.md` | Modifying public/private data flow |
| Cache invalidation | `kcp/public-cache-invalidation.md` | Performance, caching questions |
| Lessons & gotchas | `LEARNINGS.md` | Before making assumptions |
