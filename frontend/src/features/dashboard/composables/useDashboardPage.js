// src/pages/dashboard/useDashboardPage.js
import { computed, onActivated, onMounted, ref } from 'vue';

import { ApiError } from '@/shared/api/api-error';
import {
  nowLocalFormatted,
  nowUtcIso,
  calcDurationDisplay,
  computeLocalDayRangeUtc,
} from '@/shared/utils/date';
import { useLiveTicker } from '@/shared/utils/useLiveTicker';

export function useDashboardPage({
  toast,
  categoriesApi,
  activitiesApi,
  workSessionsApi,
  workSegmentsApi,
  workReportsApi,
}) {
  // ---------- State ----------
  const loading = ref(false);
  const error = ref('');

  const currentSession = ref(null);
  const sessions = ref([]);
  const currentSegment = ref(null);

  const daysCount = ref(10);

  // keep as strings for <select>
  const categoryId = ref('');
  const activityId = ref('');
  const comment = ref('');

  const categories = ref([]);
  const activities = ref([]);

  // ---------- Live tickers (keep-alive safe) ----------
  const { value: headerClock } = useLiveTicker({
    getValue: nowLocalFormatted,
    intervalMs: 1000,
    pauseWhenHidden: true,
    tickOnStart: true,
  });

  const { value: nowTickUtc } = useLiveTicker({
    getValue: nowUtcIso,
    intervalMs: 1000,
    pauseWhenHidden: true,
    tickOnStart: true,
  });

  // ---------- Derived ----------
  const isSessionRunning = computed(
    () => !!(currentSession.value && !currentSession.value.endTime),
  );

  const isSegmentRunning = computed(
    () => !!(currentSegment.value && !currentSegment.value.endTime),
  );

  const recentSessions = computed(() => {
    const n = Math.max(1, Number(daysCount.value) || 10);

    const all = Array.isArray(sessions.value) ? sessions.value : [];
    const finished = all.filter((s) => s?.endTime);

    // If running session exists, show it + (n-1) finished rows
    const limit = isSessionRunning.value ? Math.max(0, n - 1) : n;
    return finished.slice(0, limit);
  });

  const sessionRunningDuration = computed(() => {
    if (!isSessionRunning.value) return '';
    return calcDurationDisplay(currentSession.value.startTime, nowTickUtc.value);
  });

  const segmentRunningDuration = computed(() => {
    if (!isSegmentRunning.value) return '';
    return calcDurationDisplay(currentSegment.value.startTime, nowTickUtc.value);
  });

  // ---------- Helpers ----------
  function setError(e, fallback) {
    error.value = e instanceof ApiError ? e.message : fallback;
  }

  function resetSegmentForm() {
    categoryId.value = '';
    activityId.value = '';
    comment.value = '';
  }

  function applySegmentFormForRunningSegment(seg) {
    if (!seg) return;

    // only populate if user has not touched the form
    if (!categoryId.value && seg.categoryId != null) categoryId.value = String(seg.categoryId);
    if (!activityId.value && seg.activityId != null) activityId.value = String(seg.activityId);
    if (!comment.value.trim() && seg.comment) comment.value = seg.comment;
  }

  // ---------- Loading ----------
  async function loadSelectData() {
    const [cats, acts] = await Promise.all([
      categoriesApi.list({ status: 'ACTIVE' }),
      activitiesApi.list({ status: 'ACTIVE' }),
    ]);

    categories.value = Array.isArray(cats) ? cats : [];
    activities.value = Array.isArray(acts) ? acts : [];
  }

  function computeDashboardRangeUtc() {
    return computeLocalDayRangeUtc({ daysBack: 90, daysForward: 0 });
  }

  async function loadSessionsFromReports() {
    const { from, to } = computeDashboardRangeUtc();
    const days = await workReportsApi.days({ from, to });
    const daysArr = Array.isArray(days) ? days : [];

    const flat = daysArr.flatMap((d) =>
      (Array.isArray(d?.sessions) ? d.sessions : []),
    );

    sessions.value = flat;
    currentSession.value = flat.find((s) => s && !s.endTime) || null;
  }

  async function loadCurrentSegment() {
    currentSegment.value = await workSegmentsApi.current();

    if (isSegmentRunning.value) applySegmentFormForRunningSegment(currentSegment.value);
    if (!currentSegment.value) resetSegmentForm();
  }

  async function refreshDashboard({ swallowErrors = false } = {}) {
    try {
      await Promise.all([
        loadSessionsFromReports(),
        loadSelectData(),
        loadCurrentSegment(),
      ]);
    } catch (e) {
      console.error(e);
      if (!swallowErrors) setError(e, 'Failed to load dashboard');
    }
  }

  async function loadDashboard() {
    loading.value = true;
    error.value = '';
    try {
      await refreshDashboard();
    } finally {
      loading.value = false;
    }
  }

  // ---------- Actions: Work session ----------
  async function startDay() {
    try {
      await workSessionsApi.start();
      toast.success('Workday started');
      await loadDashboard();
    } catch (e) {
      console.error(e);
      toast.danger(`Error: ${e instanceof ApiError ? e.message : 'Failed to start workday'}`);
    }
  }

  async function stopDay() {
    try {
      if (isSegmentRunning.value) {
        return toast.warning('Stop the active segment first');
      }
      await workSessionsApi.stop();
      toast.success('Workday stopped');
      await loadDashboard();
    } catch (e) {
      console.error(e);
      toast.danger(`Error: ${e instanceof ApiError ? e.message : 'Failed to stop workday'}`);
    }
  }

  // ---------- Actions: Segment ----------
  async function startSegment() {
    if (!isSessionRunning.value) return toast.warning('Start a work session first');
    if (isSegmentRunning.value) return toast.warning('A segment is already running. Stop it first.');

    const cId = Number(categoryId.value);
    const aId = Number(activityId.value);
    const msg = comment.value.trim();

    if (!cId) return toast.warning('Category is required');
    if (!aId) return toast.warning('Activity is required');

    try {
      await workSegmentsApi.start({ categoryId: cId, activityId: aId, comment: msg || null });
      toast.success('Segment started');
      await loadCurrentSegment();
      await loadSessionsFromReports();
    } catch (e) {
      console.error(e);
      toast.danger(`Error: ${e instanceof ApiError ? e.message : 'Failed to start segment'}`);
    }
  }

  async function stopSegment() {
    if (!currentSegment.value) return;

    const cId = categoryId.value ? Number(categoryId.value) : null;
    const aId = activityId.value ? Number(activityId.value) : null;
    const msg = comment.value.trim();

    try {
      await workSegmentsApi.stop({ categoryId: cId, activityId: aId, comment: msg || null });
      toast.success('Segment stopped');
      await loadCurrentSegment();
      await loadSessionsFromReports();
    } catch (e) {
      console.error(e);
      toast.danger(`Error: ${e instanceof ApiError ? e.message : 'Failed to stop segment'}`);
    }
  }

  // ---------- Lifecycle ----------
  onMounted(async () => {
    await loadDashboard();
  });

  onActivated(() => {
    refreshDashboard({ swallowErrors: true });
  });

  return {
    // state
    loading,
    error,
    headerClock,

    currentSession,
    sessions,
    currentSegment,

    daysCount,
    categoryId,
    activityId,
    comment,

    categories,
    activities,

    // derived
    isSessionRunning,
    isSegmentRunning,
    recentSessions,
    sessionRunningDuration,
    segmentRunningDuration,

    // actions
    loadDashboard,
    startDay,
    stopDay,
    startSegment,
    stopSegment,
  };
}
