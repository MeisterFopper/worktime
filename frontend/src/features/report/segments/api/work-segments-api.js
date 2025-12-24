// src/api/work-segments-api.js
import { request } from '@/shared/api/client';
import { API_WORK_SEGMENTS } from '@/shared/api/api-config';

export const workSegmentsApi = {
  current: () => request(`${API_WORK_SEGMENTS}/current`),
  start: (dto) => request(`${API_WORK_SEGMENTS}/start`, { method: 'POST', body: dto }),
  stop: (dto) => request(`${API_WORK_SEGMENTS}/stop`, { method: 'POST', body: dto }),
};
