<template>
  <div class="page-container">
    <div class="page-heading">
      <h1>{{ cate.name || '频道' }}</h1>
      <span class="sub" v-if="!loading">共 {{ total }} 个作品</span>
    </div>

    <div v-if="loading && !videos.length" class="video-grid">
      <div v-for="i in 8" :key="i" class="skeleton-card">
        <div class="skeleton skeleton-cover"></div>
        <div class="skeleton skeleton-line"></div>
        <div class="skeleton skeleton-line short"></div>
      </div>
    </div>

    <div v-else-if="videos.length" class="video-grid">
      <VideoCard v-for="v in videos" :key="v.id" :video="v" />
    </div>

    <EmptyState v-else icon="video" title="该频道暂无内容" description="去其他频道看看，或发布你的第一条作品" action-text="去发布"
      @action="$router.push('/publish')" />

    <div class="load-more" v-if="hasMore && videos.length">
      <el-button text :loading="loading" @click="loadMore">加载更多</el-button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { apiCategoryVideos, apiCategories } from '../api'
import VideoCard from '../components/VideoCard.vue'
import EmptyState from '../components/EmptyState.vue'

const route = useRoute()
const videos = ref([])
const cate = ref({})
const pageNum = ref(1)
const total = ref(0)
const hasMore = ref(true)
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await apiCategoryVideos(route.params.id, pageNum.value)
    videos.value.push(...res.list)
    total.value = res.total
    hasMore.value = videos.value.length < res.total
    pageNum.value++
  } finally {
    loading.value = false
  }
}
function loadMore() {
  load()
}

onMounted(async () => {
  const list = await apiCategories()
  cate.value = list.find(c => c.id === Number(route.params.id)) || {}
  load()
})
</script>

<style scoped>
.video-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 22px 16px;
}

.skeleton-card {
  min-width: 0;
}

.skeleton-cover {
  width: 100%;
  aspect-ratio: 3 / 4;
  border-radius: 8px;
}

.skeleton-line {
  height: 14px;
  margin-top: 10px;
  width: 90%;
}

.skeleton-line.short {
  width: 55%;
  margin-top: 8px;
}

.load-more {
  padding: 28px 0 8px;
  text-align: center;
}

@media (max-width: 900px) {
  .video-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .video-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 18px 12px;
  }
}
</style>
