<script setup>
import { computed } from 'vue';

import StatusCrudHeaderFilter from './TaxonomyHeaderFilter.vue';
import StatusCrudCreateForm from './TaxonomyCreateForm.vue';
import StatusCrudItemsTable from './TaxonomyItemsTable.vue';

const props = defineProps({
  title: { type: String, required: true },

  // v-models (from parent of wrapper)
  filterStatus: { type: String, default: 'ACTIVE' },
  newName: { type: String, default: '' },
  newDesc: { type: String, default: '' },

  // UI text customization
  namePlaceholder: { type: String, default: 'Name' },
  descPlaceholder: { type: String, default: 'Description (optional)' },
  createText: { type: String, default: 'Create' },
  emptyText: { type: String, default: 'No entries found' },

  items: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' },
});

const emit = defineEmits([
  'update:filterStatus',
  'update:newName',
  'update:newDesc',
  'create',
  'edit-name',
  'edit-description',
  'toggle-active',
  'info',
]);

// v-model proxy helpers (wrapper <-> children)
const filterStatusModel = computed({
  get: () => props.filterStatus,
  set: v => emit('update:filterStatus', v),
});

const newNameModel = computed({
  get: () => props.newName,
  set: v => emit('update:newName', v),
});

const newDescModel = computed({
  get: () => props.newDesc,
  set: v => emit('update:newDesc', v),
});

// event passthrough for table (one binding)
const tableHandlers = {
  'toggle-active': it => emit('toggle-active', it),
  'edit-name': it => emit('edit-name', it),
  'edit-description': it => emit('edit-description', it),
  info: it => emit('info', it),
};
</script>

<template>
  <div class="py-3">
    <StatusCrudHeaderFilter
      :title="title"
      v-model:filterStatus="filterStatusModel"
    />

    <StatusCrudCreateForm
      v-model:newName="newNameModel"
      v-model:newDesc="newDescModel"
      :name-placeholder="namePlaceholder"
      :desc-placeholder="descPlaceholder"
      :create-text="createText"
      @create="emit('create')"
    />

    <StatusCrudItemsTable
      :items="items"
      :loading="loading"
      :error="error"
      :empty-text="emptyText"
      v-on="tableHandlers"
    />
  </div>
</template>
