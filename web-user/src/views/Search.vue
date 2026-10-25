<template>
  <div class="page-container search-page">
    <div class="search-panel">
      <el-input v-model="keyword" size="large" placeholder="搜索视频、创作者或话题" clearable @keyup.enter="doSearch">
        <template #prefix>
          <AppIcon name="search" :size="18" />
        </template>
        <template #append>
          <el-button type="primary" @click="doSearch">搜索</el-button>
        </template>
      </el-input>

      <div class="hot-tags" v-if="!searched && hotTags.length">
        <span class="hot-label">热门搜索</span>
        <button v-for="t in hotTags" :key="t.id" class="hot-tag" @click="quickSearch(t.name)">
          {{ t.name }}
        </button>
      </div>
    </div>

    <div class="result-head" v-if="searched">
      <h2>搜索结果</h2>
      <span class="result-count" v-if="!loading">找到 {{ total }} 个相关视频</span>
    </div>

    <div v-if="loading" class="video-grid">
      <div v-for="i in 8" :key="i" class="skeleton-card">
        <div class="skeleton skeleton-cover"></div>
        <div class="skeleton skeleton-line"></div>
        <div class="skeleton skeleton-line short"></div>
      </div>
    </div>

    <div v-else-if="videos.length" class="video-grid">
      <VideoCard v-for="v in videos" :key="v.id" :video="v" />
    </div>

    <EmptyState v-else-if="searched" icon="search" title="没有找到相关内容" description="换个关键词试试，或浏览推荐内容" action-text="返回首页"
      @action="$router.push('/')" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { apiSearch, apiTags } from '../api'
import VideoCard from '../components/VideoCard.vue'
import EmptyState from '../components/EmptyState.vue'
import AppIcon from '../components/AppIcon.vue'

const route = useRoute()
const router = useRouter()
const keyword = ref(route.query.keyword || '')
const videos = ref([])
const total = ref(0)
const searched = ref(false)
const loading = ref(false)
const hotTags = ref([])

async function doSearch() {
  const value = keyword.value.trim()
  if (!value) return
  searched.value = true
  loading.value = true
  try {
    const res = await apiSearch(value, 1)
    videos.value = res.list
    total.value = res.total
  } catch (e) {
    videos.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}
function quickSearch(name) {
  keyword.value = name
  doSearch()
}

onMounted(async () => {
  try {
    const tags = await apiTags()
    hotTags.value = tags.sort((a, b) => b.hot - a.hot).slice(0, 8)
  } catch (e) {
    hotTags.value = []
  }
  if (route.query.keyword) doSearch()
})
</script>

<style scoped>
.search-page {
  padding-top: 28px;
}

.search-panel {
  max-width: 720px;
  margin: 0 auto 24px;
}

.search-panel :deep(.el-input__wrapper) {
  height: 44px;
}

.search-panel :deep(.el-input__inner) {
  height: 44px;
  font-size: 15px;
}

.search-panel :deep(.el-input-group__append) {
  padding: 0;
  border-radius: 0 6px 6px 0;
  overflow: hidden;
}

.search-panel :deep(.el-input-group__append .el-button) {
  height: 44px;
  border-radius: 0;
  margin: 0;
  padding: 0 22px;
}

.hot-tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 14px;
}

.hot-label {
  font-size: 12px;
  color: var(--text-3);
}

.hot-tag {
  border: 1px solid var(--border-light);
  background: #fff;
  color: var(--text-2);
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 4px;
  cursor: pointer;
}

.hot-tag:hover {
  color: var(--brand);
  border-color: var(--brand);
}

.result-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 16px;
}

.result-head h2 {
  font-size: 17px;
  font-weight: 600;
}

.result-count {
  font-size: 12px;
  color: var(--text-3);
}

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
