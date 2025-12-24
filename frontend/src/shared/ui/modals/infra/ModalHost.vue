<script setup>
import { inject, computed } from 'vue';
import { MODAL_KEY } from './modal-plugin';
import { MODAL_COMPONENTS } from '../registry/modal-registry';

const modal = inject(MODAL_KEY);
if (!modal) throw new Error('Modal service not installed');

const active = computed(() => modal.active.value);

const component = computed(() => {
  const type = active.value?.type;
  return type ? MODAL_COMPONENTS[type] || null : null;
});

const props = computed(() => active.value?.props || {});

function close(v = null) {
  modal.close(v);
}
</script>

<template>
  <component
    v-if="component"
    :is="component"
    v-bind="props"
    @close="close(null)"
    @confirm="close($event)"
  />
</template>
