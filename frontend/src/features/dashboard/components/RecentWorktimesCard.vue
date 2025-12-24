<script setup>
import { formatLocalDateTime, calcDurationDisplay } from '@/shared/utils/date';

const props = defineProps({
  loading: { type: Boolean, required: true },
  isSessionRunning: { type: Boolean, required: true },

  currentSession: { type: Object, default: null },
  recentSessions: { type: Array, required: true },

  daysCount: { type: Number, required: true },
  sessionRunningDuration: { type: String, default: '' },
});

const emit = defineEmits(['update:daysCount']);
</script>

<template>
  <div class="card shadow-sm mb-4">
    <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center">
      <h2 class="mb-0 fs-5">Recent Worktimes</h2>

      <div class="input-group input-group-sm" style="width:220px;">
        <span class="input-group-text">Show</span>
        <input
          type="number"
          class="form-control"
          min="1"
          :value="daysCount"
          @input="emit('update:daysCount', Number($event.target.value))"
        />
        <span class="input-group-text">rows</span>
      </div>
    </div>

    <div class="card-body">
      <table class="table table-striped align-middle mb-0">
        <thead>
          <tr>
            <th style="width:30%">Start</th>
            <th style="width:30%">End</th>
            <th style="width:30%">Duration</th>
            <th style="width:10%">Segments</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="4" class="text-center text-muted">Loading…</td>
          </tr>

          <template v-else>
            <tr v-if="isSessionRunning" class="table-success">
              <td>{{ formatLocalDateTime(currentSession?.startTime) }}</td>
              <td><em>running…</em></td>
              <td>{{ sessionRunningDuration }}</td>
              <td>{{ (currentSession?.items?.length ?? 0) }}</td>
            </tr>

            <tr v-if="!isSessionRunning && recentSessions.length === 0">
              <td colspan="4" class="text-center text-muted">No work sessions found</td>
            </tr>

            <tr v-for="s in recentSessions" :key="s.id ?? s.startTime">
              <td>{{ formatLocalDateTime(s.startTime) }}</td>
              <td>{{ formatLocalDateTime(s.endTime) }}</td>
              <td>{{ calcDurationDisplay(s.startTime, s.endTime) }}</td>
              <td>{{ (s?.items?.length ?? 0) }}</td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>
  </div>
</template>
