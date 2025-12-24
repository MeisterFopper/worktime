import { createModalService } from './modal-service';

export const MODAL_KEY = Symbol('modal');

export function createModalPlugin() {
  const service = createModalService();
  return {
    install(app) {
      app.provide(MODAL_KEY, service);
    },
  };
}
