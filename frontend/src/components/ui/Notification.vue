<script setup>
import { ref } from 'vue'

const notifications = ref([])


</script>
<template>
  <div>
    <ul>
      <li v-for="notification in notifications" :key="notification.id">
        {{ notification.message }}
      </li>
    </ul>
  </div>
</template>
<!-- <style scoped>
ul {
 src: ds; 
}
</style> -->
