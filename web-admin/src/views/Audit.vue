<template>
  <div class="page-card">
    <div class="toolbar">
      <el-radio-group v-model="status" @change="load">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button :value="0">待审核</el-radio-button>
        <el-radio-button :value="1">已发布</el-radio-button>
        <el-radio-button :value="2">已下架</el-radio-button>
        <el-radio-button :value="3">已拒绝</el-radio-button>
      </el-radio-group>
      <span class="toolbar-end muted">共 {{ total }} 条视频</span>
    </div>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="封面" width="60">
        <template #default="{ row }"><img :src="row.coverUrl" class="cover" /></template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="authorName" label="作者" width="110" />
      <el-table-column prop="categoryName" label="分类" width="90" />
      <el-table-column label="数据" width="130">
        <template #default="{ row }">
          <span class="stat-item">播放 {{ row.viewCount }}</span>
          <span class="stat-item">点赞 {{ row.likeCount }}</span>
          <span class="stat-item">评论 {{ row.commentCount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          <div v-if="row.status === 3" class="reject">{{ row.rejectReason }}</div>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" width="160">
        <template #default="{ row }">{{ fmt(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" size="small" type="success" @click="audit(row, 'pass')">通过</el-button>
          <el-button v-if="row.status === 0" size="small" type="danger" @click="audit(row, 'reject')">拒绝</el-button>
          <el-button size="small" @click="previewVideo(row.id)">预览</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <el-pagination background layout="prev, pager, next, total" :total="total" :page-size="pageSize"
        :current-page="pageNum" @current-change="onPage" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiAdminVideos, apiAudit } from '../api'

const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const status = ref('')
const loading = ref(false)
const fmt = t => (t || '').replace('T', ' ').slice(0, 16)
const statusText = s => ({ 0: '待审核', 1: '已发布', 2: '已下架', 3: '已拒绝' }[s] || s)
const statusType = s => ({ 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }[s] || 'info')

async function load() {
  loading.value = true
  try {
    const res = await apiAdminVideos(pageNum.value, pageSize.value, status.value === '' ? undefined : status.value)
    list.value = res.list
    total.value = res.total
  } finally { loading.value = false }
}
function onPage(p) { pageNum.value = p; load() }

async function audit(row, action) {
  let rejectReason = ''
  if (action === 'reject') {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝视频', { inputPlaceholder: '如：涉嫌违规内容' })
    rejectReason = value || '不符合平台规范'
  }
  await apiAudit(row.id, action, rejectReason)
  ElMessage.success(action === 'pass' ? '已通过，视频公开可见' : '已拒绝')
  load()
}

// 用户端是独立工程（Hash 路由），预览属于外部链接，必须用 window.open 新开页签
// 地址可在 .env 中通过 VITE_USER_URL 覆盖；本地开发默认 5173
const USER_SITE_URL = import.meta.env.VITE_USER_URL || 'http://localhost:5173'
function previewVideo(id) {
  window.open(`${USER_SITE_URL}/#/video/${id}`, '_blank')
}

onMounted(load)
</script>

<style scoped>
.cover {
  width: 34px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
  display: block;
}

.reject {
  font-size: 11px;
  color: var(--danger);
  margin-top: 4px;
  max-width: 140px;
  line-height: 1.4;
}

.stat-item {
  display: inline-block;
  margin-right: 8px;
  color: var(--text-2);
  font-size: 12px;
}
</style>
