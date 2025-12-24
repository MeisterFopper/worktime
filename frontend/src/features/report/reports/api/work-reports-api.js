// src/api/work-reports-api.js
import { request } from '@/shared/api/client';
import { API_WORK_REPORTS_DAYS, API_WORK_REPORTS_EXPORT_PDF } from '@/shared/api/api-config';

function withQuery(url, params = {}) {
  const qs = new URLSearchParams();

  Object.entries(params || {}).forEach(([k, v]) => {
    if (v === undefined || v === null) return;

    // keep booleans/numbers, drop empty strings
    const s = typeof v === 'string' ? v.trim() : String(v);
    if (!s) return;

    qs.set(k, s);
  });

  const q = qs.toString();
  return q ? `${url}?${q}` : url;
}

export const workReportsApi = {
  days(params = {}) {
    return request(withQuery(`${API_WORK_REPORTS_DAYS}`, params));
  },

  // For "open in new tab" – returns URL only (backend endpoint comes later)
  exportPdfUrl(params = {}) {
    return withQuery(`${API_WORK_REPORTS_EXPORT_PDF}`, params);
  },
};
