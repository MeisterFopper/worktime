import { API_ACTIVITIES, API_CATEGORIES } from '@/shared/api/api-config';

export const TAXONOMY = {
  categories: {
    endpoint: API_CATEGORIES,
    title: 'Categories',
    labels: { entitySingular: 'Category', entityPlural: 'Categories' },
  },
  activities: {
    endpoint: API_ACTIVITIES,
    title: 'Activities',
    labels: { entitySingular: 'Activity', entityPlural: 'Activities' },
  },
};

export function getTaxonomy(kind) {
  const cfg = TAXONOMY[kind];
  if (!cfg) throw new Error(`Unknown taxonomy kind: ${kind}`);
  return cfg;
}
