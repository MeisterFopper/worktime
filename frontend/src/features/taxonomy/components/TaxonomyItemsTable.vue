<script setup>
import { IconButton } from '@/shared/ui/buttons';

defineProps({
  items: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' },
  emptyText: { type: String, default: 'No entries found' },
});

const emit = defineEmits([
  'edit-name',
  'edit-description',
  'toggle-active',
  'info',
]);
</script>

<template>
  <table class="table table-hover align-middle">
    <thead class="table-dark">
      <tr>
        <th style="width:8%">Active</th>
        <th style="width:26%">Name</th>
        <th style="width:58%">Description</th>
        <th style="width:8%" class="text-center">Info</th>
      </tr>
    </thead>

    <tbody>
      <tr v-if="loading">
        <td colspan="4" class="text-center text-muted">Loading…</td>
      </tr>

      <tr v-else-if="error">
        <td colspan="4" class="text-center text-danger">{{ error }}</td>
      </tr>

      <tr v-else-if="items.length === 0">
        <td colspan="4" class="text-center text-muted">{{ emptyText }}</td>
      </tr>

      <tr v-else v-for="it in items" :key="it.id">
        <td class="text-center">
          <button
            type="button"
            class="btn btn-sm w-100"
            :class="it.active ? 'btn-success' : 'btn-outline-secondary'"
            style="min-width: 80px"
            @click="emit('toggle-active', it)"
          >
            {{ it.active ? 'Active' : 'Inactive' }}
          </button>
        </td>

        <td class="text-nowrap">
          <span class="me-2">{{ it.name }}</span>
          <IconButton
            title="Edit name"
            icon="bi-pencil"
            kind="edit"
            @click="emit('edit-name', it)"
          />
        </td>

        <td>
          <span class="me-2">{{ it.description ?? '' }}</span>
          <IconButton
            title="Edit description"
            icon="bi-pencil"
            kind="edit"
            @click="emit('edit-description', it)"
          />
        </td>

        <td class="text-center">
          <IconButton
            title="Info"
            icon="bi-info-circle"
            kind="info"
            @click="emit('info', it)"
          />
        </td>
      </tr>
    </tbody>
  </table>
</template>
