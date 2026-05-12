<script setup>
import NotificationProvider from '@/components/ui/NotificationProvider.vue'
</script>

<template>
  <RouterView :key="$route.fullPath" />
  <NotificationProvider />
</template>
