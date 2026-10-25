<template>
  <div class="app-shell">
    <AppHeader v-if="!route.meta.hideChrome" />
    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
import AppHeader from './components/AppHeader.vue'

const route = useRoute()
</script>

<style>
.app-shell {
  min-height: 100vh;
  background: var(--bg);
}

.app-main {
  min-height: calc(100vh - 62px);
}
</style>
