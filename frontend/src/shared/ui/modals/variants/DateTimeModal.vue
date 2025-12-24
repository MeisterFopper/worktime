<script setup>
import { nextTick, onMounted, ref, watch, computed } from 'vue';
import { utcIsoToLocalInput, localInputToUtcIso, LOCAL_TZ } from '@/shared/utils/date';

const props = defineProps({
  title: { type: String, required: true },
  currentValue: { type: String, default: '' }, // UTC ISO
});

const emit = defineEmits(['confirm', 'close']);

const inputRef = ref(null);
const localValue = ref('');

const tzLabel = computed(() => `Local time (${LOCAL_TZ})`);

watch(
  () => props.currentValue,
  (v) => (localValue.value = utcIsoToLocalInput(v || '')),
  { immediate: true }
);

onMounted(async () => {
  await nextTick();
  inputRef.value?.focus();
});

function save() {
  const v = (localValue.value || '').trim();
  emit('confirm', v ? localInputToUtcIso(v) : null);
}
</script>

<template>
  <div class="modal d-block" tabindex="-1" role="dialog" style="background: rgba(0,0,0,.5)">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">{{ title }}</h5>
          <button type="button" class="btn-close" @click="emit('close')" />
        </div>

        <div class="modal-body">
          <input
            ref="inputRef"
            v-model="localValue"
            type="datetime-local"
            step="1"
            class="form-control"
          />
        </div>

        <div class="modal-footer d-flex justify-content-between align-items-center">
          <small class="text-muted">{{ tzLabel }}</small>
          <div class="d-flex gap-2">
            <button class="btn btn-secondary" type="button" @click="emit('close')">Cancel</button>
            <button class="btn btn-primary" type="button" @click="save">Save</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
