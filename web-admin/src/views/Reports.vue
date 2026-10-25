<template>
  <div class="page-card">
    <div class="toolbar">
      <el-radio-group v-model="status" @change="load">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button :value="0">待处理</el-radio-button>
        <el-radio-button :value="1">已处理</el-radio-button>
        <el-radio-button :value="2">已驳回</el-radio-button>
      </el-radio-group>
      <span class="toolbar-end muted">共 {{ total }} 条举报</span>
    </div>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="举报人" width="110">
        <template #default="{ row }">用户 #{{ row.userId }}</template>
      </el-table-column>
      <el-table-column prop="targetType" label="对象类型" width="100">
        <template #default="{ row }">
          <el-tag size="small">{{ { video: '视频', comment: '评论', user: '用户' }[row.targetType] || row.targetType
          }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="targetId" label="对象ID" width="80" />
      <el-table-column prop="reason" label="举报原因" width="140" />
      <el-table-column prop="detail" label="详情" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'warning' : row.status === 1 ? 'success' : 'info'">
            {{ { 0: '待处理', 1: '已处理', 2: '已驳回' }[row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="举报时间" width="160">
        <template #default="{ row }">{{ fmt(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" size="small" type="danger" @click="handle(row, 'agree')">属实·下架</el-button>
          <el-button v-if="row.status === 0" size="small" @click="handle(row, 'reject')">驳回</el-button>
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
import { apiReports, apiHandleReport } from '../api'

const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const status = ref('')
const loading = ref(false)
const fmt = t => (t || '').replace('T', ' ').slice(0, 16)

async function load() {
  loading.value = true
  try {
    const res = await apiReports(pageNum.value, pageSize.value, status.value === '' ? undefined : status.value)
    list.value = res.list
    total.value = res.total
  } finally { loading.value = false }
}
function onPage(p) { pageNum.value = p; load() }

async function handle(row, action) {
  const tip = action === 'agree' ? '确认举报属实？相关视频将被自动下架' : '确认驳回该举报？'
  await ElMessageBox.confirm(tip, '处理举报', { type: 'warning' })
  await apiHandleReport(row.id, action)
  ElMessage.success('处理完成')
  load()
}

onMounted(load)
</script>

<style scoped></style>
