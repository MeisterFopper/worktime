<script setup>
import Navbar from './components/Navbar.vue';
import { ToastHost } from '@/shared/ui/toast';
import { ModalHost } from '@/shared/ui/modals';
</script>

<template>
  <div class="bg-light min-vh-100">
    <Navbar />

    <section class="container mb-5">
      <router-view v-slot="{ Component, route }">
        <keep-alive v-if="route.meta.keepAlive">
          <component :is="Component" :key="route.name" />
        </keep-alive>
        <component v-else :is="Component" :key="route.name" />
      </router-view>
    </section>

    <ToastHost />
    <ModalHost />
  </div>
</template>
