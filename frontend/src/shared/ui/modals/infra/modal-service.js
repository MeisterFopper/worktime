import { shallowRef } from 'vue';

function createDeferred() {
  let resolve;
  const promise = new Promise((r) => (resolve = r));
  return { promise, resolve };
}

export function createModalService() {
  const active = shallowRef(null); // { type, props, deferred }

  function close(value = null) {
    const cur = active.value;
    if (!cur) return;
    active.value = null;
    cur.deferred.resolve(value);
  }

  function open(type, props = {}) {
    // If something is open, cancel it
    if (active.value) close(null);

    const deferred = createDeferred();
    active.value = { type, props, deferred };
    return deferred.promise;
  }

  return { active, open, close };
}
