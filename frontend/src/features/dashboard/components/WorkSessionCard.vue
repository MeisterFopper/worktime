<script setup>
import { formatLocalDateTime } from '@/shared/utils/date';

const props = defineProps({
  loading: { type: Boolean, required: true },
  isSessionRunning: { type: Boolean, required: true },
  isSegmentRunning: { type: Boolean, required: true },
  currentSession: { type: Object, default: null },
});

const emit = defineEmits(['start-day', 'stop-day']);
</script>

<template>
  <div class="card mb-3 shadow-sm">
    <div class="card-body p-2">
      <table class="table table-borderless mb-0 align-middle text-center">
        <thead>
          <tr>
            <th style="width:15%">
              <button
                class="btn btn-success w-100 py-3"
                :disabled="loading || isSessionRunning"
                @click="emit('start-day')"
              >
                <i class="bi bi-play-circle"></i> Start
              </button>
            </th>

            <th style="width:15%">
              <button
                class="btn btn-danger w-100 py-3"
                :disabled="loading || !isSessionRunning || isSegmentRunning"
                @click="emit('stop-day')"
              >
                <i class="bi bi-stop-circle"></i> Stop
              </button>
            </th>

            <th class="text-center align-middle">
              <span
                class="fw-semibold fs-3"
                :class="isSessionRunning ? 'text-success' : 'text-muted'"
              >
                <template v-if="loading">Loading…</template>
                <template v-else-if="isSessionRunning">
                  Currently working since {{ formatLocalDateTime(currentSession?.startTime) }}
                </template>
                <template v-else>Not working</template>
              </span>
            </th>
          </tr>
        </thead>
      </table>
    </div>
  </div>
</template>
