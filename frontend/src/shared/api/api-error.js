//src/api/api-error.js
export class ApiError extends Error {
  constructor(message, { status, statusText, url, method, payload } = {}) {
    super(message);
    this.name = 'ApiError';
    this.status = status ?? 0;
    this.statusText = statusText ?? '';
    this.url = url ?? '';
    this.method = method ?? '';
    this.payload = payload;
  }
}
