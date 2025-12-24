import { createRouter, createWebHistory } from 'vue-router';

import DashboardPage from '@/features/dashboard/DashboardPage.vue';
import ReportPage from '@/features/report/ReportPage.vue';
import TaxonomyPage from '@/features/taxonomy/TaxonomyPage.vue';

const routes = [
  { path: '/', name: 'dashboard', component: DashboardPage, meta: { keepAlive: true } },
  { path: '/report', name: 'report', component: ReportPage, meta: { keepAlive: true } },

  { path: '/categories', name: 'categories', component: TaxonomyPage, props: { kind: 'categories' }, meta: { keepAlive: true } },
  { path: '/activities', name: 'activities', component: TaxonomyPage, props: { kind: 'activities' }, meta: { keepAlive: true } },
];

export const router = createRouter({
  history: createWebHistory(),
  routes,
});
