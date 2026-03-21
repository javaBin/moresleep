# Public Talk Cache Invalidation

## Overview

`PublicTalkReadService` maintains an in-memory cache of published talks with automatic time-based invalidation. The cache prevents repeated database queries for frequently accessed public endpoints.

## Cache Structure

- **Storage**: `ConcurrentHashMap<String, AllPublicTalks>` keyed by conference ID
- **Cache Entry**: Contains `JsonArray` of talks + `lastModified` timestamp + `readAt` timestamp
- **Concurrency**: Thread-safe using `ConcurrentMap`

## Invalidation Strategy

### Time-Based (Automatic)
Cache entries expire after 15 minutes from read time. Check at line 69:
```kotlin
if (cachedTalks.readAt.plusMinutes(15).isAfter(LocalDateTime.now())) {
    return cachedTalks // still valid
}
```

If expired, fresh data is fetched from `TalkRepo.publicTalksFromConference()` and cache is updated.

### Manual (Explicit)
`clearCache()` method wipes entire cache immediately. Called after publish operations to ensure consistency.

## If-Modified-Since Support

The service supports HTTP conditional requests (line 52-63):
- Client sends `If-Unmodified-Since` header
- Service compares against cached `lastModified` (max of all talk timestamps)
- If content hasn't changed, throws `SC_PRECONDITION_FAILED` (304 semantics)

## Access Patterns

- `readAllPublicTalksById(conferenceId)` - lookup by UUID
- `readAllPublicTalksBySlug(slug)` - lookup by conference slug (e.g., "javazone_2024")

Both resolve to `allTalks()` which handles cache logic.

## Key Files

- `/src/main/kotlin/no/java/moresleep/talk/PublicTalkReadService.kt` (lines 27-80)
