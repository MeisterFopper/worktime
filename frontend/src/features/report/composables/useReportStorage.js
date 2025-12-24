// src/pages/report/composables/report/useReportStorage.js
import { computed, ref } from 'vue';
import { computeLocalDayRangeUtc, isSameInstantUtcIso } from '@/shared/utils/date';

export function useReportStorage() {
  const STORAGE_KEY_SHOW_SEGMENTS = 'report.showSegments';
  const STORAGE_KEY_FROM_UTC = 'report.fromUtc';
  const STORAGE_KEY_TO_UTC = 'report.toUtc';
  const STORAGE_KEY_RANGE_CUSTOM = 'report.rangeCustomized';

  const showSegments = ref(localStorage.getItem(STORAGE_KEY_SHOW_SEGMENTS) === 'true');

  const fromUtc = ref(localStorage.getItem(STORAGE_KEY_FROM_UTC) || '');
  const toUtc = ref(localStorage.getItem(STORAGE_KEY_TO_UTC) || '');
  const rangeCustomized = ref(localStorage.getItem(STORAGE_KEY_RANGE_CUSTOM) === 'true');

  function computeStandardRangeUtc() {
    return computeLocalDayRangeUtc({ daysBack: 7, daysForward: 0 });
  }

  function isStandardRange(curFromUtc, curToUtc) {
    const std = computeStandardRangeUtc();
    return isSameInstantUtcIso(curFromUtc, std.from) && isSameInstantUtcIso(curToUtc, std.to);
  }

  function persistRange() {
    localStorage.setItem(STORAGE_KEY_RANGE_CUSTOM, String(rangeCustomized.value));
    localStorage.setItem(STORAGE_KEY_FROM_UTC, fromUtc.value);
    localStorage.setItem(STORAGE_KEY_TO_UTC, toUtc.value);
  }

  function applyStandardRange() {
    const std = computeStandardRangeUtc();
    fromUtc.value = std.from;
    toUtc.value = std.to;

    rangeCustomized.value = false;
    persistRange();
  }

  function ensureRangeInitialized() {
    if (!rangeCustomized.value) {
      applyStandardRange();
      return;
    }
    if (!fromUtc.value || !toUtc.value) applyStandardRange();
  }

  const resetDisabled = computed(() => !rangeCustomized.value);

  function setShowSegments(v) {
    showSegments.value = !!v;
    localStorage.setItem(STORAGE_KEY_SHOW_SEGMENTS, String(showSegments.value));
  }

  return {
    showSegments,
    fromUtc,
    toUtc,
    rangeCustomized,
    resetDisabled,

    ensureRangeInitialized,
    applyStandardRange,
    isStandardRange,
    persistRange,
    setShowSegments,
  };
}
