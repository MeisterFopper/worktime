// src/pages/report/composables/useReportMutations.js
import { request } from '@/shared/api/client';
import { API_WORK_SESSIONS, API_WORK_SEGMENTS } from '@/shared/api/api-config';

export function useReportMutations({
  toast,
  modals,
  workSessionsApi,
  workSegmentsApi,
  loadReport,
}) {
  async function patchWorkSession(id, payload) {
    if (workSessionsApi?.patch) return workSessionsApi.patch(id, payload);
    if (workSessionsApi?.update) return workSessionsApi.update(id, payload);
    return request(`${API_WORK_SESSIONS}/${id}`, { method: 'PATCH', body: payload });
  }

  async function deleteWorkSession(id) {
    if (!confirm('Delete this work session?')) return;
    try {
      if (workSessionsApi?.remove) await workSessionsApi.remove(id);
      else if (workSessionsApi?.delete) await workSessionsApi.delete(id);
      else await request(`${API_WORK_SESSIONS}/${id}`, { method: 'DELETE' });

      toast?.success?.('Work session deleted');
      loadReport();
    } catch (e) {
      console.error(e);
      toast?.danger?.('Failed to delete work session');
    }
  }

  async function patchWorkSegment(id, payload) {
    if (workSegmentsApi?.patch) return workSegmentsApi.patch(id, payload);
    if (workSegmentsApi?.update) return workSegmentsApi.update(id, payload);
    return request(`${API_WORK_SEGMENTS}/${id}`, { method: 'PATCH', body: payload });
  }

  async function deleteWorkSegment(id) {
    if (!confirm('Delete this work segment?')) return;
    try {
      if (workSegmentsApi?.remove) await workSegmentsApi.remove(id);
      else if (workSegmentsApi?.delete) await workSegmentsApi.delete(id);
      else await request(`${API_WORK_SEGMENTS}/${id}`, { method: 'DELETE' });

      toast?.success?.('Work segment deleted');
      loadReport();
    } catch (e) {
      console.error(e);
      toast?.danger?.('Failed to delete work segment');
    }
  }

  async function requestEditSessionStart(sessionId, currentIso) {
    const newIso = await modals.openDateTimeModal('Adjust START time', currentIso || '');
    if (!newIso) return;

    try {
      await patchWorkSession(sessionId, { startTime: newIso });
      toast?.success?.('Start time updated');
      loadReport();
    } catch (e) {
      console.error(e);
      toast?.danger?.('Failed to update start time');
    }
  }

  async function requestEditSessionEnd(sessionId, currentIso) {
    const newIso = await modals.openDateTimeModal('Adjust END time', currentIso || '');
    if (!newIso) return;

    try {
      await patchWorkSession(sessionId, { endTime: newIso });
      toast?.success?.('End time updated');
      loadReport();
    } catch (e) {
      console.error(e);
      toast?.danger?.('Failed to update end time');
    }
  }

  async function requestEditSegmentStart(segmentId, currentIso) {
    const newIso = await modals.openDateTimeModal('Adjust SEGMENT start time', currentIso || '');
    if (!newIso) return;

    try {
      await patchWorkSegment(segmentId, { startTime: newIso });
      toast?.success?.('Segment start updated');
      loadReport();
    } catch (e) {
      console.error(e);
      toast?.danger?.('Failed to update segment start');
    }
  }

  async function requestEditSegmentEnd(segmentId, currentIso) {
    const newIso = await modals.openDateTimeModal('Adjust SEGMENT end time', currentIso || '');
    if (!newIso) return;

    try {
      await patchWorkSegment(segmentId, { endTime: newIso });
      toast?.success?.('Segment end updated');
      loadReport();
    } catch (e) {
      console.error(e);
      toast?.danger?.('Failed to update segment end');
    }
  }

  return {
    requestEditSessionStart,
    requestEditSessionEnd,
    deleteWorkSession,
    requestEditSegmentStart,
    requestEditSegmentEnd,
    deleteWorkSegment,
  };
}
