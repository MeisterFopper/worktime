// src/api/work-sessions-api.js
import { request } from '@/shared/api/client';
import { API_WORK_SESSIONS } from '@/shared/api/api-config';

export const workSessionsApi = {
  current: () => request(`${API_WORK_SESSIONS}/current`),
  list: () => request(API_WORK_SESSIONS),
  start: () => request(`${API_WORK_SESSIONS}/start`, { method: 'POST', body: {} }),
  stop: () => request(`${API_WORK_SESSIONS}/stop`, { method: 'POST', body: {} }),
};
