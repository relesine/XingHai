<template>
  <div class="page-container" v-if="user">
    <div class="profile-head surface-card">
      <img :src="user.avatar || '/upload/avatar/u1.svg'" class="big-avatar" />
      <div class="profile-info">
        <div class="name-row">
          <h1>{{ user.nickname }}</h1>
          <el-tag v-if="user.creator" size="small" type="warning">认证创作者</el-tag>
        </div>
        <p class="signature">{{ user.signature || '这个人还没有填写简介' }}</p>
        <div class="stats">
          <span><b>{{ fmtNum(user.videoCount) }}</b>作品</span>
          <span><b>{{ fmtNum(user.fansCount) }}</b>粉丝</span>
          <span><b>{{ fmtNum(user.followCount) }}</b>关注</span>
          <span class="location" v-if="user.location">
            <AppIcon name="location" :size="13" />
            {{ user.location }}
          </span>
        </div>
      </div>
      <div class="profile-actions">
        <template v-if="userStore.isLogin && user.id !== userStore.user.userId">
          <el-button :type="followed ? 'default' : 'primary'" @click="toggleFollow">
            {{ followed ? '已关注' : '关注' }}
          </el-button>
          <el-button @click="$router.push(`/messages?to=${user.id}`)">发私信</el-button>
        </template>
        <el-button v-if="!userStore.isLogin" type="primary" @click="$router.push('/login')">登录后互动</el-button>
      </div>
    </div>

    <div class="section-title works-title">
      <span>TA 的作品</span>
      <span class="work-count">{{ videos.length }} 个</span>
    </div>

    <div v-if="videos.length" class="video-grid">
      <VideoCard v-for="v in videos" :key="v.id" :video="v" />
    </div>
    <EmptyState v-else icon="video" title="TA 还没有发布作品" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { apiUserInfo, apiUserVideos, apiFollow, apiUnfollow } from '../api'
import { useUserStore } from '../stores/user'
import VideoCard from '../components/VideoCard.vue'
import EmptyState from '../components/EmptyState.vue'
import AppIcon from '../components/AppIcon.vue'

const route = useRoute()
const userStore = useUserStore()
const user = ref(null)
const videos = ref([])
const followed = ref(false)

const fmtNum = n => {
  if (!n) return 0
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  return n
}

async function toggleFollow() {
  if (followed.value) {
    await apiUnfollow(user.value.id)
    followed.value = false
    user.value.fansCount = Math.max(0, user.value.fansCount - 1)
  } else {
    await apiFollow(user.value.id)
    followed.value = true
    user.value.fansCount++
  }
}

onMounted(async () => {
  user.value = await apiUserInfo(route.params.id)
  followed.value = !!user.value.followed
  videos.value = (await apiUserVideos(route.params.id, 1, 50)).list
})
</script>

<style scoped>
.profile-head {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  margin-bottom: 24px;
}

.big-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.profile-info {
  flex: 1;
  min-width: 0;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name-row h1 {
  font-size: 20px;
  font-weight: 600;
}

.signature {
  margin-top: 4px;
  font-size: 13px;
  color: var(--text-2);
}

.stats {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-top: 12px;
  font-size: 12px;
  color: var(--text-3);
}

.stats b {
  color: var(--text-1);
  font-weight: 600;
  margin-right: 3px;
}

.location {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}

.profile-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.works-title {
  margin-bottom: 14px;
}

.work-count {
  font-size: 12px;
  font-weight: 400;
  color: var(--text-3);
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 22px 16px;
}

@media (max-width: 900px) {
  .video-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .profile-head {
    flex-wrap: wrap;
  }

  .profile-actions {
    width: 100%;
  }
}

@media (max-width: 640px) {
  .video-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 18px 12px;
  }

  .profile-head {
    padding: 18px;
  }

  .big-avatar {
    width: 56px;
    height: 56px;
  }
}
</style>
