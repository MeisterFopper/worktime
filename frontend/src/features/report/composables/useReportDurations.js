// src/pages/report/composables/useReportDurations.js
import { diffSeconds } from '@/shared/utils/date';

export function useReportDurations({ nowTickUtc }) {
  function sessionEndIso(session) {
    return session?.endTime || nowTickUtc.value;
  }

  function sessionSeconds(session) {
    return diffSeconds(session?.startTime, sessionEndIso(session));
  }

  function segmentSeconds(seg) {
    return diffSeconds(seg?.startTime, seg?.endTime || nowTickUtc.value);
  }

  function sumSegmentSeconds(items) {
    return (Array.isArray(items) ? items : []).reduce(
      (sum, seg) => sum + segmentSeconds(seg),
      0,
    );
  }

  function unallocatedSeconds(session) {
    const total = sessionSeconds(session);
    const segSum = sumSegmentSeconds(session?.items);
    return Math.max(0, total - segSum);
  }

  return {
    sessionEndIso,
    sessionSeconds,
    segmentSeconds,
    sumSegmentSeconds,
    unallocatedSeconds,
  };
}
