<script setup>
import { useToast } from '@/shared/ui/toast';
import { useModals } from '@/shared/ui/modals';

import { workReportsApi } from './reports/api/work-reports-api';
import { workSessionsApi } from './sessions/api/work-sessions-api';
import { workSegmentsApi } from './segments/api/work-segments-api';

import { useReportPage } from './composables/useReportPage';

import ReportToolbar from './components/ReportToolbar.vue';
import ReportTable from './components/ReportTable.vue';

const toast = useToast();
const modals = useModals();

const {
  loading,
  error,

  showSegments,
  resetDisabled,
  daysVm,

  setShowSegments,
  openDateRangeModal,
  clearDateRange,

  requestEditSessionStart,
  requestEditSessionEnd,
  deleteWorkSession,
  requestEditSegmentStart,
  requestEditSegmentEnd,
  deleteWorkSegment,
  exportDisabled,
  exportDisabledTitle,
  exportPdf,
} = useReportPage({
  toast,
  modals,
  workReportsApi,
  workSessionsApi,
  workSegmentsApi,
});

function dash(v) {
  const s = String(v ?? '').trim();
  return s ? s : '—';
}

async function showSessionInfo(s) {
  const lines = [
    { label: 'Start', value: dash(s?.startLabel) },
    { label: 'End', value: s?.running ? 'running…' : dash(s?.endLabel) },
    { label: 'Duration', value: dash(s?.durationTop) },
    { label: 'Segments', value: `${dash(s?.segmentsSummary)} (${Number(s?.segmentCount ?? 0)})` },
    { label: 'Unallocated', value: dash(s?.unallocatedSummary) },
  ];

  if (modals?.openInfoModal) {
    await modals.openInfoModal('Session info', lines);
    return;
  }

  await modals?.openTextModal?.(
    'Session info',
    lines.map(x => `${x.label}: ${x.value}`).join('\n'),
    { maxLength: 4000 }
  );
}

async function showSegmentInfo(seg) {
  const lines = [
    { label: 'Start', value: dash(seg?.startLabel) },
    { label: 'End', value: seg?.running ? 'running…' : dash(seg?.endLabel) },
    { label: 'Duration', value: dash(seg?.durationLabel) },

    // individual fields
    { label: 'Category', value: dash(seg?.categoryName) },
    { label: 'Activity', value: dash(seg?.activityName) },
    { label: 'Comment', value: dash(seg?.comment) },
  ];

  if (modals?.openInfoModal) {
    await modals.openInfoModal('Segment info', lines);
    return;
  }

  await modals?.openTextModal?.(
    'Segment info',
    lines.map(x => `${x.label}: ${x.value}`).join('\n'),
    { maxLength: 4000 }
  );
}
</script>

<template>
  <div class="py-3">
    <ReportToolbar
      :reset-disabled="resetDisabled"
      :show-segments="showSegments"
      :export-disabled="exportDisabled"
      :export-title="exportDisabledTitle"
      @open-range="openDateRangeModal"
      @reset-range="clearDateRange"
      @update:showSegments="setShowSegments"
      @export="exportPdf"
    />

    <div v-if="error" class="alert alert-danger">{{ error }}</div>

    <ReportTable
      :loading="loading"
      :days="daysVm"
      @edit-session-start="({ id, current }) => requestEditSessionStart(id, current)"
      @edit-session-end="({ id, current }) => requestEditSessionEnd(id, current)"
      @delete-session="deleteWorkSession"
      @info-session="showSessionInfo"
      @edit-segment-start="({ id, current }) => requestEditSegmentStart(id, current)"
      @edit-segment-end="({ id, current }) => requestEditSegmentEnd(id, current)"
      @delete-segment="deleteWorkSegment"
      @info-segment="showSegmentInfo"
    />
  </div>
</template>
