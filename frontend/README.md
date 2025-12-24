# WorkTime Frontend

The WorkTime frontend is a Vue 3 single-page application (SPA) that consumes the WorkTime backend REST API.

## Tech stack

- Vue 3 (Composition API)
- Vite
- Vue Router
- Bootstrap 5 + Bootstrap Icons

## Prerequisites

- Node.js 18+ and npm
- A running WorkTime backend (local or remote)

## Install

From `frontend/`:

```bash
npm install
```

## Run (development)

From `frontend/`:

```bash
npm run dev
```

Vite prints the local URL in the terminal (commonly `http://localhost:5173`).

### API / proxy configuration

The dev setup typically proxies API requests to the backend. Review and adjust:

- `vite.config.js` (proxy rules and/or base path)
- any environment variables used for API base URLs

Ensure the backend is reachable (commonly `http://localhost:8080`).

## Build

From `frontend/`:

```bash
npm run build
```

Output is created in `frontend/dist/`.

## Lint / tests

Use the scripts defined in `package.json` (e.g., `npm run lint`, `npm run test`) if present in your project.

## Troubleshooting

- If the UI loads but API calls fail, verify:
  - backend is running and reachable
  - CORS settings (if not using a proxy)
  - Vite proxy configuration
- If building for a sub-path (e.g., `/worktime/`), ensure Vite `base` is set appropriately before running `npm run build`.

## See also

- Frontend architecture & migration notes: `./ARCHITECTURE.md`
- Deployment & operations: `../server/README.md`

## License

MIT (see repository root).
