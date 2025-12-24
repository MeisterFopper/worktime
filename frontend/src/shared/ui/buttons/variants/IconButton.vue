<script setup>
defineProps({
  title: { type: String, default: '' },
  icon: { type: String, required: true },

  variant: { type: String, default: '' },

  size: { type: String, default: 'sm' },
  kind: { type: String, default: '' },

  disabled: { type: Boolean, default: false },
});

const emit = defineEmits(['click']);
</script>

<template>
  <button
    type="button"
    class="btn btn-icon"
    :class="[
      size ? `btn-${size}` : '',
      kind ? `btn-icon--${kind}` : '',
      variant ? `btn-${variant}` : ''
    ]"
    :title="title"
    :disabled="disabled"
    @click="emit('click')"
  >
    <i class="bi" :class="icon"></i>
  </button>
</template>
