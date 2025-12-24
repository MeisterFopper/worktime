// use-modals.js
import { inject } from 'vue';
import { MODAL_KEY } from '../infra/modal-plugin';

export function useModals() {
  const modal = inject(MODAL_KEY);
  if (!modal) throw new Error('Modal service not installed');

  return {
    openDateTimeModal: (title, currentValue = '') =>
      modal.open('datetime', { title, currentValue }),

    openDateTimeRangeModal: (title, fromUtcIso = '', toUtcIso = '') =>
      modal.open('daterange', { title, fromUtcIso, toUtcIso }),

    openTextModal: (title, currentValue = '', opts = {}) =>
      modal.open('text', { title, currentValue, ...opts }),

    openTextareaModal: (title, currentValue = '', opts = {}) =>
      modal.open('textarea', { title, currentValue, ...opts }),

    openInfoModal: (title, lines = [], opts = {}) =>
      modal.open('info', { title, lines, ...opts }),
  };
}
