<script setup>
defineProps({
  title: { type: String, required: true },
});
</script>

<template>
  <div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="mb-0">{{ title }}</h1>
    <div class="d-flex align-items-center gap-2">
      <slot />
    </div>
  </div>
</template>
