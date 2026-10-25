<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="title" placeholder="输入直播标题" style="width:280px" />
      <el-button type="primary" @click="start">开播</el-button>
      <span class="toolbar-end muted">当前为模拟直播，真实推流需对接 OBS 与流媒体服务</span>
    </div>

    <el-table :data="rooms">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="主播" width="120">
        <template #default="{ row }">
          <div class="author"><img :src="row.avatar" class="a-avatar" />{{ row.nickname }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'danger' : row.status === 0 ? 'info' : 'default'">
            {{ row.status === 1 ? '直播中' : row.status === 0 ? '未开播' : '已结束' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="观看" width="100">
        <template #default="{ row }">{{ row.viewerCount }}</template>
      </el-table-column>
      <el-table-column label="点赞" width="100">
        <template #default="{ row }">{{ row.likeCount }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button v-if="row.status === 1 && isMine(row)" size="small" type="danger" @click="stop(row)">关播</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { apiLiveList, apiLiveStart, apiLiveStop } from '../api'
import { useCreatorStore } from '../stores/creator'

const store = useCreatorStore()
const rooms = ref([])
const title = ref('我的直播间：' + new Date().toLocaleDateString())

const isMine = row => row.userId === store.user?.userId

async function load() {
  rooms.value = await apiLiveList()
}
async function start() {
  if (!title.value.trim()) return ElMessage.warning('请输入直播标题')
  await apiLiveStart(title.value)
  ElMessage.success('已开播')
  load()
}
async function stop(row) {
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

.a-avatar {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  object-fit: cover;
}
</style>
