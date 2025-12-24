<script setup>
import { nextTick, onMounted, ref, watch } from 'vue';

const props = defineProps({
  title: { type: String, required: true },
  currentValue: { type: String, default: '' },
  placeholder: { type: String, default: '' },
  maxLength: { type: Number, default: 200 },
});

const emit = defineEmits(['confirm', 'close']);

const value = ref(props.currentValue || '');
const inputRef = ref(null);

watch(
  () => props.currentValue,
  (v) => (value.value = v || ''),
  { immediate: true }
);

onMounted(async () => {
  await nextTick();
  inputRef.value?.focus();
  inputRef.value?.select?.();
});

function save() {
  emit('confirm', (value.value || '').trim());
}

function onKeydown(e) {
  if (e.key === 'Escape') emit('close');
  if (e.key === 'Enter') {
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
          <input
            ref="inputRef"
            v-model="value"
            type="text"
            class="form-control"
            :placeholder="placeholder"
            :maxlength="maxLength"
            @keydown="onKeydown"
          />
        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" type="button" @click="emit('close')">Cancel</button>
          <button class="btn btn-primary" type="button" @click="save">Save</button>
        </div>
      </div>
    </div>
  </div>
</template>
