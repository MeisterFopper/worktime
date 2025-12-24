// src/pages/report/composables/useReportLoader.js
import { ref } from 'vue';

export function useReportLoader({ toast, workReportsApi, fromUtc, toUtc }) {
  const loading = ref(false);
  const error = ref('');
  const daysRaw = ref([]);

  async function loadReport() {
    loading.value = true;
    error.value = '';

    try {
      const days = await workReportsApi.days({ from: fromUtc.value, to: toUtc.value });
      daysRaw.value = Array.isArray(days) ? days : [];
    } catch (e) {
      console.error(e);
      error.value = 'Failed to load work sessions';
      toast?.danger?.('Failed to load work sessions');
      daysRaw.value = [];
    } finally {
      loading.value = false;
    }
  }

  return { loading, error, daysRaw, loadReport };
}
