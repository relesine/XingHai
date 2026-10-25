<template>
  <div class="page-card">
    <div class="toolbar">
      <span class="muted">对违规直播间可执行强制关播，关播后观众立即断开</span>
    </div>
    <el-table :data="rooms" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="主播" width="140">
        <template #default="{ row }">
          <div class="author"><img :src="row.avatar" class="avatar" />{{ row.nickname }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="直播标题" min-width="200" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'danger' : row.status === 0 ? 'info' : 'default'">
            {{ row.status === 1 ? '直播中' : row.status === 0 ? '未开播' : '已结束' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewerCount" label="观看" width="80" />
      <el-table-column prop="likeCount" label="点赞" width="80" />
      <el-table-column label="开始时间" width="160">
        <template #default="{ row }">{{ fmt(row.startTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" size="small" type="danger" @click="stop(row)">强制关播</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiLiveList, apiLiveStop } from '../api'

const rooms = ref([])
const loading = ref(false)
const fmt = t => (t || '').replace('T', ' ').slice(0, 16)

async function load() {
  loading.value = true
  try { rooms.value = await apiLiveList() } finally { loading.value = false }
}
async function stop(row) {
  await ElMessageBox.confirm(`确认关闭「${row.nickname}」的直播间？`, '提示', { type: 'warning' })
  await apiLiveStop(row.id)
  ElMessage.success('已关播')
  load()
}

onMounted(load)
</script>

<style scoped>
.author {
  display: flex;
  gap: 8px;
  align-items: center;
}

.avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
}
</style>
