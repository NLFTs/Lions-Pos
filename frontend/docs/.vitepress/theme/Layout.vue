<script setup>
import DefaultTheme from 'vitepress/theme'
import Sponsors from './Sponsors.vue'

const { Layout } = DefaultTheme
</script>

<template>
  <Layout>
    <template #aside-outline-after>
      <Sponsors />
    </template>
  </Layout>
</template>
