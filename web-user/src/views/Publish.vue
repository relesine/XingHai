<template>
  <div class="page-container publish-page">
    <div class="page-heading">
      <h1>发布视频</h1>
      <span class="sub">发布后将进入平台审核，通过后公开展示</span>
    </div>

    <div class="publish-layout">
      <div class="form-card surface-card">
        <el-form :model="form" label-width="88px" class="publish-form">
          <el-form-item label="视频文件" required>
            <el-upload class="video-upload" drag :auto-upload="false" :show-file-list="true" :limit="1"
              accept=".mp4,.mov,.mkv,.webm" :on-change="onVideoChange">
              <div class="upload-icon">
                <AppIcon name="upload" :size="26" />
              </div>
              <div class="upload-text">将视频拖到此处，或 <em>点击选择文件</em></div>
              <div class="upload-tip">支持 mp4 / mov / mkv / webm，单个文件不超过 500MB</div>
            </el-upload>
            <div v-if="uploading" class="upload-progress">
              <el-progress :percentage="progress" :stroke-width="6" />
            </div>
          </el-form-item>

          <el-form-item label="封面">
            <div class="cover-row">
              <el-upload :auto-upload="false" :show-file-list="false" :limit="1" accept="image/*"
                :on-change="onCoverChange">
                <div class="cover-box" :class="{ filled: form.coverUrl }">
                  <img v-if="form.coverUrl" :src="form.coverUrl" class="cover-preview" />
                  <template v-else>
                    <AppIcon name="plus" :size="20" />
                    <span>上传封面</span>
                  </template>
                </div>
              </el-upload>
              <p class="cover-tip">建议使用竖版封面（3:4），画面清晰、主体突出，不加遮挡文字</p>
            </div>
          </el-form-item>

          <el-form-item label="标题" required>
            <el-input v-model="form.title" placeholder="填写作品标题，让观众快速了解内容" maxlength="50" show-word-limit />
          </el-form-item>

          <el-form-item label="简介">
            <el-input v-model="form.description" type="textarea" :rows="3" maxlength="300" show-word-limit
              placeholder="补充作品内容说明（选填）" />
          </el-form-item>

          <el-form-item label="分区" required>
            <el-select v-model="form.categoryId" placeholder="选择内容分区" style="width: 220px" @change="loadTags">
              <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item>

          <el-form-item label="标签">
            <el-select v-model="form.tagIds" multiple placeholder="选择内容标签，帮助观众发现作品" style="width: 100%">
              <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
            </el-select>
          </el-form-item>

          <el-form-item label="可见范围">
            <el-radio-group v-model="form.isPublic">
              <el-radio :value="1">公开</el-radio>
              <el-radio :value="0">仅自己可见</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="允许评论">
            <el-switch v-model="form.allowComment" :active-value="1" :inactive-value="0" />
          </el-form-item>

          <el-form-item label="拍摄地点">
            <el-input v-model="form.geoLocation" placeholder="如：广州（选填）" style="width: 220px" maxlength="20" />
          </el-form-item>

          <el-form-item>
            <div class="submit-row">
              <el-button type="primary" size="large" :loading="publishing"
                :disabled="!form.videoUrl || !form.title || !form.categoryId" @click="publish">发布作品</el-button>
              <el-button size="large" @click="$router.push('/profile')">保存并退出</el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>

      <aside class="side-col">
        <div class="side-card surface-card">
          <h3>发布须知</h3>
          <ul>
            <li>作品需为原创或已获授权内容</li>
            <li>禁止发布违法违规、低俗及侵权内容</li>
            <li>视频进入审核后通常在 1 个工作日内完成</li>
            <li>标题与封面应真实反映视频内容</li>
          </ul>
        </div>

        <div class="side-card surface-card" v-if="profile">
          <h3>上传空间</h3>
          <div class="quota-line">
            <span>已使用</span>
            <span class="quota-num">{{ (profile.quotaUsed / 1024 / 1024).toFixed(0) }} MB</span>
          </div>
          <el-progress :percentage="quotaPercent" :stroke-width="6" :show-text="false" />
          <p class="quota-total">总容量 {{ (profile.quotaTotal / 1024 / 1024 / 1024).toFixed(0) }} GB</p>
        </div>

        <div class="side-card surface-card">
          <h3>创作建议</h3>
          <p class="side-text">清晰的封面和准确的标签能显著提升作品曝光。发布后可在个人中心随时查看审核状态。</p>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { apiCategories, apiTags, apiPublishVideo, apiProfile } from '../api'
import request from '../api/request'
import AppIcon from '../components/AppIcon.vue'

const router = useRouter()
const categories = ref([])
const tags = ref([])
const profile = ref(null)
const uploading = ref(false)
const progress = ref(0)
const publishing = ref(false)
const form = ref({
  title: '',
  description: '',
  categoryId: null,
  tagIds: [],
  videoUrl: '',
  coverUrl: '',
  duration: 0,
  isPublic: 1,
  allowComment: 1,
  geoLocation: ''
})

const quotaPercent = computed(() => {
  if (!profile.value || !profile.value.quotaTotal) return 0
  return Math.min(100, Math.round(profile.value.quotaUsed * 100 / profile.value.quotaTotal))
})

async function onVideoChange(file) {
  if (!file.raw) return
  const fd = new FormData()
  fd.append('file', file.raw)
  uploading.value = true
  progress.value = 10
  try {
    const url = await request.post('/upload/video', fd, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: e => {
        progress.value = Math.min(99, Math.round((e.loaded * 100) / e.total))
      }
    })
    form.value.videoUrl = url
    progress.value = 100
    ElMessage.success('视频上传成功')
    profile.value = await apiProfile()
  } catch (e) {
    progress.value = 0
  } finally {
    uploading.value = false
  }
}

async function onCoverChange(file) {
  if (!file.raw) return
  const fd = new FormData()
  fd.append('file', file.raw)
  fd.append('dir', 'cover')
  try {
    form.value.coverUrl = await request.post('/upload/image', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    ElMessage.success('封面上传成功')
  } catch (e) { /* 拦截器统一提示 */ }
}

async function loadTags() {
  tags.value = await apiTags(form.value.categoryId)
}

async function publish() {
  publishing.value = true
  try {
    await apiPublishVideo(form.value)
    ElMessage.success('发布成功，等待平台审核')
    router.push('/profile')
  } finally {
    publishing.value = false
  }
}

onMounted(async () => {
  categories.value = await apiCategories()
  try {
    profile.value = await apiProfile()
  } catch (e) { /* 未登录由路由守卫拦截 */ }
})
</script>

<style scoped>
.publish-page {
  max-width: 1040px;
}

.publish-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 20px;
  align-items: start;
}

.form-card {
  padding: 28px 28px 16px;
}

.publish-form :deep(.el-form-item__label) {
  color: var(--text-2);
  font-size: 13px;
}

.video-upload {
  width: 100%;
}

.video-upload :deep(.el-upload) {
  width: 100%;
}

.video-upload :deep(.el-upload-dragger) {
  width: 100%;
  padding: 34px 20px;
  border: 1px dashed #d9dcdf;
  border-radius: 8px;
  background: #fafbfc;
  transition: border-color 0.15s, background 0.15s;
}

.video-upload :deep(.el-upload-dragger:hover) {
  border-color: var(--brand);
  background: #fffafb;
}

.upload-icon {
  color: var(--text-3);
  margin-bottom: 8px;
  display: flex;
  justify-content: center;
}

.upload-text {
  font-size: 14px;
  color: var(--text-2);
}

.upload-text em {
  color: var(--brand);
  font-style: normal;
}

.upload-tip {
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-3);
}

.upload-progress {
  width: 100%;
  margin-top: 10px;
}

.cover-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.cover-box {
  width: 104px;
  height: 138px;
  border: 1px dashed #d9dcdf;
  border-radius: 6px;
  background: #fafbfc;
  color: var(--text-3);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 12px;
  cursor: pointer;
  overflow: hidden;
}

.cover-box:hover {
  border-color: var(--brand);
  color: var(--brand);
}

.cover-box.filled {
  border-style: solid;
  border-color: var(--border-light);
  background: #eef0f2;
}

.cover-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-tip {
  max-width: 300px;
  font-size: 12px;
  line-height: 20px;
  color: var(--text-3);
}

.submit-row {
  display: flex;
  gap: 10px;
}

.side-col {
  display: flex;
  flex-direction: column;
  gap: 14px;
  position: sticky;
  top: 78px;
}

.side-card {
  padding: 18px;
}

.side-card h3 {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 10px;
}

.side-card ul {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.side-card li {
  position: relative;
  padding-left: 12px;
  font-size: 12px;
  line-height: 19px;
  color: var(--text-2);
}

.side-card li::before {
  content: "";
  position: absolute;
  left: 0;
  top: 7px;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--border);
}

.quota-line {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-3);
  margin-bottom: 8px;
}

.quota-num {
  color: var(--text-1);
}

.quota-total {
  margin-top: 8px;
  font-size: 12px;
  color: var(--text-3);
}

.side-text {
  font-size: 12px;
  line-height: 20px;
  color: var(--text-2);
}

@media (max-width: 900px) {
  .publish-layout {
    grid-template-columns: 1fr;
  }

  .side-col {
    position: static;
  }
}

@media (max-width: 640px) {
  .form-card {
    padding: 18px 14px 8px;
  }
}
</style>
