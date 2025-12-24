import { createApp } from 'vue';
import App from './App.vue';

import { router } from './router';
import { createModalPlugin } from '@/shared/ui/modals';

import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import 'bootstrap-icons/font/bootstrap-icons.css';

import '@/shared/styles/app.css';

createApp(App)
  .use(router)
  .use(createModalPlugin())
  .mount('#app');
