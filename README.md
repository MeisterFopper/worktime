# WorkTime — Work & Project Time Tracking

WorkTime is a time tracking web application consisting of:

- **Backend:** Java 21, Spring Boot (REST API + Swagger UI)
- **Frontend:** Vue 3 (Composition API) + Vite (Single-Page Application)

This repository is organized as a **monorepo**: backend and frontend live in separate folders but are versioned and released together.

## Repository structure

- `backend/` — Spring Boot backend (Maven)
  - Backend documentation: `backend/README.md`
  - Architecture & decisions: `backend/ARCHITECTURE.md`
- `frontend/` — Vue 3 / Vite frontend
  - Frontend documentation: `frontend/README.md`
  - Architecture & migration notes: `frontend/ARCHITECTURE.md`
- `server/` — Production deployment & operations (Nginx + systemd)
  - Deployment guide: `server/README.md`

## Prerequisites

- Java 21
- Node.js 18+ and npm

## Quickstart (local development)

From the repository root, run both components in separate terminals.

**Terminal 1 — backend**
```bash
cd backend
./mvnw spring-boot:run
```

**Terminal 2 — frontend**
```bash
cd frontend
npm install
npm run dev
```

Vite prints the local URL in the terminal (commonly `http://localhost:5173`).

## Documentation

- Backend: `backend/README.md`
- Backend architecture: `backend/ARCHITECTURE.md`
- Frontend: `frontend/README.md`
- Frontend architecture: `frontend/ARCHITECTURE.md`
- Deployment & operations: `server/README.md`

## Configuration overview

- Backend configuration: `backend/src/main/resources/application.yml` (details in `backend/README.md`)
- Frontend configuration / API wiring: `frontend/vite.config.js` (details in `frontend/README.md`)
- Secrets (DB credentials, etc.) should be provided via environment variables or a secret manager.

## License

MIT (see `LICENSE`, if present).
