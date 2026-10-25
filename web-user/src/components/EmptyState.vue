<template>
  <div class="empty-state">
    <div class="icon"><AppIcon :name="icon" :size="30" /></div>
    <p class="title">{{ title }}</p>
    <p v-if="description" class="desc">{{ description }}</p>
    <el-button v-if="actionText" size="small" @click="$emit('action')">{{ actionText }}</el-button>
  </div>
</template>

<script setup>
import AppIcon from './AppIcon.vue'

defineProps({
  icon: { type: String, default: 'film' },
  title: { type: String, default: '暂无内容' },
  description: { type: String, default: '' },
  actionText: { type: String, default: '' }
})
defineEmits(['action'])
</script>

<style scoped>
.empty-state {
  padding: 64px 20px;
  text-align: center;
}
.icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 14px;
  border-radius: 50%;
  background: #f4f5f7;
  color: #b6bac0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.title {
  font-size: 14px;
  color: var(--text-2);
}
.desc {
  margin: 4px 0 14px;
  font-size: 12px;
  color: var(--text-3);
}
</style>
