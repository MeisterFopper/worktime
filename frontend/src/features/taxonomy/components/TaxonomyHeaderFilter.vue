<script setup>
import { PageHeader } from '@/shared/ui/components';

defineProps({
  title: { type: String, required: true },
  filterStatus: { type: String, default: 'ACTIVE' },
});

const emit = defineEmits(['update:filterStatus']);
</script>

<template>
  <PageHeader :title="title">
    <span class="text-muted small">Status:</span>

    <select
      class="form-select form-select-sm"
      style="width:auto;"
      :value="filterStatus"
      @change="emit('update:filterStatus', $event.target.value)"
    >
      <option value="ALL">All</option>
      <option value="ACTIVE">Active</option>
      <option value="INACTIVE">Inactive</option>
    </select>
  </PageHeader>
</template>
