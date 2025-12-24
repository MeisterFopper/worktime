<script setup>
import { useDashboardPage } from './composables/useDashboardPage';

import { useToast } from '@/shared/ui/toast';
import { TAXONOMY } from '@/features/taxonomy/taxonomy-config';
import { createTaxonomyApi } from '@/features/taxonomy/api/taxonomy-api';
import { workSessionsApi } from '@/features/report/sessions/api/work-sessions-api';
import { workSegmentsApi } from '@/features/report/segments/api/work-segments-api';
import { workReportsApi } from '@/features/report/reports/api/work-reports-api';

import WorkSessionCard from '@/features/dashboard/components/WorkSessionCard.vue';
import SegmentCard from '@/features/dashboard/components/WorkSegmentCard.vue';
import RecentWorktimesCard from '@/features/dashboard/components/RecentWorktimesCard.vue';

const categoriesApi = createTaxonomyApi(TAXONOMY.categories.endpoint);
const activitiesApi = createTaxonomyApi(TAXONOMY.activities.endpoint);

const toast = useToast();

const {
  loading,
  error,
  headerClock,

  currentSession,
  currentSegment,

  categories,
  activities,

  daysCount,
  categoryId,
  activityId,
  comment,

  isSessionRunning,
  isSegmentRunning,

  recentSessions,
  sessionRunningDuration,
  segmentRunningDuration,

  startDay,
  stopDay,
  startSegment,
  stopSegment,
} = useDashboardPage({
  toast,
  categoriesApi,
  activitiesApi,
  workSessionsApi,
  workSegmentsApi,
  workReportsApi,
});
</script>

<template>
  <div class="py-3">
    <h1 class="mb-4 d-flex align-items-center">
      <span>Worktime Dashboard</span>
      <span class="ms-auto text-muted fs-3">{{ headerClock }}</span>
    </h1>

    <div v-if="error" class="alert alert-danger">{{ error }}</div>

    <WorkSessionCard
      :loading="loading"
      :is-session-running="isSessionRunning"
      :is-segment-running="isSegmentRunning"
      :current-session="currentSession"
      @start-day="startDay"
      @stop-day="stopDay"
    />

    <SegmentCard
      :loading="loading"
      :is-session-running="isSessionRunning"
      :is-segment-running="isSegmentRunning"
      :categories="categories"
      :activities="activities"
      :current-segment="currentSegment"
      :segment-running-duration="segmentRunningDuration"
      v-model:categoryId="categoryId"
      v-model:activityId="activityId"
      v-model:comment="comment"
      @start-segment="startSegment"
      @stop-segment="stopSegment"
    />

    <RecentWorktimesCard
      :loading="loading"
      :is-session-running="isSessionRunning"
      :current-session="currentSession"
      :recent-sessions="recentSessions"
      :session-running-duration="sessionRunningDuration"
      v-model:daysCount="daysCount"
    />
  </div>
</template>
