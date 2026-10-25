<template>
  <div class="page-container profile-page" v-if="profile">
    <div class="profile-head surface-card">
      <img :src="profile.avatar || '/upload/avatar/u1.svg'" class="big-avatar" />
      <div class="info">
        <div class="name-row">
          <h1>{{ profile.nickname }}</h1>
          <el-tag v-for="r in profile.roles" :key="r" size="small" :type="r === 'CREATOR' ? 'warning' : 'info'">
            {{ roleName(r) }}
          </el-tag>
        </div>
        <p class="sig">{{ profile.signature || '还没有填写个人简介' }}</p>
        <div class="stats">
          <span><b>{{ profile.videoCount }}</b>作品</span>
          <span><b>{{ profile.fansCount }}</b>粉丝</span>
          <span><b>{{ profile.followCount }}</b>关注</span>
          <span class="quota">上传空间 {{ gb(profile.quotaUsed) }} / {{ gb(profile.quotaTotal) }} GB</span>
        </div>
      </div>
      <div class="head-actions">
        <el-button @click="openEdit">编辑资料</el-button>
        <el-button @click="pwdVisible = true">修改密码</el-button>
      </div>
    </div>

    <div class="content-card surface-card">
      <div class="tabs">
        <button
          v-for="t in tabs"
          :key="t.key"
          class="tab"
          :class="{ active: tab === t.key }"
          @click="switchTab(t.key)"
        >{{ t.label }}</button>
      </div>

      <div v-if="tab === 'works'">
        <div v-if="videos.length" class="work-grid">
          <div v-for="v in videos" :key="v.id" class="work-card">
            <div class="work-cover-wrap" @click="$router.push(`/video/${v.id}`)">
              <img :src="v.coverUrl" class="work-cover" loading="lazy" />
              <span class="status" :class="'st' + v.status">{{ statusText(v.status) }}</span>
              <span class="visibility">{{ v.isPublic ? '公开' : '私密' }}</span>
            </div>
            <div class="work-meta">
              <span class="work-title ellipsis">{{ v.title }}</span>
              <el-dropdown trigger="click">
                <button class="more-btn"><AppIcon name="more" :size="16" /></button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="togglePublic(v)">{{ v.isPublic ? '设为私密' : '设为公开' }}</el-dropdown-item>
                    <el-dropdown-item divided @click="remove(v)">删除作品</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
        <EmptyState
          v-else
          icon="video"
          title="还没有作品"
          description="发布第一条视频，记录你的星海"
          action-text="去发布"
          @action="$router.push('/publish')"
        />
      </div>

      <div v-if="tab === 'likes' || tab === 'collects'">
        <div v-if="currentList.length" class="video-grid">
          <VideoCard v-for="v in currentList" :key="v.id" :video="v" />
        </div>
        <EmptyState
          v-else
          icon="heart"
          :title="tab === 'likes' ? '还没有点赞的视频' : '还没有收藏的视频'"
          description="看到喜欢的内容，记得点赞或收藏"
          action-text="去逛逛"
          @action="$router.push('/')"
        />
      </div>

      <div v-if="tab === 'follow' || tab === 'fans'" class="user-list">
        <div
          v-for="u in userList"
          :key="u.id"
          class="user-row"
          @click="$router.push(`/user/${u.id}`)"
        >
          <img :src="u.avatar || '/upload/avatar/u1.svg'" class="u-avatar" />
          <div class="u-info">
            <div class="u-name">{{ u.nickname }}</div>
            <div class="u-sig ellipsis">{{ u.signature || '暂无简介' }}</div>
          </div>
          <el-tag v-if="u.creator" size="small" type="warning">创作者</el-tag>
        </div>
        <EmptyState v-if="!userList.length" icon="user" title="暂无数据" />
      </div>
    </div>

    <el-dialog v-model="editVisible" title="编辑资料" width="440">
      <el-form label-width="70px">
        <el-form-item label="昵称"><el-input v-model="editForm.nickname" maxlength="20" /></el-form-item>
        <el-form-item label="签名"><el-input v-model="editForm.signature" maxlength="60" /></el-form-item>
        <el-form-item label="城市"><el-input v-model="editForm.location" maxlength="20" /></el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="editForm.gender">
            <el-radio :value="0">保密</el-radio>
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="pwdVisible" title="修改密码" width="400">
      <el-form label-width="80px">
        <el-form-item label="原密码"><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" @click="savePwd">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  apiProfile, apiMyVideos, apiMyLikedVideos, apiMyCollectedVideos, apiFollowing, apiFans,
  apiUpdateProfile, apiUpdatePassword, apiDeleteVideo, apiSetVisibility
} from '../api'
import VideoCard from '../components/VideoCard.vue'
import EmptyState from '../components/EmptyState.vue'
import AppIcon from '../components/AppIcon.vue'

const profile = ref(null)
const tab = ref('works')
const videos = ref([])
const likedVideos = ref([])
const collectedVideos = ref([])
const userList = ref([])
const editVisible = ref(false)
const pwdVisible = ref(false)
const editForm = ref({})
const pwdForm = ref({})

const tabs = [
  { key: 'works', label: '我的作品' },
  { key: 'likes', label: '我的点赞' },
  { key: 'collects', label: '我的收藏' },
  { key: 'follow', label: '我的关注' },
  { key: 'fans', label: '我的粉丝' }
]

const currentList = computed(() => (tab.value === 'likes' ? likedVideos.value : collectedVideos.value))

const roleName = r => ({ USER: '普通用户', CREATOR: '认证创作者', ADMIN: '管理员' }[r] || r)
const statusText = s => ({ 0: '待审核', 1: '已发布', 2: '已下架', 3: '审核拒绝' }[s] || s)
const gb = b => (b ? (b / 1024 / 1024 / 1024).toFixed(1) : 0)

async function loadWorks() {
  videos.value = (await apiMyVideos(1, 50)).list
}
async function loadLikes() {
  likedVideos.value = await apiMyLikedVideos()
}
async function loadCollects() {
  collectedVideos.value = await apiMyCollectedVideos()
}
async function loadFollow(type) {
  userList.value = type === 'follow'
    ? await apiFollowing(profile.value.id)
    : await apiFans(profile.value.id)
}

function switchTab(key) {
  tab.value = key
  if (key === 'works') loadWorks()
  else if (key === 'likes') loadLikes()
  else if (key === 'collects') loadCollects()
  else loadFollow(key)
}

function openEdit() {
  editForm.value = {
    nickname: profile.value.nickname,
    signature: profile.value.signature || '',
    location: profile.value.location || '',
    gender: profile.value.gender
  }
  editVisible.value = true
}

async function togglePublic(v) {
  await apiSetVisibility(v.id, v.isPublic ? 0 : 1)
  v.isPublic = v.isPublic ? 0 : 1
  ElMessage.success('可见性已更新')
}
async function remove(v) {
  await ElMessageBox.confirm('删除后不可恢复，确定删除该作品吗？', '删除作品', { type: 'warning' })
  await apiDeleteVideo(v.id)
  videos.value = videos.value.filter(x => x.id !== v.id)
  ElMessage.success('已删除')
}

async function saveProfile() {
  await apiUpdateProfile(editForm.value)
  editVisible.value = false
  ElMessage.success('保存成功')
  profile.value = await apiProfile()
}
async function savePwd() {
  await apiUpdatePassword(pwdForm.value)
  pwdVisible.value = false
  ElMessage.success('密码已修改，下次请用新密码登录')
}

onMounted(async () => {
  profile.value = await apiProfile()
  loadWorks()
})
</script>

<style scoped>
.profile-page {
  max-width: 1100px;
}
.profile-head {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  margin-bottom: 16px;
}
.big-avatar {
  width: 76px;
  height: 76px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}
.info {
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
.sig {
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
.quota {
  color: var(--text-3);
}
.head-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}
.content-card {
  padding: 0 20px 20px;
}
.tabs {
  display: flex;
  gap: 6px;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 18px;
}
.tab {
  height: 46px;
  padding: 0 14px;
  border: none;
  background: none;
  font-size: 14px;
  color: var(--text-2);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
}
.tab:hover {
  color: var(--text-1);
}
.tab.active {
  color: var(--brand);
  border-bottom-color: var(--brand);
  font-weight: 500;
}
.work-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 22px 16px;
}
.work-card {
  min-width: 0;
}
.work-cover-wrap {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  background: #eef0f2;
  cursor: pointer;
}
.work-cover {
  width: 100%;
  aspect-ratio: 3 / 4;
  object-fit: cover;
}
.status {
  position: absolute;
  top: 6px;
  left: 6px;
  padding: 1px 6px;
  font-size: 11px;
  line-height: 17px;
  border-radius: 3px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
}
.status.st0 { color: #ffd66b; }
.status.st1 { color: #b7f1c8; }
.status.st2 { color: #dfe2e6; }
.status.st3 { color: #ffb3b3; }
.visibility {
  position: absolute;
  right: 6px;
  bottom: 6px;
  padding: 1px 6px;
  font-size: 11px;
  line-height: 17px;
  border-radius: 3px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
}
.work-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
}
.work-title {
  flex: 1;
  min-width: 0;
  font-size: 13px;
  color: var(--text-1);
}
.more-btn {
  width: 26px;
  height: 26px;
  border: none;
  background: transparent;
  color: var(--text-3);
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.more-btn:hover {
  background: #f4f5f7;
  color: var(--text-1);
}
.video-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 22px 16px;
}
.user-list {
  display: flex;
  flex-direction: column;
}
.user-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 8px;
  border-bottom: 1px solid var(--border-light);
  cursor: pointer;
}
.user-row:hover {
  background: #fafbfc;
}
.u-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
}
.u-info {
  flex: 1;
  min-width: 0;
}
.u-name {
  font-size: 14px;
  font-weight: 500;
}
.u-sig {
  font-size: 12px;
  color: var(--text-3);
}
@media (max-width: 900px) {
  .work-grid,
  .video-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
  .profile-head {
    flex-wrap: wrap;
  }
  .head-actions {
    width: 100%;
  }
}
@media (max-width: 640px) {
  .work-grid,
  .video-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 18px 12px;
  }
  .content-card {
    padding: 0 14px 14px;
  }
  .tabs {
    overflow-x: auto;
  }
}
</style>
