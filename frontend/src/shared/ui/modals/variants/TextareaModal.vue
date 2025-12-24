<script setup>
import { nextTick, onMounted, ref, watch } from 'vue';

const props = defineProps({
  title: { type: String, required: true },
  currentValue: { type: String, default: '' },

  placeholder: { type: String, default: '' },
  rows: { type: Number, default: 4 },
  maxLength: { type: Number, default: 500 },

  // UX: Enter saves only if multiline is not needed (default: multiline friendly)
  submitOnEnter: { type: Boolean, default: false },
});

const emit = defineEmits(['confirm', 'close']);

const value = ref(props.currentValue || '');
const textareaRef = ref(null);

watch(
  () => props.currentValue,
  (v) => (value.value = v || ''),
  { immediate: true }
);

onMounted(async () => {
  await nextTick();
  textareaRef.value?.focus();
  textareaRef.value?.select?.();
});

function save() {
  emit('confirm', (value.value || '').trim());
}

function onKeydown(e) {
  if (e.key === 'Escape') emit('close');

  if (props.submitOnEnter && e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault();
    save();
  }
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
          <textarea
            ref="textareaRef"
            v-model="value"
            class="form-control"
            :placeholder="placeholder"
            :rows="rows"
            :maxlength="maxLength"
            @keydown="onKeydown"
          />
          <div class="form-text text-muted" v-if="maxLength && maxLength > 0">
            {{ (value?.length ?? 0) }} / {{ maxLength }}
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" type="button" @click="emit('close')">Cancel</button>
          <button class="btn btn-primary" type="button" @click="save">Save</button>
        </div>
      </div>
    </div>
  </div>
</template>
