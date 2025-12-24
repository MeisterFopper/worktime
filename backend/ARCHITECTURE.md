# WorkTime Architecture and Error-Handling Decisions

This document summarizes the current strategy and architecture decisions for the WorkTime project.

## Layering and responsibilities

### Controller layer

**Owns**
- HTTP contract only: routes, request/response types, status codes (e.g., `201 CREATED` on create/start).
- Validation trigger points (`@Valid`, `@Min`, etc.).
- No business rules and no persistence logic.

**Does not own**
- Logging and exception translation (delegated to exception handling strategy below).

### Service layer

**Owns**
- Input normalization and validation (e.g., `StringSanitizer`, blank checks, “no fields to update”).
- Business rules and orchestration (e.g., “cannot start if already running”, “cannot stop session while segment is open”).
- Reference resolution across aggregates (e.g., load `WorkSessionEntity`/`CategoryEntity`/`ActivityEntity` to set relationships).
- Transaction boundary (`@Transactional` on service methods).
- Caching policy (`@Cacheable` for reads, `@CacheEvict` for writes).
- Technical exception translation (e.g., `DataIntegrityViolationException` → domain exceptions).

**Does not own**
- Direct repository calls (use persistence layer for DB primitives; orchestration may use explicit resolvers/repositories only when needed and deliberate).
- Logging.

### Persistence layer

**Owns only**
- Database operations (queries, locking queries, save, delete).
- Locking helpers: `lockCurrentForUpdate()`, `lockByIdForUpdate()`.
- Entity retrieval helpers (e.g., `getByIdOrThrow` / `lockByIdForUpdate` throwing domain `NotFoundException`).
- Mapping boundaries: entity ↔ request/response mapping via MapStruct.
- Flush boundary helpers (e.g., `saveAndFlush`) so constraint violations are raised inside the service call.

**Does not own**
- Use-case methods (avoid names like `startNow`, `stopNow`, `create`, `patch` if they implement a full workflow).
- Transaction and caching annotations.
- Logging.

## Request/Response model strategy

### Packages and naming

- **Requests** live in `com.mrfop.worktime.model.request` and are named `*Request`.
- **Responses** live in `com.mrfop.worktime.model.response` and are named `*Response`.
- The former `model.dto` package is being phased out (no “internal DTO” layer for now).

### “View DTO” policy

- We are standardizing on **response models** (`*Response`) that match API needs.
- Separate “View DTOs” are only justified if:
  - the API needs multiple materially different projections of the same resource (different fields and semantics), and
  - we want to keep those projections explicit and stable.
- Current direction: **prefer one response type per endpoint family**, to keep Swagger/OpenAPI consistent and reduce confusion.

### Duration fields

- Decision: do **not** return `durationMs` on work-session/work-segment endpoints (UI computes if needed).
- Reporting endpoints may still compute aggregate durations if the endpoint’s purpose is reporting/summary.

## Exceptions and error handling

### Exception model

**Primary model: `DomainException` + `ErrorCode` + `Subject`**
- All business/domain failures that the API should expose are represented as subclasses of `DomainException`.
- `DomainException` carries:
  - `ErrorCode code` (machine-readable)
  - `Subject resource` (domain object/aggregate)
  - message (human-readable; stable, built via `ExceptionMessages`)
- Exceptions may optionally carry structured context for API clients and logs, e.g.:
  - `field` / `value` (for lookup and validation)
  - `start` / `end` (for time range issues)

**Do not rely on `@ResponseStatus` for domain exceptions**
- Domain exceptions are mapped to HTTP status codes centrally in `GlobalExceptionHandler` via `CODE_TO_STATUS`.
- `@ResponseStatus` is treated as a **legacy escape hatch** (only for non-domain exceptions or transitional classes) and is supported by the generic fallback resolver.

### Error taxonomy

We explicitly separate failures into **validation**, **not found**, and **conflict**, because they imply different client actions.

**400 Bad Request**
- `VALIDATION_FAILED`: invalid input (blank name, invalid parameter, etc.)
- `NO_FIELDS_TO_UPDATE`: PATCH request had no meaningful fields
- `INVALID_TIME_RANGE`: `end < start`
- `MISSING_TIME_VALUE`: required time value missing (closed range requires both start and end)

**404 Not Found**
- `NOT_FOUND`: resource not found by ID/name/etc.

**409 Conflict**
- `NAME_ALREADY_EXISTS`: business-rule precheck for uniqueness (friendly error)
- `ALREADY_RUNNING`: invariant violation (“only one open session/segment”)
- `NO_ACTIVE`: invariant violation (“expected an active session/segment”)
- `OPERATION_BLOCKED_BY_RUNNING`: dependency rule (“cannot stop X while Y is running”)
- `CONFLICT`: generic domain-level conflict (often from DB constraints / races)

### Service-level exception translation

- Services translate technical persistence exceptions into domain exceptions:
  - `DataIntegrityViolationException` → `ConflictException(Subject, cause)`
- Services may use pre-checks (`existsBy…`) to provide friendly error messages, but **must not** assume pre-checks prevent races.
  - The database remains the source of truth, so the conflict fallback remains mandatory.

### Global exception handling and response shape

**Single response format: `ProblemDetail` (RFC 7807)**
- All errors returned by the API are `ProblemDetail` with consistent extensions:
  - `code` (string; `ErrorCode` name for domain errors; non-domain constants for framework errors)
  - `subject` (string; `Subject` name, when applicable)
  - `timestamp` (string)
  - optional: `field`, `value`, `errors[]`, `start`, `end`, etc.

**Centralized status mapping**
- `DomainException` → status from `CODE_TO_STATUS`.
- Bean validation / HTTP input problems → `400` with `code = VALIDATION_FAILED`:
  - `MethodArgumentNotValidException`
  - `MethodArgumentTypeMismatchException`
  - `MissingServletRequestParameterException`
  - `HttpMessageNotReadableException`
- Method not allowed → `405` with `code = METHOD_NOT_ALLOWED`.
- `ResponseStatusException` → status with `code = HTTP_<status>`.
- Unhandled errors → `500` with `code = INTERNAL_ERROR` and a non-leaking message.

**Centralized logging policy**
- No logging in service/persistence layers.
- `GlobalExceptionHandler` logs:
  - `5xx`: error level with stack trace
  - `4xx`: warn level (typically without stack trace)

### Structured validation errors

**Bean validation errors**
- `MethodArgumentNotValidException` returns:
  - `code = VALIDATION_FAILED`
  - `errors = [{ field, message }, ...]`

**Domain validation errors**
- `ValidationException` returns:
  - `code = VALIDATION_FAILED`
  - optional `field` and `value` when meaningful

Policy note for `value`:
- For “blank name” the rejected value is often empty/whitespace; that can be useful for diagnostics but can also be noisy.
- Recommendation: only include `value` when it adds clarity (your handler already omits it when `null`).

### OpenAPI / Swagger documentation of errors

- Error responses are documented via reusable OpenAPI components:
  - `BadRequestProblem`, `NotFoundProblem`, `ConflictProblem`, `InternalErrorProblem`
  - schema: `ApiProblem` (documented ProblemDetail + extensions model)
- Controllers use `@ApiResponses(ref = "#/components/responses/...")` at class/method level to avoid repetition.
- Optional enhancement: attach named examples to reusable responses (e.g., `NOT_FOUND`, `NAME_ALREADY_EXISTS`, `ALREADY_RUNNING`) so Swagger UI displays concrete payloads.

## Concurrency and “DB as source of truth”

- Application-level checks such as `existsBy…` provide friendly errors but do **not** prevent races.
- The database constraint is the final enforcement (“source of truth”), so the DB-exception fallback remains necessary:
  - `DataIntegrityViolationException` → `ConflictException(Subject, cause)`.
- Use pessimistic locking where needed for “only one open” invariants:
  - `lockCurrentForUpdate()` for current open session/segment.
  - `lockByIdForUpdate()` for patch operations.
- Use `saveAndFlush` (or explicit `flush`) so constraint violations occur **inside** the service method and can be translated reliably.

## Patch strategy: validate-before-flush

- For PATCH flows we prefer the “clean” sequence:
  1. Load and lock entity.
  2. Apply patch **in-memory** (no DB write yet), typically via `persistence.applyPatch(...)` (MapStruct).
  3. Resolve relationship updates explicitly in the service (category/activity/session references).
  4. Validate the final in-memory entity state (e.g., `TimeUtil.requireValidOpenRange(...)`).
  5. Persist via `saveAndFlush(...)`.
- This prevents flushing invalid state to the database and aligns exception behavior with business validation.

## Mapping (MapStruct) rules

- `@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)` for compile-time safety.
- For PATCH: `@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)`.
- Services control relationship fields and critical timestamps explicitly:
  - WorkSegment relationships (`workSession`, `category`, `activity`) are ignored by the mapper and set by the service.
  - For explicitness, some fields (e.g., `comment`, `startTime`, `endTime`) may be intentionally ignored in `toNewEntity` and set in the service.
- Mappers provide:
  - `toResponse(...)` for entity → response model mapping.
  - `applyPatch(...)` for request → entity patching (in-memory).

## Entities (Lombok and JPA hygiene)

- Remove Lombok `@ToString` from JPA entities unless there is a specific, controlled reason.
  - Avoid accidental lazy-loading, recursion, and noisy logs.
- Keep entities focused on persistence concerns (constraints, relationships, auditing).

## Repository conventions

- Repository methods are documented with short Javadoc blocks (what they do and why they exist).
- Locking queries are explicitly documented as concurrency control primitives.

## Caching policy

- Use `@Cacheable` on **service** read methods.
- Use `@CacheEvict(allEntries = true)` on **service** write methods that can change cached results.
- Keep cache keys explicit and stable (e.g., `'current'`, `'all'`, or status-based keys).

## Reporting strategy

- Reporting endpoints are allowed to return nested response objects (day → sessions → segments).
- Current implementation may load broad sets and filter in memory; optimization (query methods/specs) is deferred until there is evidence of scale/performance need.

---

## Current operating rule set (short)

1. **No logs** in services/persistence/mappers.
2. Services **validate + orchestrate** and define **transaction + caching** boundaries.
3. Persistence does **DB operations + mapping only**.
4. Convert technical exceptions to domain exceptions; let global handlers decide logging and HTTP responses.
5. Prefer **validate-before-flush** for patch operations.
6. Standardize on `model.request` + `model.response`; avoid “internal DTOs” until a real need appears.
