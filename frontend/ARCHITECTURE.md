# WorkTime Frontend — Architecture, Migration Notes, and Conversation Context

> Note: The operational “how to run/build” instructions live in `README.md`.
> This document focuses on architecture, migration notes, and design conventions.


## Project Status
This project is an ongoing migration from a Thymeleaf + vanilla JS frontend
to a Vue 3 (Composition API) single-page application.

This README is intended as a **conversation context template** for future chats.

---

## Tech Stack
- Vue 3 (Composition API)
- Vite
- Vue Router
- Bootstrap 5 + Bootstrap Icons
- REST backend (Java / Spring)
- No Vuex/Pinia (local composables instead)

---

## High-Level Architecture

### Layering
- **app/**: application wiring (router, main, layout shell)
- **features/**: business features (pages + components + composables + feature APIs)
- **shared/**: cross-cutting modules (UI kit, API client, styles, utilities)

Pages are thin containers that:
- wire feature APIs + modals + toasts
- delegate logic to composables
- render via reusable components

---

## Routing & Page Caching (Keep-Alive)

Routes can set `meta.keepAlive = true`. `AppLayout.vue` wraps those routes in `<keep-alive>`.

Verified behavior:
- first visit loads normally
- returning to the tab is smooth (no hard reset / reduced flashing)

---

## Unified “Taxonomy” Feature (Categories + Activities)

Categories and Activities are treated as **taxonomy kinds** and rendered by a single page:

- `/categories` → `TaxonomyPage` with `props: { kind: 'categories' }`
- `/activities` → `TaxonomyPage` with `props: { kind: 'activities' }`

Configuration lives in `features/taxonomy/taxonomy-config.js`:
- maps `kind -> { endpoint, title, labels }`

API uses a generic factory (`createTaxonomyApi(baseUrl)`), so adding another taxonomy kind is config-only.

---

## Reusable Taxonomy CRUD Pattern

Pattern:
`TaxonomyPage.vue → useTaxonomyPage → TaxonomyTable.vue`

### useTaxonomyPage (generic CRUD composable)
Responsibilities:
- load all items once (status=ALL)
- client-side filtering (ALL / ACTIVE / INACTIVE)
- optimistic updates with rollback on failure
- sorting by name
- create / edit name / edit description / toggle active
- info modal showing created/updated timestamps

Input:
- `api` (list, create, patch)
- `labels`
- `modals`
- `toasts`
- optional `rules`

Output:
- state refs: `newName`, `newDesc`, `filterStatus`, `loading`, `error`
- data: `visibleItems`, `sortedAll`
- actions: `loadAll`, `create`, `editName`, `editDescription`, `toggleActive`, `showInfo`

### TaxonomyTable.vue (presentational)
Responsibilities:
- receives **plain values** (NOT refs)
- uses `v-model` for `filterStatus`, `newName`, `newDesc`
- emits semantic events (`create`, `edit-name`, `edit-description`, `toggle-active`, `info`)
- never calls APIs and never owns business logic

---

## Shared UI Services: Modals & Toasts

Design rule: features import **only public APIs**, not internal store/infra.

### Modals (`shared/ui/modals`)
- Public API: `useModals()`
- App wiring: `createModalPlugin()` installed in `main.js`
- Renderer: `<ModalHost />` mounted once in `AppLayout.vue`
- Variants live under `modals/variants/*`

### Toasts (`shared/ui/toast`)
- Public API: `useToast()`
- Renderer: `<ToastHost />` mounted once in `AppLayout.vue`
- Infra: `toast-store.js` holds reactive `toastState` and push/remove
- Verified working: `toast.success(...)`, `toast.danger(...)`
