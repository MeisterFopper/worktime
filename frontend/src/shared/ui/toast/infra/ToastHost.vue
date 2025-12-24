<script setup>
import { nextTick, watch } from 'vue';
import { Toast as BootstrapToast } from 'bootstrap';
import { toastState, toast } from './toast-store';

async function showBootstrapToast(toastItem) {
  // Wait for DOM to render the new toast
  await nextTick();

  const el = document.getElementById(`toast-${toastItem.id}`);
  if (!el) return;

  // Re-use instance if it already exists (safe on re-renders)
  const bsToast = BootstrapToast.getOrCreateInstance(el, {
    delay: toastItem.delayMs,
    autohide: true,
  });

  el.addEventListener(
    'hidden.bs.toast',
    () => toast.remove(toastItem.id),
    { once: true }
  );

  bsToast.show();
}

// whenever a new toast is pushed, show it
watch(
  () => toastState.items.length,
  (len, prev) => {
    if (len <= prev) return;
    const newest = toastState.items[len - 1];
    showBootstrapToast(newest);
  }
);
</script>

<template>
  <div
    id="toastContainer"
    class="toast-container position-fixed top-0 end-0 p-3"
    style="z-index: 1080;"
  >
    <div
      v-for="t in toastState.items"
      :key="t.id"
      class="toast align-items-center border-0 mb-2"
      :class="`text-bg-${t.type}`"
      role="alert"
      aria-live="assertive"
      aria-atomic="true"
      :id="`toast-${t.id}`"
    >
      <div class="d-flex">
        <div class="toast-body">{{ t.message }}</div>
        <button
          type="button"
          class="btn-close btn-close-white me-2 m-auto"
          data-bs-dismiss="toast"
          aria-label="Close"
        />
      </div>
    </div>
  </div>
</template>
