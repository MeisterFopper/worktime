// src/pages/shared/useTaxonomyPage.js
import { computed, onActivated, onMounted, ref } from 'vue';
import { ApiError } from '@/shared/api/api-error';
import { formatLocalDateTime } from '@/shared/utils/date';

export function useTaxonomyPage({
  api,
  labels = { entitySingular: 'Item', entityPlural: 'Items' },
  toasts,
  modals,
  initialFilterStatus = 'ACTIVE',
  rules = { nameMax: 200, descMax: 500, descRows: 4 },
  // Legacy behavior: if active is missing/null => treat as active
  legacyActiveDefault = true,
  // SPA/keep-alive behavior
  autoLoad = true,
  reloadOnActivated = false,
} = {}) {
  const newName = ref('');
  const newDesc = ref('');
  const filterStatus = ref(initialFilterStatus);

  const loading = ref(false);
  const error = ref('');

  // Source of truth for the page: keep one list, always sorted by name.
  const sortedAll = ref([]);

  // Prevent overlapping loads (helps when toggling routes quickly)
  let loadInFlight = null;

  /* ------------------------- small utilities ------------------------- */

  const entitySingularLower = () => String(labels.entitySingular || 'Item').toLowerCase();
  const entityPluralLower = () => String(labels.entityPlural || 'Items').toLowerCase();

  function toastWarning(msg) { toasts?.warning?.(msg); }
  function toastSuccess(msg) { toasts?.success?.(msg); }
  function toastDanger(msg)  { toasts?.danger?.(msg); }

  function errMessage(e, fallback) {
    return e instanceof ApiError ? e.message : fallback;
  }

  function dash(v) {
    const s = String(v ?? '').trim();
    return s ? s : '—';
  }

  function isActiveValue(item) {
    // Backward compatible: if active is missing/null => default
    const v = item?.active;
    if (v === true) return true;
    if (v === false) return false;
    return !!legacyActiveDefault;
  }

  function normalizeItem(item) {
    if (!item || typeof item !== 'object') return item;
    return { ...item, active: isActiveValue(item) };
  }

  function sortByName(list) {
    return (Array.isArray(list) ? [...list] : []).sort((a, b) =>
      String(a?.name ?? '').localeCompare(String(b?.name ?? ''))
    );
  }

  function findIndexById(id) {
    return sortedAll.value.findIndex(x => x?.id === id);
  }

  function replaceAt(idx, nextItem) {
    sortedAll.value[idx] = nextItem;
  }

  function resort() {
    sortedAll.value = sortByName(sortedAll.value);
  }

  function upsertSorted(item) {
    if (!item || item.id == null) return;

    const normalized = normalizeItem(item);
    const idx = findIndexById(normalized.id);

    if (idx >= 0) {
      replaceAt(idx, { ...sortedAll.value[idx], ...normalized });
      resort(); // name might have changed
      return;
    }

    sortedAll.value = sortByName([...sortedAll.value, normalized]);
  }

  function patchLocal(id, patch, { resortAfter = false } = {}) {
    const idx = findIndexById(id);
    if (idx < 0) return;

    replaceAt(idx, { ...sortedAll.value[idx], ...patch });

    if (resortAfter) resort();
  }

  // Helper: optimistic patch with revert on failure
  async function optimisticPatch(id, patch, apiPatchFn, { resortAfter = false, successMsg = '' } = {}) {
    const idx = findIndexById(id);
    if (idx < 0) return;

    const before = sortedAll.value[idx];

    patchLocal(id, patch, { resortAfter });

    try {
      await apiPatchFn();
      if (successMsg) toastSuccess(successMsg);
    } catch (e) {
      console.error(e);
      replaceAt(idx, before);
      toastDanger(`Error: ${errMessage(e, 'Request failed')}`);
    }
  }

  /* ------------------------- derived view ------------------------- */

  const visibleItems = computed(() => {
    const status = filterStatus.value;

    if (status === 'ALL') return sortedAll.value;

    const wantActive = status === 'ACTIVE';
    return sortedAll.value.filter(x => isActiveValue(x) === wantActive);
  });

  /* ------------------------- load ------------------------- */

  async function loadAll() {
    if (loadInFlight) return loadInFlight;

    loading.value = true;
    error.value = '';

    loadInFlight = (async () => {
      try {
        if (!api?.list) throw new Error('api.list is not configured');

        const data = await api.list({ status: 'ALL' });
        const normalized = (Array.isArray(data) ? data : []).map(normalizeItem);
        sortedAll.value = sortByName(normalized);
      } catch (e) {
        console.error(e);
        error.value = errMessage(e, `Failed to load ${entityPluralLower()}`);
        sortedAll.value = [];
      } finally {
        loading.value = false;
        loadInFlight = null;
      }
    })();

    return loadInFlight;
  }

  /* ------------------------- create ------------------------- */

  async function create() {
    const name = newName.value.trim();
    const description = newDesc.value.trim();

    if (!name) return toastWarning(`${labels.entitySingular} name is required`);

    const nameMax = rules.nameMax ?? 200;
    const descMax = rules.descMax ?? 500;

    if (name.length > nameMax) return toastWarning(`Name must be at most ${nameMax} characters`);
    if (description.length > descMax) return toastWarning(`Description must be at most ${descMax} characters`);

    try {
      if (!api?.create) throw new Error('api.create is not configured');

      const created = await api.create({
        name,
        description: description || null,
        active: true,
      });

      toastSuccess(`${labels.entitySingular} created`);
      newName.value = '';
      newDesc.value = '';

      // If backend returns created object => insert locally, else reload.
      if (created && typeof created === 'object') {
        upsertSorted(created);
      } else {
        await loadAll();
      }
    } catch (e) {
      console.error(e);
      toastDanger(`Error: ${errMessage(e, `Failed to create ${entitySingularLower()}`)}`);
    }
  }

  /* ------------------------- edit name/description ------------------------- */

  async function editName(item) {
    const id = item?.id;
    if (id == null) return;

    const current = String(item?.name ?? '');

    const res = await modals.openTextModal(`Edit ${labels.entitySingular} Name`, current, {
      placeholder: `${labels.entitySingular} name`,
      maxLength: rules.nameMax ?? 200,
    });
    if (res === null) return;

    const next = String(res).trim();
    if (!next) return toastWarning(`${labels.entitySingular} name is required`);
    if (next === current.trim()) return;

    await optimisticPatch(
      id,
      { name: next },
      () => api.patch(id, { name: next }),
      { resortAfter: true, successMsg: 'Name updated' }
    );
  }

  async function editDescription(item) {
    const id = item?.id;
    if (id == null) return;

    const current = String(item?.description ?? '');

    const res = await modals.openTextareaModal('Edit Description', current, {
      placeholder: `${labels.entitySingular} description`,
      maxLength: rules.descMax ?? 500,
      rows: rules.descRows ?? 4,
      submitOnEnter: false,
    });
    if (res === null) return;

    const next = String(res).trim();
    if (next === current.trim()) return;

    await optimisticPatch(
      id,
      { description: next },
      () => api.patch(id, { description: next }),
      { resortAfter: false, successMsg: 'Description updated' }
    );
  }

  /* ------------------------- info ------------------------- */

  async function showInfo(item) {
    const name = dash(item?.name)
    const created = dash(formatLocalDateTime(item?.createdAt));
    const updated = dash(formatLocalDateTime(item?.updatedAt));

    // Preferred: dedicated info modal (read-only)
    if (modals?.openInfoModal) {
      await modals.openInfoModal(`${labels.entitySingular} Info`, [
        { label: 'Name', value: name },
        { label: 'Created', value: created },
        { label: 'Last edit', value: updated },
      ]);
      return;
    }

    // Fallback: existing text modal
    await modals?.openTextModal?.(
      `${labels.entitySingular} Info`,
      `Created: ${created}\nLast edit: ${updated}`,
      { maxLength: 2000 }
    );
  }

  /* ------------------------- toggle active ------------------------- */

  async function toggleActive(item) {
    const id = item?.id;
    if (id == null) return;

    const idx = findIndexById(id);
    if (idx < 0) return;

    const before = sortedAll.value[idx];
    const nextActive = !isActiveValue(before);

    await optimisticPatch(
      id,
      { active: nextActive },
      () => api.patch(id, { active: nextActive }),
      { resortAfter: false, successMsg: 'Status updated' }
    );
  }

  if (autoLoad) {
    onMounted(loadAll);

    // If page is cached via <keep-alive>, refresh when user returns.
    if (reloadOnActivated) {
      onActivated(loadAll);
    }
  }

  return {
    // state
    newName,
    newDesc,
    filterStatus,
    loading,
    error,

    // data
    sortedAll,
    visibleItems,

    // actions
    loadAll,
    create,
    editName,
    editDescription,
    toggleActive,
    showInfo, // NEW
  };
}
