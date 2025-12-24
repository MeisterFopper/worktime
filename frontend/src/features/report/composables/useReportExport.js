// src/pages/report/composables/useReportExport.js
import { computed } from 'vue';
import { LOCAL_TZ, DEFAULT_LOCALE } from '@/shared/utils/date';

export function useReportExport({
  toast,
  loading,
  daysRaw,
  fromUtc,
  toUtc,
  showSegments,
  workReportsApi,
}) {
  const hasRunningSession = computed(() => {
    const days = Array.isArray(daysRaw.value) ? daysRaw.value : [];
    return days.some((d) =>
      (Array.isArray(d?.sessions) ? d.sessions : []).some((s) => !s?.endTime),
    );
  });

  const hasRunningSegment = computed(() => {
    const days = Array.isArray(daysRaw.value) ? daysRaw.value : [];
    return days.some((d) =>
      (Array.isArray(d?.sessions) ? d.sessions : []).some((s) =>
        (Array.isArray(s?.items) ? s.items : []).some((seg) => !seg?.endTime),
      ),
    );
  });

  const exportDisabled = computed(
    () => loading.value || hasRunningSession.value || hasRunningSegment.value,
  );

  const exportDisabledTitle = computed(() => {
    if (loading.value) return 'Report is loading';
    if (hasRunningSegment.value) return 'Export disabled: a segment is still running';
    if (hasRunningSession.value) return 'Export disabled: a session is still running';
    return 'Export PDF';
  });

  const exportUrl = computed(() => {
    if (!workReportsApi?.exportPdfUrl) return '';

    // Backend: showSegments is required. Always send an explicit boolean string.
    const includeSegments = Boolean(showSegments.value);

    return workReportsApi.exportPdfUrl({
      from: fromUtc.value || '',
      to: toUtc.value || '',
      tz: LOCAL_TZ || '',
      locale: DEFAULT_LOCALE || '',
      showSegments: includeSegments ? 'true' : 'false',
    });
  });

  function exportPdf() {
    if (exportDisabled.value) {
      toast?.warning?.(exportDisabledTitle.value);
      return;
    }

    const url = exportUrl.value;
    if (!url) {
      toast?.danger?.('Export URL is not configured');
      return;
    }

    window.open(url, '_blank', 'noopener,noreferrer');
  }

  return {
    hasRunningSession,
    hasRunningSegment,
    exportDisabled,
    exportDisabledTitle,
    exportUrl,
    exportPdf,
  };
}
