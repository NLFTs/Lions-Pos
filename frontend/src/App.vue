<script setup>
import NotificationProvider from '@/components/ui/NotificationProvider.vue'
import LoadingBar from '@/components/ui/LoadingBar.vue'
</script>

<template>
  <LoadingBar />
  <RouterView :key="$route.fullPath" />
  <NotificationProvider />
</template>
