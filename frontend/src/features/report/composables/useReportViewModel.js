// src/pages/report/composables/useReportViewModel.js
import { computed } from 'vue';
import {
  diffSeconds,
  formatDuration,
  formatLocalDateTime,
  formatDayLabel,
  utcDayToUtcMidnightIso,
} from '@/shared/utils/date';

export function useReportViewModel({ daysRaw, showSegments, durations }) {
  const daysVm = computed(() => {
    const days = Array.isArray(daysRaw.value) ? daysRaw.value : [];

    let groupIndex = 0;

    return days.map((day) => {
      const sessions = Array.isArray(day?.sessions) ? day.sessions : [];
      const orderedSessions = sessions;

      const totalSessionSecs = orderedSessions.reduce((sum, s) => sum + durations.sessionSeconds(s), 0);
      const totalSegmentSecs = orderedSessions.reduce((sum, s) => sum + durations.sumSegmentSeconds(s?.items), 0);
      const totalUnallocSecs = Math.max(0, totalSessionSecs - totalSegmentSecs);

      const sessionsVm = orderedSessions.map((s) => {
        const running = !s?.endTime;

        const groupClass = (groupIndex % 2 === 0) ? 'group-even' : 'group-odd';
        groupIndex += 1;

        const items = Array.isArray(s?.items) ? s.items : [];
        const hasSegments = items.length > 0;
        const willRenderSegments = showSegments.value && hasSegments;

        const segSecs = durations.sumSegmentSeconds(items);
        const unallocSecs = durations.unallocatedSeconds(s);

        const segmentsVm = willRenderSegments
          ? items.map((seg, idx) => {
              const categoryName = seg.categoryName || '';
              const activityName = seg.activityName || '';
              const comment = (seg.comment || '').trim();

              const metaParts = [categoryName, activityName, comment].filter(Boolean);
              const metaLabel = metaParts.join(' · ');

              return {
                id: seg.id,
                startTime: seg.startTime,
                endTime: seg.endTime,
                running: !seg.endTime,

                startLabel: formatLocalDateTime(seg.startTime),
                endLabel: seg.endTime ? formatLocalDateTime(seg.endTime) : '',

                durationLabel: seg.endTime
                  ? formatDuration(diffSeconds(seg.startTime, seg.endTime))
                  : formatDuration(durations.segmentSeconds(seg)),

                // individual values (for info box etc.)
                categoryName,
                activityName,
                comment,
                // combined (for table row)
                metaLabel,

                isLast: idx === items.length - 1,
              };
            })
          : [];

        return {
          id: s.id,
          startTime: s.startTime,
          endTime: s.endTime,
          running,

          startLabel: formatLocalDateTime(s.startTime),
          endLabel: s.endTime ? formatLocalDateTime(s.endTime) : '',

          durationTop: running
            ? formatDuration(durations.sessionSeconds(s))
            : formatDuration(diffSeconds(s.startTime, s.endTime)),

          segmentCount: items.length,

          segmentsSummary: formatDuration(segSecs),
          unallocatedSummary: formatDuration(unallocSecs),

          groupClass,
          sessionRowClass: willRenderSegments ? groupClass : `${groupClass} group-end`,
          segments: segmentsVm,
        };
      });

      const dayLabelIso = utcDayToUtcMidnightIso(day?.dayUtc);

      return {
        dayUtc: day?.dayUtc,
        dayLabel: formatDayLabel(dayLabelIso),
        totals: {
          total: formatDuration(totalSessionSecs),
          segments: formatDuration(totalSegmentSecs),
          unallocated: formatDuration(totalUnallocSecs),
        },
        sessions: sessionsVm,
      };
    });
  });

  return { daysVm };
}
