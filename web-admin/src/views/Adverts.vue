<template>
  <div class="page-card">
    <div class="toolbar">
      <el-button type="primary" @click="openAdd">新增广告</el-button>
      <span class="toolbar-end muted">投放中的广告按排序值展示</span>
    </div>
    <el-table :data="adverts" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="图" width="80">
        <template #default="{ row }"><img :src="row.imageUrl" class="ad-img" /></template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="160" />
      <el-table-column prop="linkUrl" label="跳转链接" min-width="180" show-overflow-tooltip />
      <el-table-column prop="position" label="位置" width="90" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '投放中' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="70" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggle(row)">
            {{ row.status === 1 ? '停用' : '投放' }}
          </el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑广告' : '新增广告'" width="540">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="图片">
          <el-upload :auto-upload="false" :limit="1" accept="image/*" :on-change="onImage">
            <el-button>选择图片</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="跳转链接"><el-input v-model="form.linkUrl" placeholder="https://..." /></el-form-item>
        <el-form-item label="投放位置">
          <el-select v-model="form.position">
            <el-option label="首页" value="home" />
            <el-option label="关注流" value="follow" />
            <el-option label="直播页" value="live" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
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
import request from '../api/request'
import { apiAdverts, apiSaveAdvert, apiUpdateAdvert, apiDeleteAdvert } from '../api'

const adverts = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editing = ref(false)
const form = ref({ id: null, title: '', imageUrl: '', linkUrl: '', position: 'home', sort: 0, status: 1 })

async function load() {
  loading.value = true
  try { adverts.value = await apiAdverts() } finally { loading.value = false }
}
function openAdd() {
  editing.value = false
  form.value = { id: null, title: '', imageUrl: '', linkUrl: '', position: 'home', sort: 0, status: 1 }
  dialogVisible.value = true
}
function openEdit(row) {
  editing.value = true
  form.value = { ...row }
  dialogVisible.value = true
}
async function onImage(file) {
  if (!file.raw) return
  const fd = new FormData()
  fd.append('file', file.raw)
  fd.append('dir', 'cover')
  form.value.imageUrl = await request.post('/upload/image', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
  ElMessage.success('图片已上传')
}
async function save() {
  if (!form.value.title.trim()) return ElMessage.warning('请输入标题')
  if (editing.value) await apiUpdateAdvert(form.value)
  else await apiSaveAdvert(form.value)
  dialogVisible.value = false
  ElMessage.success('保存成功')
  load()
}
async function toggle(row) {
  await apiUpdateAdvert({ ...row, status: row.status === 1 ? 0 : 1 })
  load()
}
async function remove(row) {
  await ElMessageBox.confirm(`确定删除广告「${row.title}」？`, '提示', { type: 'warning' })
  await apiDeleteAdvert(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.ad-img {
  width: 64px;
  height: 36px;
  object-fit: cover;
  border-radius: 4px;
  display: block;
}
</style>
