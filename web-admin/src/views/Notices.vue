<template>
  <div class="page-card">
    <div class="toolbar">
      <el-button type="primary" @click="openAdd">新增公告</el-button>
      <span class="toolbar-end muted">公告发布后展示在用户端首页</span>
    </div>
    <el-table :data="notices" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '发布中' : '已下线' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="70" />
      <el-table-column label="发布时间" width="160">
        <template #default="{ row }">{{ fmt(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggle(row)">
            {{ row.status === 1 ? '下线' : '发布' }}
          </el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑公告' : '新增公告'" width="540">
      <el-form :model="form" label-width="60px">
        <el-form-item label="标题"><el-input v-model="form.title" placeholder="公告标题" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="4"
            placeholder="公告内容" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiNotices, apiSaveNotice, apiUpdateNotice, apiDeleteNotice } from '../api'

const notices = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editing = ref(false)
const form = ref({ id: null, title: '', content: '' })
const fmt = t => (t || '').replace('T', ' ').slice(0, 16)

async function load() {
  loading.value = true
  try { notices.value = await apiNotices() } finally { loading.value = false }
}
function openAdd() {
  editing.value = false
  form.value = { id: null, title: '', content: '' }
  dialogVisible.value = true
}
function openEdit(row) {
  editing.value = true
  form.value = { id: row.id, title: row.title, content: row.content }
  dialogVisible.value = true
}
async function save() {
  if (!form.value.title.trim()) return ElMessage.warning('请输入标题')
  if (editing.value) await apiUpdateNotice(form.value.id, { title: form.value.title, content: form.value.content })
  else await apiSaveNotice(form.value.title, form.value.content)
  dialogVisible.value = false
  ElMessage.success('保存成功')
  load()
}
async function toggle(row) {
  await apiUpdateNotice(row.id, { status: row.status === 1 ? 0 : 1 })
  ElMessage.success('已更新')
  load()
}
async function remove(row) {
  await ElMessageBox.confirm(`确定删除公告「${row.title}」？`, '提示', { type: 'warning' })
  await apiDeleteNotice(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped></style>
