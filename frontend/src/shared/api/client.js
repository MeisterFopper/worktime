//src/api/client.js
import { ApiError } from './api-error';

function formatProblemDetail(payload) {
  // RFC 7807-ish
  if (!payload || typeof payload !== 'object') return null;

  if (Array.isArray(payload.errors) && payload.errors.length > 0) {
    const first = payload.errors[0];
    if (first && typeof first === 'object') {
      const f = first.field ? `${first.field}: ` : '';
      const m = first.message || 'Invalid value';
      return `${f}${m}`;
    }
    return String(payload.errors[0]);
  }

  if (typeof payload.detail === 'string' && payload.detail.trim()) return payload.detail;
  if (typeof payload.title === 'string' && payload.title.trim()) return payload.title;

  if (typeof payload.message === 'string' && payload.message.trim()) return payload.message;
  if (typeof payload.error === 'string' && payload.error.trim()) return payload.error;

  return null;
}

function extractMessage(payload, fallback) {
  if (!payload) return fallback;
  if (typeof payload === 'string') return payload;

  const problemMsg = formatProblemDetail(payload);
  return problemMsg || fallback;
}

async function readJsonOrText(res) {
  const ct = (res.headers.get('content-type') || '').toLowerCase();
  if (ct.includes('application/json') || ct.includes('application/problem+json')) {
    try {
      return await res.json();
    } catch {
      // fall through to text
    }
  }
  const text = await res.text();
  return text || null;
}

export async function request(url, { method = 'GET', body, headers = {} } = {}) {
  const m = String(method || 'GET').toUpperCase();

  const options = {
    method: m,
    headers: {
      Accept: 'application/problem+json, application/json',
      ...headers,
    },
  };

  const canHaveBody = m !== 'GET' && m !== 'HEAD';
  const hasBody = body !== undefined && body !== null;

  if (canHaveBody && hasBody) {
    options.headers['Content-Type'] = 'application/json';
    options.body = JSON.stringify(body);
  }

  let res;
  try {
    res = await fetch(url, options);
  } catch (e) {
    throw new ApiError('Network error', {
      status: 0,
      url,
      method: m,
      payload: null,
    });
  }

  if (res.status === 204) return null;

  const payload = await readJsonOrText(res);

  if (!res.ok) {
    const fallback = res.statusText || 'Request failed';
    const msg = extractMessage(payload, fallback);

    throw new ApiError(msg, {
      status: res.status,
      statusText: res.statusText,
      url,
      method: m,
      payload,
    });
  }

  return payload;
}

export async function safeRequest(url, opts) {
  try {
    const data = await request(url, opts);
    return { data, error: null };
  } catch (error) {
    return { data: null, error };
  }
}
