<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索用户名 / 昵称" style="width:220px" clearable @keyup.enter="load" />
      <el-button type="primary" @click="load">搜索</el-button>
      <span class="toolbar-end muted">共 {{ total }} 位用户</span>
    </div>
    <el-table :data="list" v-loading="loading">
      <el-table-column label="头像" width="60">
        <template #default="{ row }"><img :src="row.avatar" class="avatar" /></template>
      </el-table-column>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="140" />
      <el-table-column label="角色" width="160">
        <template #default="{ row }">
          <el-tag v-for="r in row.roles" :key="r" size="small"
            :type="r === 'ADMIN' ? 'danger' : r === 'CREATOR' ? 'primary' : 'info'" style="margin-right:4px">{{ r
            }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创作者认证" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="row.creator ? 'success' : 'info'">{{ row.creator ? '已认证' : '未认证' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="location" label="城市" width="100" />
      <el-table-column prop="followCount" label="关注" width="70" />
      <el-table-column prop="fansCount" label="粉丝" width="70" />
      <el-table-column label="上传配额" width="110">
        <template #default="{ row }">{{ fmtQuota(row.quotaUsed) }}/{{ fmtQuota(row.quotaTotal) }}</template>
      </el-table-column>
      <el-table-column label="注册时间" width="160">
        <template #default="{ row }">{{ fmt(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="authCreator(row)">认证创作者</el-button>
          <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggle(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
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
import { apiUsers, apiUserStatus, apiAssignRole, apiRoles } from '../api'

const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const loading = ref(false)
const roles = ref([])
const fmt = t => (t || '').replace('T', ' ').slice(0, 16)
const fmtQuota = b => b == null ? '-' : (b / 1024 / 1024 / 1024).toFixed(1) + 'GB'

async function load() {
  loading.value = true
  try {
    const res = await apiUsers(pageNum.value, pageSize.value, keyword.value)
    list.value = res.list
    total.value = res.total
  } finally { loading.value = false }
}
function onPage(p) { pageNum.value = p; load() }

async function authCreator(row) {
  if (!roles.value.length) roles.value = await apiRoles()
  const creatorRole = roles.value.find(r => r.code === 'CREATOR')
  if (!creatorRole) return ElMessage.warning('系统中未配置创作者角色')
  await ElMessageBox.confirm(`将「${row.nickname}」认证为创作者？`, '提示')
  await apiAssignRole(row.id, creatorRole.id)
  ElMessage.success('认证成功')
  load()
}
async function toggle(row) {
  const target = row.status === 1 ? 0 : 1
  await ElMessageBox.confirm(`确认${target === 0 ? '禁用' : '启用'}用户「${row.nickname}」？`, '提示', { type: 'warning' })
  await apiUserStatus(row.id, target)
  ElMessage.success('操作成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  display: block;
}
</style>
