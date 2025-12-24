import { DateTime } from 'luxon';

export const LOCAL_TZ = Intl.DateTimeFormat().resolvedOptions().timeZone;
export const DEFAULT_LOCALE = 'de-DE';

/* ---------- INTERNAL ---------- */
function parseUtc(iso) {
  if (!iso) return null;
  return DateTime.fromISO(iso, { zone: 'utc' });
}

/* ---------- UTC NOW ---------- */
export function nowUtcIso() {
  return DateTime.now().toUTC().toISO({ suppressSeconds: false, suppressMilliseconds: false });
}

/* ---------- FORMATTING ---------- */
export function formatLocalDateTime(iso, locale = DEFAULT_LOCALE) {
  const dt = parseUtc(iso);
  if (!dt?.isValid) return '';
  return dt.setZone(LOCAL_TZ).setLocale(locale).toFormat('dd.MM.yy HH:mm:ss');
}

export function nowLocalFormatted(locale = DEFAULT_LOCALE) {
  return DateTime.now().setZone(LOCAL_TZ).setLocale(locale).toFormat('dd.MM.yy HH:mm:ss');
}

export function formatDayLabel(iso, locale = DEFAULT_LOCALE) {
  const dt = parseUtc(iso);
  if (!dt?.isValid) return '';
  return dt.setZone(LOCAL_TZ).setLocale(locale).toFormat('cccc, dd.LL.yyyy');
}

/* ---------- INPUT CONVERSIONS ---------- */
export function localInputToUtcIso(localStr) {
  if (!localStr) return '';
  const local = DateTime.fromISO(localStr, { zone: LOCAL_TZ });
  if (!local.isValid) return '';
  return local.toUTC().toISO({ suppressSeconds: false, suppressMilliseconds: false });
}

export function utcIsoToLocalInput(iso) {
  const dt = parseUtc(iso);
  if (!dt?.isValid) return '';
  return dt.setZone(LOCAL_TZ).toFormat("yyyy-LL-dd'T'HH:mm:ss");
}

/* ---------- DURATION ---------- */
export function diffSeconds(startIso, endIso) {
  const start = parseUtc(startIso);
  const end = parseUtc(endIso);
  if (!start?.isValid || !end?.isValid) return 0;

  const secs = end.diff(start, 'seconds').seconds;
  return Math.max(0, Math.floor(secs));
}

export function formatDuration(secs) {
  const total = Math.max(0, Math.floor(secs || 0));
  const h = Math.floor(total / 3600);
  const m = Math.floor((total % 3600) / 60);
  const s = total % 60;
  return `${h}h ${String(m).padStart(2, '0')}m ${String(s).padStart(2, '0')}s`;
}

export function calcDurationDisplay(startIso, endIso) {
  return formatDuration(diffSeconds(startIso, endIso));
}

/* ---------- RANGE ---------- */
export function computeLocalDayRangeUtc({ daysBack = 7, daysForward = 0 } = {}) {
  const base = DateTime.now().setZone(LOCAL_TZ);

  const from = base.minus({ days: daysBack }).startOf('day').toUTC().toISO({
    suppressSeconds: false,
    suppressMilliseconds: false,
  });

  // end of the day `daysForward` days ahead
  const to = base.plus({ days: daysForward }).endOf('day').toUTC().toISO({
    suppressSeconds: false,
    suppressMilliseconds: false,
  });

  return { from, to };
}

export function utcDayToUtcMidnightIso(dayUtc) {
  if (!dayUtc) return '';
  const dt = DateTime.fromISO(dayUtc, { zone: 'utc' });
  if (!dt.isValid) return '';
  return dt.startOf('day').toISO({ suppressSeconds: false, suppressMilliseconds: false });
}

/* ---------- COMPARISON ---------- */
export function utcIsoToMillis(iso) {
  const dt = parseUtc(iso);
  return dt?.isValid ? dt.toMillis() : NaN;
}

export function isSameInstantUtcIso(a, b) {
  if (!a && !b) return true;
  if (!a || !b) return false;

  const am = utcIsoToMillis(a);
  const bm = utcIsoToMillis(b);

  return Number.isFinite(am) && Number.isFinite(bm) && am === bm;
}
