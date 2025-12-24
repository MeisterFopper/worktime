//src/shared/api/api-config.js
export const API_VERSION = 'v1';
export const API_BASE = `/api/${API_VERSION}`;

export const API_CATEGORIES = `${API_BASE}/categories`;
export const API_ACTIVITIES = `${API_BASE}/activities`;
export const API_WORK_SESSIONS = `${API_BASE}/worksessions`;
export const API_WORK_SEGMENTS = `${API_BASE}/worksegments`;

export const API_WORK_REPORTS = `${API_BASE}/reports`;
export const API_WORK_REPORTS_DAYS = `${API_WORK_REPORTS}/days`;
export const API_WORK_REPORTS_EXPORT_PDF = `${API_WORK_REPORTS}/export.pdf`;