<script setup>
defineProps({
  resetDisabled: { type: Boolean, default: true },
  showSegments: { type: Boolean, default: false },
  exportDisabled: { type: Boolean, default: false },
  exportTitle: { type: String, default: 'Export PDF' },
});

const emit = defineEmits(['open-range', 'reset-range', 'update:showSegments', 'export']);
</script>

<template>
  <div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="mb-0">Work Sessions</h1>

    <div class="d-flex align-items-center gap-3">
      <button
        class="btn btn-sm btn-outline-primary"
        type="button"
        :disabled="exportDisabled"
        :title="exportTitle"
        @click="emit('export')"
      >
        Export PDF
      </button>

      <button
        class="btn btn-sm btn-outline-secondary"
        type="button"
        @click="emit('open-range')"
      >
        Date range
      </button>

      <button
        class="btn btn-sm btn-outline-secondary"
        type="button"
        :disabled="resetDisabled"
        :title="resetDisabled ? 'Already using standard range' : 'Reset to standard range'"
        @click="emit('reset-range')"
      >
        Reset
      </button>

      <div class="form-check form-switch mb-0">
        <input
          id="toggleSegments"
          class="form-check-input"
          type="checkbox"
          :checked="showSegments"
          @change="emit('update:showSegments', $event.target.checked)"
        />
        <label class="form-check-label" for="toggleSegments">Show segments</label>
      </div>
    </div>
  </div>
</template>
