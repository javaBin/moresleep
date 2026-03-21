# Authorization Model

## UserType Hierarchy & Ordinal Comparison

Moresleep uses **enum ordinal comparison** for authorization checks. Access levels are defined by enum order:

```kotlin
enum class UserType {
    ANONYMOUS,      // ordinal = 0
    READ_ONLY,      // ordinal = 1
    FULLACCESS,     // ordinal = 2
    SUPERACCESS     // ordinal = 3
}
```

### Authorization Logic

Every `Command` declares `requiredAccess: UserType`. Access is granted using **less-than comparison**:

```kotlin
// ServiceExecutor.kt line 162
if (systemUser.userType < command.requiredAccess) {
    if (systemUser.userType == UserType.ANONYMOUS) {
        throw RequestError(SC_UNAUTHORIZED, "Unauthorized")  // 401
    } else {
        throw ForbiddenRequest("Not required access")  // 403
    }
}
```

**This means**: A user's `UserType` must have an ordinal **greater than or equal to** the command's `requiredAccess`.

### Why Ordinal Comparison?

Kotlin enums have implicit ordinal values based on declaration order. Comparing `userType < requiredAccess` naturally enforces the hierarchy without explicit privilege mappings. Higher ordinals = more access.

### User Resolution

Users are resolved from Basic Auth credentials:

1. **ALL_OPEN_MODE**: Override all auth, grant `FULLACCESS` to everyone
2. **No credentials**: `ANONYMOUS` with `SystemId.ANONYMOUS`
3. **READ_USER match**: `READ_ONLY` with `SystemId.READ_ONLY_SYSTEM`
4. **ALLACCESS_USER match**: `FULLACCESS` with configured `SystemId` (SUBMITIT, CAKE, MORESLEEP_ADMIN, etc.)
5. **No match**: Throw `ForbiddenRequest("Unknown authorization")`

### SystemUser vs SystemId

- **SystemUser**: Combines `UserType` (privilege level) + `SystemId` (identity) + credentials
- **SystemId**: Identifies which external system is making the request (SUBMITIT, CAKE, SPACECAKE, etc.)

Multiple external systems can have the same `UserType` but different `SystemId`. This enables both authorization (UserType) and audit logging (SystemId).

### Response Codes

- **401 Unauthorized**: `ANONYMOUS` user attempting privileged operation
- **403 Forbidden**: Authenticated but insufficient privilege level
