<template>
  <div class="page-card" v-if="profile">
    <div class="toolbar">
      <span class="muted">账号资料会同步展示在用户端的个人主页</span>
    </div>
    <el-form :model="form" label-width="80px" style="max-width:480px">
      <el-form-item label="头像">
        <img :src="profile.avatar || '/upload/avatar/c1.svg'" class="avatar" />
        <el-upload :auto-upload="false" :limit="1" accept="image/*" :on-change="onAvatar" style="margin-left:12px">
          <el-button size="small">更换头像</el-button>
        </el-upload>
      </el-form-item>
      <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
      <el-form-item label="签名"><el-input v-model="form.signature" type="textarea" :rows="3" /></el-form-item>
      <el-form-item label="城市"><el-input v-model="form.location" /></el-form-item>
      <el-form-item>
        <el-button type="primary" @click="save">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../api/request'
import { apiProfile, apiUpdateProfile } from '../api'

const profile = ref(null)
const form = ref({ nickname: '', signature: '', location: '' })

async function load() {
  profile.value = await apiProfile()
  form.value = {
    nickname: profile.value.nickname,
    signature: profile.value.signature || '',
    location: profile.value.location || ''
  }
}
async function onAvatar(file) {
  if (!file.raw) return
  const fd = new FormData()
  fd.append('file', file.raw)
  fd.append('dir', 'avatar')
  const url = await request.post('/upload/image', fd, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  await request.put('/user/avatar', { avatar: url })
  ElMessage.success('头像已更新')
  load()
}
async function save() {
  await apiUpdateProfile(form.value)
  ElMessage.success('保存成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid var(--border-light);
}
</style>
