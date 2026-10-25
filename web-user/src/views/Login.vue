<template>
  <div class="auth-page">
    <section class="brand-panel">
      <div class="brand-top" @click="$router.push('/')">
        <span class="brand-mark"><AppIcon name="play" :size="18" :stroke-width="2" /></span>
        <span class="brand-name">星海</span>
      </div>
      <div class="brand-copy">
        <h1>记录每一片星海</h1>
        <p>一个专注于真实表达的视频社区。发布作品、发现同好、与创作者一起成长。</p>
        <ul class="feature-list">
          <li><AppIcon name="video" :size="18" />短视频内容社区</li>
          <li><AppIcon name="message" :size="18" />创作者与粉丝直接互动</li>
          <li><AppIcon name="shop" :size="18" />内容与好物一站式连接</li>
        </ul>
      </div>
      <p class="brand-foot">星海 · XingHai Video Community</p>
    </section>

    <section class="form-panel">
      <div class="form-wrap">
        <h2>登录星海</h2>
        <p class="form-sub">还没有账号？<router-link to="/register" class="link">立即注册</router-link></p>

        <el-form :model="form" label-position="top" @submit.prevent>
          <el-form-item label="账号">
            <el-input v-model="form.username" size="large" placeholder="请输入用户名" clearable>
              <template #prefix><AppIcon name="user" :size="16" /></template>
            </el-input>
          </el-form-item>
          <el-form-item label="密码">
            <el-input
              v-model="form.password"
              type="password"
              size="large"
              placeholder="请输入密码"
              show-password
              @keyup.enter="submit"
            >
              <template #prefix><AppIcon name="shield" :size="16" /></template>
            </el-input>
          </el-form-item>
          <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="submit">
            登录
          </el-button>
        </el-form>

        <p class="legal">登录即代表你已阅读并同意《用户协议》与《隐私政策》</p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import AppIcon from '../components/AppIcon.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const form = ref({ username: '', password: '' })
const loading = ref(false)

async function submit() {
  if (!form.value.username.trim() || !form.value.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    await userStore.login(form.value)
    router.push(route.query.redirect || '/')
  } catch (e) {
    /* 请求拦截器已统一提示 */
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  background: #fff;
}
.brand-panel {
  background: #fafafa;
  border-right: 1px solid var(--border-light);
  padding: 40px 56px;
  display: flex;
  flex-direction: column;
}
.brand-top {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  width: fit-content;
}
.brand-mark {
  width: 34px;
  height: 34px;
  border-radius: 9px;
  background: var(--brand);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}
.brand-name {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 1px;
}
.brand-copy {
  margin: auto 0;
  max-width: 400px;
}
.brand-copy h1 {
  font-size: 34px;
  line-height: 1.35;
  font-weight: 600;
  letter-spacing: 1px;
}
.brand-copy p {
  margin-top: 14px;
  font-size: 14px;
  line-height: 24px;
  color: var(--text-2);
}
.feature-list {
  list-style: none;
  margin-top: 28px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.feature-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: var(--text-2);
}
.feature-list svg {
  color: var(--text-3);
}
.brand-foot {
  font-size: 12px;
  color: var(--text-3);
}
.form-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
}
.form-wrap {
  width: 100%;
  max-width: 360px;
}
.form-wrap h2 {
  font-size: 26px;
  font-weight: 600;
}
.form-sub {
  margin: 8px 0 28px;
  font-size: 13px;
  color: var(--text-3);
}
.link {
  color: var(--brand);
}
.form-wrap :deep(.el-form-item) {
  margin-bottom: 18px;
}
.form-wrap :deep(.el-form-item__label) {
  font-size: 13px;
  color: var(--text-2);
  padding-bottom: 6px;
}
.submit-btn {
  width: 100%;
  height: 42px;
  margin-top: 4px;
  font-size: 15px;
}
.legal {
  margin-top: 26px;
  font-size: 11px;
  line-height: 18px;
  color: var(--text-3);
}
@media (max-width: 860px) {
  .auth-page {
    grid-template-columns: 1fr;
  }
  .brand-panel {
    display: none;
  }
  .form-panel {
    min-height: 100vh;
  }
}
</style>
