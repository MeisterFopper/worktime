// src/pages/report/composables/useReportPage.js
import { onActivated, onMounted } from 'vue';
import { nowUtcIso } from '@/shared/utils/date';
import { useLiveTicker } from '@/shared/utils/useLiveTicker';

import { useReportStorage } from './useReportStorage';
import { useReportLoader } from './useReportLoader';
import { useReportDurations } from './useReportDurations';
import { useReportViewModel } from './useReportViewModel';
import { useReportMutations } from './useReportMutations';
import { useReportExport } from './useReportExport';

export function useReportPage({
  toast,
  modals,
  workReportsApi,
  workSessionsApi,
  workSegmentsApi,
}) {
  // Storage / range state
  const storage = useReportStorage();
  storage.ensureRangeInitialized();

  // Loader
  const { loading, error, daysRaw, loadReport } = useReportLoader({
    toast,
    workReportsApi,
    fromUtc: storage.fromUtc,
    toUtc: storage.toUtc,
  });

  // Live ticker (UTC now)
  const { value: nowTickUtc } = useLiveTicker({
    getValue: nowUtcIso,
    intervalMs: 1000,
    pauseWhenHidden: true,
    tickOnStart: true,
  });

  // Durations
  const durations = useReportDurations({ nowTickUtc });

  // View model
  const { daysVm } = useReportViewModel({
    daysRaw,
    showSegments: storage.showSegments,
    durations,
  });

  // UI actions that must reload
  function setShowSegments(v) {
    storage.setShowSegments(v);
    loadReport();
  }

  async function openDateRangeModal() {
    const res = await modals.openDateTimeRangeModal(
      'Filter by time range',
      storage.fromUtc.value,
      storage.toUtc.value,
    );
    if (!res) return;

    storage.fromUtc.value = res.from || '';
    storage.toUtc.value = res.to || '';
    storage.rangeCustomized.value = !storage.isStandardRange(storage.fromUtc.value, storage.toUtc.value);
    storage.persistRange();

    loadReport();
  }

  function clearDateRange() {
    if (!storage.rangeCustomized.value) return;
    storage.applyStandardRange();
    loadReport();
  }

  // Mutations
  const mutations = useReportMutations({
    toast,
    modals,
    workSessionsApi,
    workSegmentsApi,
    loadReport,
  });

  // Export
  const exportApi = useReportExport({
    toast,
    loading,
    daysRaw,
    fromUtc: storage.fromUtc,
    toUtc: storage.toUtc,
    showSegments: storage.showSegments,
    workReportsApi,
  });

  // Lifecycle
  onMounted(loadReport);
  onActivated(loadReport);

  return {
    // state
    loading,
    error,

    // storage/range
    showSegments: storage.showSegments,
    fromUtc: storage.fromUtc,
    toUtc: storage.toUtc,
    rangeCustomized: storage.rangeCustomized,
    resetDisabled: storage.resetDisabled,

    // view model
    daysVm,

    // ui actions
    setShowSegments,
    openDateRangeModal,
    clearDateRange,

    // mutations
    ...mutations,

    // export
    ...exportApi,
  };
}
