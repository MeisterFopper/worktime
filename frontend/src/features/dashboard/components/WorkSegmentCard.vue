<script setup>
import { computed } from 'vue';
import { formatLocalDateTime, calcDurationDisplay } from '@/shared/utils/date';

const props = defineProps({
  loading: { type: Boolean, required: true },
  isSessionRunning: { type: Boolean, required: true },
  isSegmentRunning: { type: Boolean, required: true },

  categories: { type: Array, required: true },
  activities: { type: Array, required: true },

  categoryId: { type: String, required: true },
  activityId: { type: String, required: true },
  comment: { type: String, required: true },

  currentSegment: { type: Object, default: null },
  segmentRunningDuration: { type: String, default: '' },
});

const emit = defineEmits([
  'update:categoryId',
  'update:activityId',
  'update:comment',
  'start-segment',
  'stop-segment',
]);

const formDisabled = computed(() => !props.isSessionRunning || props.loading);
</script>

<template>
  <div class="card mb-3 shadow-sm">
    <div class="card-header bg-dark text-white d-flex align-items-center">
      <h2 class="mb-0 fs-5">Work Segment</h2>
    </div>

    <div class="card-body">
      <div class="row g-2 align-items-end mb-3">
        <div class="col-md-3">
          <label class="form-label mb-1">Category</label>
          <select
            class="form-select"
            :disabled="formDisabled"
            :value="categoryId"
            @change="emit('update:categoryId', $event.target.value)"
          >
            <option value="" disabled>Select category</option>
            <option v-for="c in categories" :key="c.id" :value="String(c.id)">
              {{ c.name }}
            </option>
          </select>
        </div>

        <div class="col-md-3">
          <label class="form-label mb-1">Activity</label>
          <select
            class="form-select"
            :disabled="formDisabled"
            :value="activityId"
            @change="emit('update:activityId', $event.target.value)"
          >
            <option value="" disabled>Select activity</option>
            <option v-for="a in activities" :key="a.id" :value="String(a.id)">
              {{ a.name }}
            </option>
          </select>
        </div>

        <div class="col-md-4">
          <label class="form-label mb-1">Comment</label>
          <input
            class="form-control"
            maxlength="500"
            placeholder="Optional comment"
            :disabled="formDisabled"
            :value="comment"
            @input="emit('update:comment', $event.target.value)"
          />
        </div>

        <div class="col-md-1 d-grid">
          <button
            class="btn btn-success"
            title="Start segment"
            :disabled="loading || !isSessionRunning || isSegmentRunning"
            @click="emit('start-segment')"
          >
            <i class="bi bi-play-fill"></i>
          </button>
        </div>

        <div class="col-md-1 d-grid">
          <button
            class="btn btn-danger"
            title="Stop segment"
            :disabled="loading || !isSessionRunning || !isSegmentRunning"
            @click="emit('stop-segment')"
          >
            <i class="bi bi-stop-fill"></i>
          </button>
        </div>
      </div>

      <table class="table table-striped align-middle mb-0">
        <thead>
          <tr>
            <th style="width:30%">Start</th>
            <th style="width:30%">End</th>
            <th style="width:30%">Duration</th>
            <th style="width:10%"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!isSessionRunning">
            <td colspan="4" class="text-center text-muted">Start a work session to track segments</td>
          </tr>

          <tr v-else-if="!currentSegment">
            <td colspan="4" class="text-center text-muted">No segment running</td>
          </tr>

          <tr v-else>
            <td>{{ formatLocalDateTime(currentSegment.startTime) }}</td>
            <td>
              <span v-if="currentSegment.endTime">{{ formatLocalDateTime(currentSegment.endTime) }}</span>
              <em v-else>running…</em>
            </td>
            <td>
              <span v-if="currentSegment.endTime">
                {{ calcDurationDisplay(currentSegment.startTime, currentSegment.endTime) }}
              </span>
              <span v-else>{{ segmentRunningDuration }}</span>
            </td>
              <td></td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
