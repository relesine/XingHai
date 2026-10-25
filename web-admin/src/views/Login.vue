<template>
  <div class="login-page">
    <div class="login-card">
      <div class="brand">
        <span class="brand-mark">星</span>
        <div class="brand-text">
          <strong>星海管理后台</strong>
          <span>平台运营 · 内容审核 · 权限管理</span>
        </div>
      </div>

      <el-form :model="form" label-position="top" @keyup.enter="submit">
        <el-form-item label="管理员账号">
          <el-input v-model="form.username" size="large" placeholder="请输入账号" autocomplete="username">
            <template #prefix><AppIcon name="user" :size="16" /></template>
          </el-input>
        </el-form-item>
        <el-form-item label="登录密码">
          <el-input
            v-model="form.password"
            type="password"
            size="large"
            placeholder="请输入密码"
            show-password
            autocomplete="current-password"
          >
            <template #prefix><AppIcon name="shield" :size="16" /></template>
          </el-input>
        </el-form-item>
        <el-button type="primary" size="large" class="submit" :loading="loading" @click="submit">登录</el-button>
      </el-form>

      <p class="legal">登录即代表你已阅读并同意平台管理规范与数据安全条款</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAdminStore } from '../stores/admin'
import AppIcon from '../components/AppIcon.vue'

const router = useRouter()
const store = useAdminStore()
const form = ref({ username: '', password: '' })
const loading = ref(false)

async function submit() {
  if (!form.value.username.trim() || !form.value.password) {
    return ElMessage.warning('请输入账号和密码')
  }
  loading.value = true
  try {
    await store.login(form.value)
    router.push('/dashboard')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg);
  padding: 24px;
}
.login-card {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 32px 32px 24px;
}
.brand { display: flex; align-items: center; gap: 11px; margin-bottom: 26px; }
.brand-mark {
  width: 40px;
  height: 40px;
  border-radius: 9px;
  background: var(--primary);
  color: #fff;
  font-size: 19px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.brand-text { display: flex; flex-direction: column; gap: 3px; }
.brand-text strong { font-size: 17px; font-weight: 600; color: var(--text-1); }
.brand-text span { font-size: 12px; color: var(--text-3); }

.submit { width: 100%; margin-top: 4px; }

.legal { margin-top: 18px; text-align: center; font-size: 11px; color: var(--text-4); line-height: 1.6; }
</style>
