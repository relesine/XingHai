<template>
  <div class="home-page">
    <div class="channel-bar">
      <div class="channel-inner">
        <button class="channel-item" :class="{ active: isAll }" @click="$router.push('/')">全部</button>
        <button
          v-for="c in categories"
          :key="c.id"
          class="channel-item"
          @click="$router.push(`/category/${c.id}`)"
        >{{ c.name }}</button>
        <span class="channel-divider"></span>
        <button class="channel-item" @click="$router.push('/live')">直播</button>
        <button class="channel-item" @click="$router.push('/mall')">商城</button>
      </div>
    </div>

    <div class="page-container home-body">
      <section class="feed-main">
        <div class="feed-tabs">
          <button
            v-for="t in tabs"
            :key="t.key"
            class="feed-tab"
            :class="{ active: tab === t.key }"
            @click="switchTab(t.key)"
          >{{ t.label }}</button>
          <span class="feed-count" v-if="!loading">共 {{ total }} 条内容</span>
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

        <EmptyState
          v-else
          icon="video"
          :title="tab === 'follow' ? '还没有关注动态' : '这里暂时没有内容'"
          :description="tab === 'follow' ? '关注喜欢的创作者，他们的新作品会出现在这里' : '换个频道看看，或者发布你的第一条作品'"
          action-text="去发布"
          @action="$router.push('/publish')"
        />

        <div v-if="hasMore && videos.length && !loading" class="load-more">
          <el-button text @click="load(true)">加载更多</el-button>
        </div>
      </section>

      <aside class="side-rail">
        <div class="rail-card" v-if="notices.length">
          <div class="rail-title">
            <AppIcon name="bell" :size="16" />
            <span>平台公告</span>
          </div>
          <div v-for="n in notices.slice(0, 3)" :key="n.id" class="notice-item">
            <span class="notice-dot"></span>
            <span class="notice-text clamp-2">{{ n.title }}</span>
          </div>
        </div>

        <div class="rail-card">
          <div class="rail-title">
            <AppIcon name="fire" :size="16" />
            <span>热门视频</span>
          </div>
          <div v-if="hotVideos.length" class="hot-list">
            <div v-for="(v, i) in hotVideos" :key="v.id" class="hot-item" @click="$router.push(`/video/${v.id}`)">
              <span class="rank" :class="{ top: i < 3 }">{{ i + 1 }}</span>
              <span class="hot-title ellipsis">{{ v.title }}</span>
              <span class="hot-count">{{ formatNum(v.viewCount) }}</span>
            </div>
          </div>
          <p v-else class="rail-empty">暂无热门内容</p>
        </div>

        <div class="rail-card" v-if="ads.length">
          <div class="rail-title">
            <AppIcon name="megaphone" :size="16" />
            <span>平台活动</span>
          </div>
          <a
            v-for="ad in ads.slice(0, 2)"
            :key="ad.id"
            class="ad-item"
            :href="ad.linkUrl || 'javascript:void(0)'"
            target="_blank"
            rel="noopener noreferrer"
          >
            <img :src="ad.imageUrl" :alt="ad.title" class="ad-cover" />
            <div class="ad-info">
              <div class="ad-title clamp-2">{{ ad.title }}</div>
              <span class="ad-link">查看详情</span>
            </div>
          </a>
        </div>

        <div class="rail-foot">
          <p>星海社区 · 记录每一片星海</p>
          <p class="muted">内容由创作者发布，平台审核后展示</p>
        </div>
      </aside>

      <transition name="fade">
        <button v-if="showTop" class="back-top" title="回到顶部" @click="scrollTop">
          <AppIcon name="arrow-up" :size="18" />
        </button>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import {
  apiFeedRecommend, apiFeedFollow, apiFeedNearby,
  apiCategories, apiNotices, apiAdverts
} from '../api'
import VideoCard from '../components/VideoCard.vue'
import EmptyState from '../components/EmptyState.vue'
import AppIcon from '../components/AppIcon.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const videos = ref([])
const hotVideos = ref([])
const categories = ref([])
const notices = ref([])
const ads = ref([])
const tab = ref('recommend')
const pageNum = ref(1)
const total = ref(0)
const loading = ref(false)
const hasMore = ref(true)
const showTop = ref(false)

const tabs = [
  { key: 'recommend', label: '推荐' },
  { key: 'follow', label: '关注' },
  { key: 'nearby', label: '同城' }
]

const isAll = computed(() => route.path === '/')

function formatNum(n) {
  if (!n) return 0
  if (n >= 100000000) return (n / 100000000).toFixed(1) + '亿'
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  return n
}

function onScroll() {
  showTop.value = window.scrollY > 600
}
function scrollTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

async function load(append = false) {
  if (loading.value) return
  loading.value = true
  try {
    const page = append ? pageNum.value : 1
    let res
    if (tab.value === 'follow') res = await apiFeedFollow(page, 12)
    else if (tab.value === 'nearby') res = await apiFeedNearby(undefined, page, 12)
    else res = await apiFeedRecommend(page, 12)

    if (append) videos.value.push(...res.list)
    else videos.value = res.list

    total.value = res.total
    hasMore.value = videos.value.length < res.total
    pageNum.value = page + 1

    if (!append) {
      hotVideos.value = [...res.list].sort((a, b) => (b.viewCount || 0) - (a.viewCount || 0)).slice(0, 6)
    }
  } catch (e) {
    if (!append) videos.value = []
  } finally {
    loading.value = false
  }
}

function switchTab(t) {
  if (t !== 'recommend' && !userStore.isLogin) {
    router.push({ path: '/login', query: { redirect: '/' } })
    return
  }
  tab.value = t
  videos.value = []
  hotVideos.value = []
  pageNum.value = 1
  hasMore.value = true
  load()
}

onMounted(async () => {
  load()
  const [cateRes, noticeRes, adRes] = await Promise.allSettled([
    apiCategories(),
    apiNotices(),
    apiAdverts('home')
  ])
  if (cateRes.status === 'fulfilled') categories.value = cateRes.value || []
  if (noticeRes.status === 'fulfilled') notices.value = noticeRes.value || []
  if (adRes.status === 'fulfilled') ads.value = adRes.value || []
  window.addEventListener('scroll', onScroll, { passive: true })
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', onScroll)
})
</script>

<style scoped>
.channel-bar {
  background: #fff;
  border-bottom: 1px solid var(--border-light);
}
.channel-inner {
  max-width: 1240px;
  margin: 0 auto;
  padding: 0 24px;
  height: 46px;
  display: flex;
  align-items: center;
  gap: 4px;
  overflow-x: auto;
  scrollbar-width: none;
}
.channel-inner::-webkit-scrollbar {
  display: none;
}
.channel-item {
  flex-shrink: 0;
  height: 30px;
  padding: 0 12px;
  border: none;
  background: transparent;
  color: var(--text-2);
  font-size: 13px;
  border-radius: 6px;
  cursor: pointer;
}
.channel-item:hover {
  background: #f4f5f7;
  color: var(--text-1);
}
.channel-item.active {
  background: var(--brand-light);
  color: var(--brand);
  font-weight: 500;
}
.channel-divider {
  width: 1px;
  height: 14px;
  background: var(--border);
  margin: 0 8px;
  flex-shrink: 0;
}

.home-body {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 288px;
  gap: 24px;
  padding-top: 16px;
}
.feed-tabs {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 16px;
}
.feed-tab {
  height: 32px;
  padding: 0 14px;
  border: none;
  background: transparent;
  font-size: 15px;
  color: var(--text-2);
  border-radius: 6px;
  cursor: pointer;
}
.feed-tab:hover {
  color: var(--text-1);
  background: #f1f2f3;
}
.feed-tab.active {
  color: var(--text-1);
  font-weight: 600;
  background: #fff;
  border: 1px solid var(--border-light);
}
.feed-count {
  margin-left: auto;
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
.load-more {
  padding: 28px 0 8px;
  text-align: center;
}
.load-more :deep(.el-button) {
  color: var(--text-2);
  font-size: 13px;
}

.side-rail {
  display: flex;
  flex-direction: column;
  gap: 14px;
  align-content: start;
}
.rail-card {
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: 8px;
  padding: 16px;
}
.rail-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-1);
  margin-bottom: 12px;
}
.rail-title svg {
  color: var(--brand);
}
.notice-item {
  display: flex;
  gap: 8px;
  padding: 6px 0;
  font-size: 13px;
  color: var(--text-2);
}
.notice-dot {
  width: 4px;
  height: 4px;
  margin-top: 9px;
  border-radius: 50%;
  background: var(--border);
  flex-shrink: 0;
}
.notice-text {
  line-height: 20px;
}
.hot-list {
  display: flex;
  flex-direction: column;
}
.hot-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 0;
  cursor: pointer;
  font-size: 13px;
}
.hot-item:hover .hot-title {
  color: var(--brand);
}
.rank {
  width: 16px;
  font-size: 12px;
  color: var(--text-3);
  text-align: center;
  flex-shrink: 0;
}
.rank.top {
  color: var(--brand);
  font-weight: 600;
}
.hot-title {
  color: var(--text-1);
  flex: 1;
  min-width: 0;
}
.hot-count {
  font-size: 11px;
  color: var(--text-3);
  flex-shrink: 0;
}
.rail-empty {
  font-size: 13px;
  color: var(--text-3);
  padding: 6px 0;
}
.ad-item {
  display: flex;
  gap: 10px;
  padding: 6px 0;
}
.ad-cover {
  width: 64px;
  height: 64px;
  object-fit: cover;
  border-radius: 6px;
  flex-shrink: 0;
}
.ad-info {
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.ad-title {
  font-size: 13px;
  line-height: 18px;
  color: var(--text-1);
}
.ad-item:hover .ad-title {
  color: var(--brand);
}
.ad-link {
  font-size: 12px;
  color: var(--text-3);
}
.rail-foot {
  padding: 4px 4px 0;
  font-size: 11px;
  line-height: 18px;
  color: var(--text-3);
}

.back-top {
  position: fixed;
  right: 32px;
  bottom: 48px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1px solid var(--border);
  background: #fff;
  color: var(--text-2);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  z-index: 90;
}
.back-top:hover {
  color: var(--brand);
  border-color: var(--brand);
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.18s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 1080px) {
  .home-body {
    grid-template-columns: 1fr;
  }
  .side-rail {
    display: none;
  }
  .video-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}
@media (max-width: 820px) {
  .video-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
@media (max-width: 600px) {
  .video-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 18px 12px;
  }
}
</style>
