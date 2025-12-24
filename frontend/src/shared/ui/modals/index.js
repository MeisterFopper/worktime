// Public API (feature code)
export { useModals } from './api/useModals';

// App wiring (bootstrap/layout)
export { createModalPlugin } from './infra/modal-plugin';
export { default as ModalHost } from './infra/ModalHost.vue';
