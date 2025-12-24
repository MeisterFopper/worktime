<script setup>
import { nextTick, onMounted, ref, watch, computed } from 'vue';
import { utcIsoToLocalInput, localInputToUtcIso, utcIsoToMillis, LOCAL_TZ } from '@/shared/utils/date';

const props = defineProps({
  title: { type: String, required: true },
  fromUtcIso: { type: String, default: '' },
  toUtcIso: { type: String, default: '' },
});

const emit = defineEmits(['confirm', 'close']);

const fromRef = ref(null);
const fromLocal = ref('');
const toLocal = ref('');
const error = ref('');

const tzLabel = computed(() => `Local time (${LOCAL_TZ})`);

watch(
  () => [props.fromUtcIso, props.toUtcIso],
  ([fromUtc, toUtc]) => {
    fromLocal.value = utcIsoToLocalInput(fromUtc || '');
    toLocal.value = utcIsoToLocalInput(toUtc || '');
    error.value = '';
  },
  { immediate: true }
);

onMounted(async () => {
  await nextTick();
  (fromLocal.value ? null : fromRef.value)?.focus?.();
});

function save() {
  const fromUtc = (fromLocal.value || '').trim() ? localInputToUtcIso(fromLocal.value.trim()) : null;
  const toUtc = (toLocal.value || '').trim() ? localInputToUtcIso(toLocal.value.trim()) : null;

  if (fromUtc && toUtc) {
    const a = utcIsoToMillis(fromUtc);
    const b = utcIsoToMillis(toUtc);
    if (Number.isFinite(a) && Number.isFinite(b) && b < a) {
      error.value = '"To" must be after or equal to "From".';
      return;
    }
  }

  emit('confirm', { from: fromUtc, to: toUtc });
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
          <div class="row g-3">
            <div class="col-12">
              <label class="form-label">From (local)</label>
                <input
                  ref="fromRef"
                  v-model="fromLocal"
                  type="datetime-local"
                  step="1"
                  class="form-control"
                />
            </div>
            <div class="col-12">
              <label class="form-label">To (local)</label>
                <input
                  v-model="toLocal"
                  type="datetime-local"
                  step="1"
                  class="form-control"
                />
            </div>
          </div>

          <small class="text-muted d-block mt-2">{{ tzLabel }}</small>
          <small class="text-muted d-block">Range is inclusive-from, exclusive-to.</small>
          <small v-if="error" class="text-danger d-block mt-2">{{ error }}</small>
        </div>

        <div class="modal-footer">
          <button class="btn btn-outline-secondary" type="button" @click="emit('close')">Cancel</button>
          <button class="btn btn-primary" type="button" @click="save">Apply</button>
        </div>
      </div>
    </div>
  </div>
</template>
