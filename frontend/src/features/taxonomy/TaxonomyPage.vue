<script setup>
import { computed } from 'vue';

import TaxonomyTable from './components/TaxonomyTable.vue';
import { useTaxonomyPage } from './composables/useTaxonomyPage';

import { getTaxonomy } from './taxonomy-config';
import { createTaxonomyApi } from './api/taxonomy-api';

import { useToast } from '@/shared/ui/toast';
import { useModals } from '@/shared/ui/modals';

const props = defineProps({
  kind: { type: String, required: true }, // 'categories' | 'activities'
});

const cfg = computed(() => getTaxonomy(props.kind));
const api = computed(() => createTaxonomyApi(cfg.value.endpoint));

const toast = useToast();
const modals = useModals();

// Adapter to the interface expected by useTaxonomyPage
const toasts = {
  success: toast.success,
  warning: toast.warning,
  danger: toast.danger,
};

const page = useTaxonomyPage({
  api: api.value,
  labels: cfg.value.labels,
  toasts,
  modals,
  initialFilterStatus: 'ACTIVE',
  autoLoad: true,
  reloadOnActivated: false,
});
</script>

<template>
  <TaxonomyTable
    :title="cfg.title"
    :labels="cfg.labels"
    :loading="page.loading.value"
    :error="page.error.value"
    :items="page.visibleItems.value"
    :newName="page.newName.value"
    :newDesc="page.newDesc.value"
    :filterStatus="page.filterStatus.value"
    @update:newName="page.newName.value = $event"
    @update:newDesc="page.newDesc.value = $event"
    @update:filterStatus="page.filterStatus.value = $event"
    @create="page.create"
    @edit-name="page.editName"
    @edit-description="page.editDescription"
    @toggle-active="page.toggleActive"
    @info="page.showInfo"
  />
</template>
