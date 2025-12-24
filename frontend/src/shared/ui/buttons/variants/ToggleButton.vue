<script setup>
defineProps({
  active: { type: Boolean, default: false },
  minWidth: { type: String, default: '80px' },
  activeText: { type: String, default: 'Active' },
  inactiveText: { type: String, default: 'Inactive' },
});
const emit = defineEmits(['click']);
</script>

<template>
  <button
    type="button"
    class="btn btn-sm w-100"
    :class="active ? 'btn-success' : 'btn-outline-secondary'"
    :style="{ minWidth }"
    @click="emit('click')"
  >
    {{ active ? activeText : inactiveText }}
  </button>
</template>
