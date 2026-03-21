# Transaction & Thread-Local Model

## How Transactions Are Isolated Per HTTP Request

Moresleep uses **thread-local connection storage** to isolate database transactions per HTTP request. Each servlet thread gets its own database connection stored in a `ConcurrentHashMap<Long, MyDbConnection>` keyed by thread ID.

### Connection Lifecycle

```kotlin
// ServiceExecutor.kt
@Synchronized fun createConnection(): DbConnection {
    val threadId = Thread.currentThread().id
    connectionsUsed[threadId]?.let {
        throw MoresleepInternalError("Transaction error. Connection already open")
    }
    val connection = Database.connection()
    connection.autoCommit = false  // Manual transaction control
    val dbConnection = MyDbConnection(connection)
    connectionsUsed[threadId] = dbConnection
    return dbConnection
}
```

**Key invariant**: Only one connection per thread. Attempting to create a second connection on the same thread throws an error.

### Commit/Rollback Semantics

The standard request flow in `doStuff()` is:

```kotlin
createConnection().use {
    val res = command.execute(usertype, parameters)
    commit()  // Explicit commit on success
    res
}
```

**On success**: `commit()` calls `connection.commit()` for the current thread's connection.

**On error**: The `.use {}` block triggers `closeConnection()`, which:
1. Rolls back: `connection.rollback()`
2. Closes connection: `connection.close()`
3. Removes thread-local entry

**Critical detail**: `closeConnection()` ALWAYS rolls back before closing, regardless of success/failure. This means any uncommitted changes are automatically rolled back when the connection is closed.

### Thread Safety

- `createConnection()` is `@Synchronized` to prevent race conditions
- `ConcurrentHashMap` ensures thread-safe storage
- Each HTTP request runs in its own servlet thread with isolated transaction state

### Gotchas

- Commands accessing the database outside `doStuff()`'s transaction scope will fail with "No found connection"
- Manual transaction management (no ORM automatic flush)
- Connection must be explicitly closed via `.use {}` or `closeConnection()` - otherwise thread-local map leaks entries
