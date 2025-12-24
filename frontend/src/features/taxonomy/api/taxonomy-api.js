import { request } from '@/shared/api/client';

/**
 * Generic "taxonomy-like" CRUD API:
 * - list({status}) with optional status query param (except ALL)
 * - create(dto)
 * - patch(id, patch)
 */
export function createTaxonomyApi(baseUrl) {
  return {
    list: ({ status } = {}) => {
      const params = new URLSearchParams();
      if (status && status !== 'ALL') params.set('status', status);
      const qs = params.toString();
      return request(`${baseUrl}${qs ? `?${qs}` : ''}`);
    },

    create: (dto) => request(baseUrl, { method: 'POST', body: dto }),

    patch: (id, patch) => request(`${baseUrl}/${id}`, { method: 'PATCH', body: patch }),
  };
}
