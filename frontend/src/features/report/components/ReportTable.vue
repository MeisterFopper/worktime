<script setup>
import { IconButton } from '@/shared/ui/buttons';

defineProps({
  loading: { type: Boolean, default: false },
  days: { type: Array, default: () => [] },
});

const emit = defineEmits([
  'edit-session-start',
  'edit-session-end',
  'delete-session',
  'info-session',
  'edit-segment-start',
  'edit-segment-end',
  'delete-segment',
  'info-segment',
]);

function onEditSessionStart(s) {
  emit('edit-session-start', { id: s.id, current: s.startTime });
}
function onEditSessionEnd(s) {
  emit('edit-session-end', { id: s.id, current: s.endTime });
}
function onDeleteSession(s) {
  emit('delete-session', s.id);
}
function onInfoSession(s) {
  emit('info-session', s);
}

function onEditSegmentStart(seg) {
  emit('edit-segment-start', { id: seg.id, current: seg.startTime });
}
function onEditSegmentEnd(seg) {
  emit('edit-segment-end', { id: seg.id, current: seg.endTime });
}
function onDeleteSegment(seg) {
  emit('delete-segment', seg.id);
}
function onInfoSegment(seg) {
  emit('info-segment', seg);
}
</script>

<template>
  <table class="table table-hover align-middle">
    <thead class="table-dark">
      <tr>
        <th style="width:6%"></th>
        <th style="width:16%">Start</th>
        <th style="width:16%">End</th>
        <th style="width:56%">Duration</th>
        <th style="width:6%" class="text-center">Info</th>
      </tr>
    </thead>

    <tbody>
      <tr v-if="loading">
        <td colspan="5" class="text-center text-muted">Loading…</td>
      </tr>

      <tr v-else-if="!days || days.length === 0">
        <td colspan="5" class="text-center text-muted">No work sessions found</td>
      </tr>

      <template v-else>
        <template v-for="day in days" :key="day.dayUtc">
          <!-- Day header -->
          <tr class="table-secondary">
            <td colspan="5" class="fw-bold">
              <span>{{ day.dayLabel }}</span>
              <span class="float-end text-muted small">
                Total: {{ day.totals.total }} |
                Segments: {{ day.totals.segments }} |
                Unallocated: {{ day.totals.unallocated }}
              </span>
            </td>
          </tr>

          <!-- Sessions -->
          <template v-for="s in day.sessions" :key="s.id ?? s.startTime">
            <tr class="work-session-row" :class="s.sessionRowClass">
              <!-- delete session -->
              <td class="text-center">
                <IconButton
                  title="Delete session"
                  icon="bi-trash"
                  kind="delete"
                  :disabled="loading"
                  @click="onDeleteSession(s)"
                />
              </td>

              <!-- session start -->
              <td class="text-nowrap">
                <span class="me-2">{{ s.startLabel }}</span>
                <IconButton
                  title="Edit start"
                  icon="bi-pencil"
                  kind="edit"
                  :disabled="loading"
                  @click="onEditSessionStart(s)"
                />
              </td>

              <!-- session end -->
              <td class="text-nowrap">
                <span class="me-2">
                  <span v-if="s.endLabel">{{ s.endLabel }}</span>
                  <em v-else>running…</em>
                </span>

                <IconButton
                  v-if="!s.running"
                  title="Edit end"
                  icon="bi-pencil"
                  kind="edit"
                  :disabled="loading"
                  @click="onEditSessionEnd(s)"
                />
              </td>

              <!-- session duration -->
              <td>
                <div class="text-muted">{{ s.durationTop }}</div>
                <div class="text-muted small">
                  Segments: {{ s.segmentsSummary }} | Unallocated: {{ s.unallocatedSummary }}
                </div>
              </td>

              <!-- session info -->
              <td class="text-center">
                <IconButton
                  title="Session info"
                  icon="bi-info-circle"
                  kind="info"
                  :disabled="loading"
                  @click="onInfoSession(s)"
                />
              </td>
            </tr>

            <!-- Segments -->
            <tr
              v-for="seg in s.segments"
              :key="seg.id ?? seg.startTime"
              class="table-light work-segment-row"
              :class="[s.groupClass, seg.isLast ? 'group-end' : '']"
            >
              <!-- delete segment -->
              <td class="text-center">
                <IconButton
                  title="Delete segment"
                  icon="bi-trash"
                  kind="delete"
                  :disabled="loading"
                  @click="onDeleteSegment(seg)"
                />
              </td>

              <!-- segment start -->
              <td class="text-nowrap">
                <span class="me-2">{{ seg.startLabel }}</span>
                <IconButton
                  title="Edit segment start"
                  icon="bi-pencil"
                  kind="edit"
                  :disabled="loading"
                  @click="onEditSegmentStart(seg)"
                />
              </td>

              <!-- segment end -->
              <td class="text-nowrap">
                <span class="me-2">
                  <span v-if="seg.endLabel">{{ seg.endLabel }}</span>
                  <em v-else>running…</em>
                </span>

                <IconButton
                  v-if="!seg.running"
                  title="Edit segment end"
                  icon="bi-pencil"
                  kind="edit"
                  :disabled="loading"
                  @click="onEditSegmentEnd(seg)"
                />
              </td>

              <!-- segment duration/meta -->
              <td>
                <!-- line 1 -->
                <div class="text-muted">{{ seg.durationLabel }}</div>

                <!-- line 2: meta + comment (comment truncated only) -->
                <div
                  v-if="seg.metaLabel"
                  class="text-muted small text-truncate seg-line"
                  :title="seg.metaLabel"
                >
                  {{ seg.metaLabel }}
                </div>
              </td>

              <!-- segment info -->
              <td class="text-center">
                <IconButton
                  title="Segment info"
                  icon="bi-info-circle"
                  kind="info"
                  :disabled="loading"
                  @click="onInfoSegment(seg)"
                />
              </td>
            </tr>
          </template>
        </template>
      </template>
    </tbody>
  </table>
</template>
