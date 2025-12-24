# WorkTime Backend

The WorkTime backend is a Spring Boot REST API for tracking:

- **Work sessions** (start/stop your workday)
- **Work segments** within a session (time spent on an activity within a category)
- Optional **comments** on segments

API documentation is available via **Swagger UI**.

## Tech stack

- Java 21
- Spring Boot (Web, Validation, Data JPA, Cache, Actuator)
- MariaDB / H2 (dev/test depending on profile)
- MapStruct
- springdoc-openapi (Swagger UI)

## Prerequisites

- Java 21
- A supported database (optional for local dev if using an in-memory profile)

## Run (development)

From `backend/`:

```bash
./mvnw spring-boot:run
```

Swagger UI (depending on springdoc version):  
`http://localhost:8080/swagger-ui/index.html`

OpenAPI JSON:  
`http://localhost:8080/v3/api-docs`

## Build

From `backend/`:

```bash
./mvnw clean package
```

The JAR will be created in `backend/target/`.

## Configuration

Backend configuration is driven primarily through `src/main/resources/application.yml` plus profile-specific overrides.

Common configuration topics:

- Datasource URL / username / password
- Hibernate / Flyway (if enabled)
- CORS / allowed origins (for local SPA development)
- Cache configuration (if enabled)

For production deployment and operational configuration (systemd, Nginx, TLS), see:  
`../server/README.md`

## Project structure (high level)

- `controller` — HTTP contract only (routes, request/response DTOs)
- `service` — business rules, transactions, orchestration
- `persistence` — repositories, queries, entity retrieval helpers
- `domain` — domain types and exceptions
- `mapper` — MapStruct mappers (DTO ↔ entity)

See detailed layering rules and conventions in:  
`./ARCHITECTURE.md`

## API overview

The authoritative API description is the OpenAPI spec exposed by the running server.

Typical resource groups include:

- Categories (CRUD, activate/deactivate)
- Activities (CRUD, activate/deactivate)
- Work sessions (start/stop, current session)
- Work segments (start/stop, list by session)
- Reports (aggregations; varies by implementation)

For a complete and up-to-date list, use Swagger UI:
`http://localhost:8080/swagger-ui/index.html`

## See also

- Architecture & error-handling decisions: `./ARCHITECTURE.md`
- Deployment & operations: `../server/README.md`

## License

MIT (see repository root).
