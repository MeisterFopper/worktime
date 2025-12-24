// src/utils/useLiveTicker.js
import { ref, onMounted, onBeforeUnmount, onActivated, onDeactivated } from 'vue';

/**
 * Generic live ticker for "current time" strings (or any computed value).
 *
 * - keep-alive safe (starts onActivated, stops onDeactivated)
 * - optionally pauses while tab is hidden
 * - triggers an immediate tick when tab becomes visible again
 */
export function useLiveTicker({
  getValue,
  intervalMs = 1000,
  pauseWhenHidden = true,
  tickOnStart = true,
} = {}) {
  if (typeof getValue !== 'function') {
    throw new Error('useLiveTicker: getValue must be a function');
  }

  const value = ref(getValue());

  let timer = null;

  function tick() {
    value.value = getValue();
  }

  function start() {
    if (timer) return;

    if (tickOnStart) tick();

    timer = setInterval(() => {
      if (pauseWhenHidden && document.hidden) return;
      tick();
    }, intervalMs);
  }

  function stop() {
    if (!timer) return;
    clearInterval(timer);
    timer = null;
  }

  const onVisibilityChange = () => {
    if (!document.hidden) tick();
  };

  onMounted(() => {
    document.addEventListener('visibilitychange', onVisibilityChange);
    start();
  });

  onActivated(() => {
    start();
  });

  onDeactivated(() => {
    stop();
  });

  onBeforeUnmount(() => {
    stop();
    document.removeEventListener('visibilitychange', onVisibilityChange);
  });

  return {
    value,  // ref
    tick,   // manual refresh
    start,
    stop,
  };
}
