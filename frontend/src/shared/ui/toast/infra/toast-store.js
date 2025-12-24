//src/shared/ui/toast/toast-store.js
import { reactive } from 'vue';

let nextId = 1;

export const toastState = reactive({
  items: [], // { id, message, type, delayMs }
});

const allowed = new Set(['success','danger','warning','info','primary','secondary','dark','light']);

function push(message, type = 'success', delayMs = 4000) {
  const safeType = allowed.has(type) ? type : 'secondary';
  toastState.items.push({
    id: nextId++,
    message: String(message ?? ''),
    type: safeType,
    delayMs: Number(delayMs) || 4000,
  });
}

function remove(id) {
  const idx = toastState.items.findIndex(t => t.id === id);
  if (idx >= 0) toastState.items.splice(idx, 1);
}

export const toast = {
  show: push,
  success: (m, d) => push(m, 'success', d),
  danger: (m, d) => push(m, 'danger', d),
  warning: (m, d) => push(m, 'warning', d),
  info: (m, d) => push(m, 'info', d),
  remove,
};
