<template>
  <div class="page-card">
    <div class="toolbar">
      <span class="muted">新发布的视频需经平台审核后公开可见</span>
      <span class="toolbar-end muted">共 {{ videos.length }} 个作品</span>
    </div>
    <el-table :data="videos" v-loading="loading">
      <el-table-column label="封面" width="70">
        <template #default="{ row }">
          <img :src="row.coverUrl" class="t-cover" />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          <div v-if="row.status === 3" class="reject">{{ row.rejectReason }}</div>
        </template>
      </el-table-column>
      <el-table-column label="数据" width="150">
        <template #default="{ row }">
          <span class="stat">{{ row.viewCount }} 播放 · {{ row.likeCount }} 赞 · {{ row.commentCount }} 评论</span>
        </template>
      </el-table-column>
      <el-table-column label="可见性" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="row.isPublic === 1 ? 'success' : 'info'">
            {{ row.isPublic === 1 ? '公开' : '私密' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" width="150">
        <template #default="{ row }">{{ fmt(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="190" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="preview(row)">预览</el-button>
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-dropdown>
            <el-button size="small" text>···</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="togglePublic(row)">
                  {{ row.isPublic ? '设为私密' : '设为公开' }}
                </el-dropdown-item>
                <el-dropdown-item divided @click="remove(row)">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="editVisible" title="编辑作品" width="480">
      <el-form label-width="70px">
        <el-form-item label="标题"><el-input v-model="editForm.title" maxlength="40" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="editForm.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="封面URL"><el-input v-model="editForm.coverUrl"
            placeholder="/upload/cover/xxx.svg" /></el-form-item>
        <el-form-item label="时长(秒)"><el-input-number v-model="editForm.duration" :min="1" :max="86400" /></el-form-item>
        <el-form-item label="位置"><el-input v-model="editForm.geoLocation" placeholder="如：肇庆" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveEdit">保存修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiMyVideos, apiUpdateVideo, apiDeleteVideo, apiSetVisibility } from '../api'

const videos = ref([])
const loading = ref(false)
const editVisible = ref(false)
const saving = ref(false)
const editForm = ref({})
const editId = ref(null)
const fmt = t => (t || '').replace('T', ' ').slice(0, 16)
const statusText = s => ({ 0: '待审核', 1: '已发布', 2: '已下架', 3: '审核拒绝' }[s] || s)
const statusType = s => ({ 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }[s] || 'info')

async function load() {
  loading.value = true
  try {
    videos.value = (await apiMyVideos(1)).list
  } finally { loading.value = false }
}

function preview(row) {
  // 用户端在 5173 端口，创作者端(5174)用新标签页打开预览
  window.open(`http://localhost:5173/#/video/${row.id}`, '_blank')
}

function openEdit(row) {
  editId.value = row.id
  editForm.value = {
    title: row.title,
    description: row.description || '',
    coverUrl: row.coverUrl || '',
    duration: row.duration,
    geoLocation: row.geoLocation || ''
  }
  editVisible.value = true
}

async function saveEdit() {
  if (!editForm.value.title || !editForm.value.title.trim()) {
    return ElMessage.warning('标题不能为空')
  }
  saving.value = true
  try {
    await apiUpdateVideo(editId.value, {
      title: editForm.value.title.trim(),
      description: editForm.value.description,
      coverUrl: editForm.value.coverUrl,
      duration: editForm.value.duration,
      geoLocation: editForm.value.geoLocation
    })
    ElMessage.success('保存成功')
    editVisible.value = false
    load()
  } finally { saving.value = false }
}

async function togglePublic(row) {
  await apiSetVisibility(row.id, row.isPublic ? 0 : 1)
  row.isPublic = row.isPublic ? 0 : 1
  ElMessage.success('已更新')
}
async function remove(row) {
  await ElMessageBox.confirm('确定删除该作品吗？', '提示', { type: 'warning' })
  await apiDeleteVideo(row.id)
  videos.value = videos.value.filter(v => v.id !== row.id)
  ElMessage.success('已删除')
}

onMounted(load)
</script>

<style scoped>
.t-cover {
  width: 36px;
  height: 64px;
  object-fit: cover;
  border-radius: 4px;
  display: block;
}

.stat {
  font-size: 12px;
  color: var(--text-3);
}

.reject {
  font-size: 11px;
  color: var(--danger);
  margin-top: 4px;
  max-width: 120px;
  line-height: 1.4;
}
</style>
