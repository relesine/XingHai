<template>
  <div class="page-container">
    <div class="page-heading live-heading">
      <div>
        <h1>星海直播</h1>
        <p class="muted">正在直播的内容会出现在最前面</p>
      </div>
      <span class="notice">当前为演示直播间，真实推流能力见扩展方案</span>
    </div>

    <div v-if="rooms.length" class="room-grid" v-loading="loading">
      <article
        v-for="room in sortedRooms"
        :key="room.id"
        class="room-card"
        :class="{ live: room.status === 1 }"
      >
        <div class="room-cover">
          <img v-if="room.coverUrl" :src="room.coverUrl" alt="" />
          <div v-else class="cover-fallback">
            <AppIcon name="live" :size="34" />
          </div>
          <span v-if="room.status === 1" class="badge-live">
            <i class="dot"></i>直播中
          </span>
          <span class="badge-viewers" v-if="room.status === 1">
            <AppIcon name="eye" :size="13" />{{ formatNum(room.viewerCount) }}
          </span>
        </div>
        <div class="room-body">
          <h3 class="room-title">{{ room.title }}</h3>
          <div class="room-foot">
            <img :src="room.avatar || '/upload/avatar/u1.svg'" class="r-avatar" alt="" />
            <span class="r-name">{{ room.nickname }}</span>
            <span class="r-likes" v-if="room.status === 1">
              <AppIcon name="heart" :size="13" />{{ formatNum(room.likeCount) }}
            </span>
          </div>
        </div>
      </article>
    </div>

    <EmptyState
      v-else-if="!loading"
      title="暂无直播间"
      description="主播开播后会在这里展示，先去首页看看视频吧"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { apiLiveList } from '../api'
import AppIcon from '../components/AppIcon.vue'
import EmptyState from '../components/EmptyState.vue'

const rooms = ref([])
const loading = ref(false)

const sortedRooms = computed(() =>
  [...rooms.value].sort((a, b) => (b.status === 1 ? 1 : 0) - (a.status === 1 ? 1 : 0))
)

function formatNum(n) {
  const v = Number(n) || 0
  if (v >= 100000000) return (v / 100000000).toFixed(1) + '亿'
  if (v >= 10000) return (v / 10000).toFixed(1) + '万'
  return String(v)
}

onMounted(async () => {
  loading.value = true
  try {
    rooms.value = (await apiLiveList()) || []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.live-heading { display: flex; align-items: flex-end; justify-content: space-between; gap: 16px; }
.notice { font-size: 12px; color: var(--text-3); }

.room-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  margin-top: 4px;
}

.room-card {
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: border-color .15s;
}
.room-card:hover { border-color: var(--border); }

.room-cover {
  position: relative;
  aspect-ratio: 16 / 10;
  background: #f4f5f7;
}
.room-cover img { width: 100%; height: 100%; object-fit: cover; display: block; }
.cover-fallback {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c3c7cc;
}

.badge-live {
  position: absolute;
  top: 10px;
  left: 10px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  height: 22px;
  padding: 0 8px;
  border-radius: 4px;
  background: rgba(245, 63, 63, .92);
  color: #fff;
  font-size: 12px;
}
.badge-live .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #fff;
  animation: pulse 1.4s ease-in-out infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: .35; }
}
.badge-viewers {
  position: absolute;
  top: 10px;
  right: 10px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 22px;
  padding: 0 8px;
  border-radius: 4px;
  background: rgba(0, 0, 0, .55);
  color: #fff;
  font-size: 12px;
}

.room-body { padding: 12px 14px 14px; }
.room-title {
  margin: 0;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-1);
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.room-foot { display: flex; align-items: center; gap: 7px; margin-top: 10px; }
.r-avatar { width: 24px; height: 24px; border-radius: 50%; object-fit: cover; }
.r-name { font-size: 12px; color: var(--text-2); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.r-likes {
  margin-left: auto;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  color: var(--text-3);
  flex-shrink: 0;
}

@media (max-width: 640px) {
  .live-heading { flex-direction: column; align-items: flex-start; }
}
</style>
