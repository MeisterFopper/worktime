<script setup>
defineProps({
  newName: { type: String, default: '' }, // v-model:newName
  newDesc: { type: String, default: '' }, // v-model:newDesc

  namePlaceholder: { type: String, default: 'Name' },
  descPlaceholder: { type: String, default: 'Description (optional)' },
  createText: { type: String, default: 'Create' },
});

const emit = defineEmits([
  'update:newName',
  'update:newDesc',
  'create',
]);
</script>

<template>
  <div class="row g-2 mb-3">
    <div class="col-md-4">
      <input
        class="form-control"
        :placeholder="namePlaceholder"
        :value="newName"
        @input="emit('update:newName', $event.target.value)"
      />
    </div>

    <div class="col-md-6">
      <input
        class="form-control"
        :placeholder="descPlaceholder"
        :value="newDesc"
        @input="emit('update:newDesc', $event.target.value)"
      />
    </div>

    <div class="col-md-2 d-grid">
      <button class="btn btn-primary" type="button" @click="emit('create')">
        {{ createText }}
      </button>
    </div>
  </div>
</template>
