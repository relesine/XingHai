<template>
  <div class="page-container play-page">
    <div class="breadcrumb">
      <button class="crumb-link" @click="$router.back()">
        <AppIcon name="arrow-left" :size="16" />
        返回
      </button>
      <span class="crumb-sep">/</span>
      <router-link to="/" class="crumb-link">首页</router-link>
      <span class="crumb-sep">/</span>
      <span class="crumb-current ellipsis">{{ video.title || '视频详情' }}</span>
    </div>

    <div class="play-layout">
      <div class="main-col">
        <div class="player-wrap">
          <video :key="video.id" class="player" :src="video.videoUrl" :poster="video.coverUrl" controls autoplay
            loop></video>
        </div>

        <h1 class="video-title">{{ video.title }}</h1>
        <div class="meta-row">
          <span class="meta-item">
            <AppIcon name="play" :size="14" />
            {{ formatNum(video.viewCount) }} 次播放
          </span>
          <span class="meta-item">{{ fmt(video.createTime) }}</span>
          <span class="meta-item" v-if="video.geoLocation">
            <AppIcon name="location" :size="14" />
            {{ video.geoLocation }}
          </span>
          <div class="actions">
            <button class="action" :class="{ active: video.liked }" @click="toggleLike">
              <AppIcon name="heart" :size="18" />
              <span>{{ formatNum(video.likeCount) }}</span>
            </button>
            <button class="action" :class="{ active: video.collected }" @click="toggleCollect">
              <AppIcon name="star" :size="18" />
              <span>{{ formatNum(video.collectCount) }}</span>
            </button>
            <button class="action" @click="scrollToComments">
              <AppIcon name="comment" :size="18" />
              <span>{{ formatNum(video.commentCount) }}</span>
            </button>
            <button class="action" @click="share">
              <AppIcon name="share" :size="18" />
              <span>分享</span>
            </button>
            <button class="action report" @click="openReport">
              <AppIcon name="flag" :size="16" />
              <span>举报</span>
            </button>
          </div>
        </div>

        <div class="author-bar">
          <img :src="video.authorAvatar || '/upload/avatar/u1.svg'" class="author-avatar"
            @click="$router.push(`/user/${video.userId}`)" />
          <div class="author-info">
            <div class="author-name" @click="$router.push(`/user/${video.userId}`)">{{ video.authorName }}</div>
            <div class="author-sub">创作者 · 发布于星海</div>
          </div>
          <el-button v-if="userStore.isLogin && video.userId !== userStore.user.userId"
            :type="video.followed ? 'default' : 'primary'" size="small" @click="toggleFollow">{{ video.followed ? '已关注'
            : '关注' }}</el-button>
          <el-button v-else-if="!userStore.isLogin" size="small" @click="$router.push('/login')">登录后关注</el-button>
          <el-button v-if="userStore.isLogin && video.userId !== userStore.user.userId" size="small" text
            @click="$router.push(`/messages?to=${video.userId}`)">发私信</el-button>
        </div>

        <div class="desc-card" v-if="video.description || (video.tags && video.tags.length)">
          <p v-if="video.description" class="desc-text">{{ video.description }}</p>
          <div class="tag-row" v-if="video.tags && video.tags.length">
            <router-link v-for="t in video.tags" :key="t" class="tag"
              :to="{ path: '/search', query: { keyword: t } }">#{{ t }}</router-link>
          </div>
        </div>

        <div class="switch-row">
          <el-button size="small" :disabled="!prevVideo" @click="switchVideo(prevVideo)">
            <AppIcon name="arrow-up" :size="14" />
            <span class="switch-text">上一条</span>
          </el-button>
          <el-button size="small" :disabled="!nextVideo" @click="switchVideo(nextVideo)">
            <AppIcon name="arrow-up" :size="14" class="rotate" />
            <span class="switch-text">下一条</span>
          </el-button>
        </div>

        <section class="comment-card" id="comments">
          <div class="comment-head">
            <h2>评论</h2>
            <span class="comment-count">{{ video.commentCount || 0 }}</span>
          </div>

          <div class="comment-input">
            <img :src="myAvatar" class="c-avatar" />
            <div class="input-body">
              <el-input v-model="commentText" type="textarea" :rows="2" maxlength="300" show-word-limit resize="none"
                :placeholder="video.allowComment === 0 ? '作者已关闭评论' : '友善评论，理性发言'"
                :disabled="video.allowComment === 0" />
              <div class="input-actions">
                <el-button type="primary" size="small" :disabled="!commentText.trim() || video.allowComment === 0"
                  @click="submitComment">发表评论</el-button>
              </div>
            </div>
          </div>

          <div class="comment-list">
            <div v-for="c in comments" :key="c.id" class="comment-item" :class="{ reply: c.parentId }">
              <img :src="c.avatar || '/upload/avatar/u1.svg'" class="c-avatar" />
              <div class="c-body">
                <div class="c-name">{{ c.nickname }}</div>
                <div class="c-content">{{ c.content }}</div>
                <div class="c-meta">
                  <span>{{ fmt(c.createTime) }}</span>
                  <button class="c-like" :class="{ active: c.liked }" @click="likeComment(c)">
                    <AppIcon name="heart" :size="13" />
                    {{ c.likeCount || 0 }}
                  </button>
                  <button v-if="canDel(c)" class="c-del" @click="delComment(c.id)">删除</button>
                </div>
              </div>
            </div>
            <EmptyState v-if="!comments.length" icon="comment" title="还没有评论" description="来说说你的看法吧" />
          </div>
        </section>
      </div>

      <aside class="related-col">
        <div class="related-head">
          <h2>相关推荐</h2>
        </div>
        <div v-if="relatedVideos.length" class="related-list">
          <div v-for="rv in relatedVideos" :key="rv.id" class="related-item" @click="$router.push(`/video/${rv.id}`)">
            <div class="related-cover-wrap">
              <img :src="rv.coverUrl" class="related-cover" loading="lazy" />
              <span class="related-duration">{{ durationText(rv.duration) }}</span>
            </div>
            <div class="related-info">
              <p class="related-title clamp-2">{{ rv.title }}</p>
              <span class="related-meta">{{ rv.authorName }}</span>
              <span class="related-meta">{{ formatNum(rv.viewCount) }} 次播放</span>
            </div>
          </div>
        </div>
        <p v-else class="related-empty">暂无相关推荐</p>
      </aside>
    </div>

    <el-dialog v-model="reportVisible" title="举报内容" width="420">
      <el-form label-width="80px">
        <el-form-item label="举报原因">
          <el-select v-model="reportReason" style="width: 100%">
            <el-option v-for="r in reasons" :key="r" :label="r" :value="r" />
          </el-select>
        </el-form-item>
        <el-form-item label="补充说明">
          <el-input v-model="reportDetail" type="textarea" :rows="3" maxlength="200" show-word-limit
            placeholder="请补充具体情况（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!reportReason" @click="submitReport">提交举报</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import {
  apiVideoDetail, apiFeedRecommend, apiVideoRelated, apiLike, apiUnlike,
  apiCollect, apiUncollect, apiFollow, apiUnfollow,
  apiComments, apiAddComment, apiDeleteComment, apiReport
} from '../api'
import AppIcon from '../components/AppIcon.vue'
import EmptyState from '../components/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const video = ref({})
const comments = ref([])
const relatedVideos = ref([])
const commentText = ref('')
const feed = ref([])
const reportVisible = ref(false)
const reportReason = ref('低俗色情')
const reportDetail = ref('')
const reasons = ['低俗色情', '虚假宣传', '侵权盗版', '人身攻击', '违法违规', '其他']

const prevVideo = ref(null)
const nextVideo = ref(null)
const myAvatar = computed(() => userStore.user?.avatar || '/upload/avatar/u1.svg')

const fmt = t => (t || '').replace('T', ' ').slice(0, 16)
const formatNum = n => {
  if (!n) return 0
  if (n >= 100000000) return (n / 100000000).toFixed(1) + '亿'
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  return n
}
const durationText = s => {
  if (!s) return ''
  const m = Math.floor(s / 60)
  const sec = s % 60
  return `${String(m).padStart(2, '0')}:${String(sec).padStart(2, '0')}`
}

async function loadVideo(id) {
  const v = await apiVideoDetail(id)
  video.value = v
  const [commentRes, relatedRes] = await Promise.allSettled([
    apiComments(id),
    apiVideoRelated(id, 8)
  ])
  comments.value = commentRes.status === 'fulfilled' ? commentRes.value.list : []
  relatedVideos.value = relatedRes.status === 'fulfilled' ? relatedRes.value : []
  const idx = feed.value.findIndex(f => f.id === Number(id))
  if (idx >= 0) {
    prevVideo.value = feed.value[idx - 1] || null
    nextVideo.value = feed.value[idx + 1] || null
  }
}

function switchVideo(v) {
  if (v) router.push(`/video/${v.id}`)
}

async function toggleLike() {
  if (!userStore.isLogin) return router.push('/login')
  try {
    if (video.value.liked) {
      await apiUnlike('video', video.value.id)
      video.value.liked = false
      video.value.likeCount--
    } else {
      await apiLike('video', video.value.id)
      video.value.liked = true
      video.value.likeCount++
    }
  } catch (e) { /* 拦截器统一提示 */ }
}

async function toggleCollect() {
  if (!userStore.isLogin) return router.push('/login')
  try {
    if (video.value.collected) {
      await apiUncollect(video.value.id)
      video.value.collected = false
      video.value.collectCount--
    } else {
      await apiCollect(video.value.id)
      video.value.collected = true
      video.value.collectCount++
    }
  } catch (e) { /* 拦截器统一提示 */ }
}

async function toggleFollow() {
  if (!userStore.isLogin) return router.push('/login')
  try {
    if (video.value.followed) {
      await apiUnfollow(video.value.userId)
      video.value.followed = false
    } else {
      await apiFollow(video.value.userId)
      video.value.followed = true
    }
  } catch (e) { /* 拦截器统一提示 */ }
}

async function submitComment() {
  if (!commentText.value.trim()) return
  try {
    await apiAddComment(video.value.id, { content: commentText.value.trim(), parentId: 0 })
    commentText.value = ''
    ElMessage.success('评论已发布')
    comments.value = (await apiComments(video.value.id)).list
    video.value.commentCount++
  } catch (e) { /* 拦截器统一提示 */ }
}

function likeComment(c) {
  if (!userStore.isLogin) return router.push('/login')
  const act = c.liked ? apiUnlike : apiLike
  act('comment', c.id).then(() => {
    c.liked = !c.liked
    c.likeCount += c.liked ? 1 : -1
  }).catch(() => { })
}

function canDel(c) {
  return (userStore.isLogin && c.userId === userStore.user.userId) || userStore.roles.includes('ADMIN')
}

async function delComment(id) {
  await apiDeleteComment(id)
  comments.value = (await apiComments(video.value.id)).list
  if (video.value.commentCount > 0) video.value.commentCount--
}

function share() {
  navigator.clipboard?.writeText(location.href)
  ElMessage.success('链接已复制')
}

function scrollToComments() {
  document.getElementById('comments')?.scrollIntoView({ behavior: 'smooth' })
}

function openReport() {
  if (!userStore.isLogin) return router.push('/login')
  reportVisible.value = true
}

async function submitReport() {
  await apiReport({
    targetType: 'video',
    targetId: video.value.id,
    reason: reportReason.value,
    detail: reportDetail.value
  })
  reportVisible.value = false
  ElMessage.success('举报已提交，平台将尽快处理')
}

onMounted(async () => {
  try {
    feed.value = (await apiFeedRecommend(1, 20)).list
  } catch (e) {
    feed.value = []
  }
  await loadVideo(route.params.id)
})

watch(() => route.params.id, newId => {
  if (newId) {
    loadVideo(newId)
    window.scrollTo({ top: 0 })
  }
})
</script>

<style scoped>
.play-page {
  padding-top: 14px;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--text-3);
  margin-bottom: 12px;
}

.crumb-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: none;
  background: none;
  color: var(--text-2);
  cursor: pointer;
  font-size: 12px;
}

.crumb-link:hover {
  color: var(--brand);
}

.crumb-sep {
  color: #c9ccd0;
}

.crumb-current {
  max-width: 420px;
  color: var(--text-3);
}

.play-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 24px;
  align-items: start;
}

.main-col {
  min-width: 0;
}

.player-wrap {
  background: #000;
  border-radius: 8px;
  overflow: hidden;
}

.player {
  display: block;
  width: 100%;
  aspect-ratio: 16 / 9;
  background: #000;
}

.video-title {
  font-size: 20px;
  font-weight: 600;
  line-height: 28px;
  margin: 14px 0 8px;
}

.meta-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 14px;
  font-size: 12px;
  color: var(--text-3);
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-light);
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.actions {
  display: flex;
  align-items: center;
  gap: 2px;
  margin-left: auto;
}

.action {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  height: 32px;
  padding: 0 10px;
  border: none;
  background: transparent;
  border-radius: 6px;
  color: var(--text-2);
  font-size: 13px;
  cursor: pointer;
}

.action:hover {
  background: #f4f5f7;
  color: var(--text-1);
}

.action.active {
  color: var(--brand);
}

.action.report {
  margin-left: 6px;
  color: var(--text-3);
}

.author-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 0;
}

.author-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  object-fit: cover;
  cursor: pointer;
  flex-shrink: 0;
}

.author-info {
  min-width: 0;
}

.author-name {
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.author-name:hover {
  color: var(--brand);
}

.author-sub {
  font-size: 12px;
  color: var(--text-3);
}

.desc-card {
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: 8px;
  padding: 14px 16px;
}

.desc-text {
  font-size: 14px;
  line-height: 22px;
  color: var(--text-2);
  white-space: pre-wrap;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.tag {
  font-size: 12px;
  color: var(--brand);
  background: var(--brand-light);
  border-radius: 4px;
  padding: 2px 8px;
}

.tag:hover {
  background: #ffe3ec;
}

.switch-row {
  display: flex;
  gap: 8px;
  padding: 14px 0 4px;
}

.switch-row :deep(.el-button) {
  color: var(--text-2);
}

.switch-text {
  margin-left: 4px;
}

.rotate {
  transform: rotate(180deg);
}

.comment-card {
  margin-top: 16px;
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: 8px;
  padding: 18px 20px;
}

.comment-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}

.comment-head h2 {
  font-size: 16px;
  font-weight: 600;
}

.comment-count {
  font-size: 13px;
  color: var(--text-3);
}

.comment-input {
  display: flex;
  gap: 12px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--border-light);
}

.c-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.input-body {
  flex: 1;
  min-width: 0;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.comment-list {
  padding-top: 6px;
}

.comment-item {
  display: flex;
  gap: 10px;
  padding: 14px 0;
  border-bottom: 1px solid var(--border-light);
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-item.reply {
  margin-left: 44px;
  padding: 10px 0;
}

.c-body {
  flex: 1;
  min-width: 0;
}

.c-name {
  font-size: 13px;
  color: var(--text-2);
  font-weight: 500;
}

.c-content {
  font-size: 14px;
  color: var(--text-1);
  margin: 4px 0 6px;
  line-height: 22px;
  word-break: break-word;
}

.c-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: var(--text-3);
}

.c-like,
.c-del {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  border: none;
  background: none;
  padding: 0;
  color: var(--text-3);
  cursor: pointer;
  font-size: 12px;
}

.c-like:hover {
  color: var(--text-1);
}

.c-like.active {
  color: var(--brand);
}

.c-del:hover {
  color: #f56c6c;
}

.related-col {
  position: sticky;
  top: 78px;
}

.related-head h2 {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
}

.related-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.related-item {
  display: flex;
  gap: 10px;
  cursor: pointer;
}

.related-cover-wrap {
  position: relative;
  width: 108px;
  height: 144px;
  border-radius: 6px;
  overflow: hidden;
  background: #eef0f2;
  flex-shrink: 0;
}

.related-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.related-duration {
  position: absolute;
  right: 4px;
  bottom: 4px;
  padding: 0 4px;
  font-size: 10px;
  line-height: 15px;
  color: #fff;
  background: rgba(0, 0, 0, 0.55);
  border-radius: 3px;
}

.related-info {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.related-title {
  font-size: 13px;
  font-weight: 500;
  line-height: 19px;
  color: var(--text-1);
}

.related-item:hover .related-title {
  color: var(--brand);
}

.related-meta {
  font-size: 12px;
  color: var(--text-3);
}

.related-empty {
  font-size: 13px;
  color: var(--text-3);
}

@media (max-width: 1000px) {
  .play-layout {
    grid-template-columns: 1fr;
  }

  .related-col {
    position: static;
    border-top: 1px solid var(--border-light);
    padding-top: 16px;
  }

  .related-list {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 14px;
  }
}

@media (max-width: 640px) {
  .video-title {
    font-size: 17px;
    line-height: 24px;
  }

  .actions {
    width: 100%;
    margin-left: 0;
    overflow-x: auto;
  }

  .related-list {
    grid-template-columns: 1fr;
  }

  .comment-card {
    padding: 14px;
  }

  .comment-item.reply {
    margin-left: 20px;
  }
}
</style>
