<template>
  <div class="auth-page">
    <section class="brand-panel">
      <div class="brand-top" @click="$router.push('/')">
        <span class="brand-mark"><AppIcon name="play" :size="18" :stroke-width="2" /></span>
        <span class="brand-name">星海</span>
      </div>
      <div class="brand-copy">
        <h1>加入星海<br />开始你的创作</h1>
        <p>注册即可获得 10GB 上传空间，发布作品、积累粉丝、开启内容变现。</p>
        <ul class="feature-list">
          <li><AppIcon name="upload" :size="18" />高清视频上传与管理</li>
          <li><AppIcon name="grid" :size="18" />作品数据与粉丝画像</li>
          <li><AppIcon name="shop" :size="18" />橱窗带货与订单管理</li>
        </ul>
      </div>
      <p class="brand-foot">星海 · XingHai Video Community</p>
    </section>

    <section class="form-panel">
      <div class="form-wrap">
        <h2>创建账号</h2>
        <p class="form-sub">已有账号？<router-link to="/login" class="link">去登录</router-link></p>

        <el-form :model="form" label-position="top" @submit.prevent>
          <el-form-item label="用户名">
            <el-input v-model="form.username" size="large" placeholder="3-20 位字母或数字" maxlength="20" clearable />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="form.nickname" size="large" placeholder="展示给其他用户的名字" maxlength="20" clearable />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" size="large" placeholder="至少 6 位字符" show-password />
          </el-form-item>
          <el-form-item label="所在城市（选填）">
            <el-input v-model="form.location" size="large" placeholder="用于同城内容推荐" maxlength="20" clearable />
          </el-form-item>
          <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="submit">
            注册并登录
          </el-button>
        </el-form>

        <p class="legal">注册即代表你已阅读并同意《用户协议》与《社区规范》</p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import AppIcon from '../components/AppIcon.vue'

const router = useRouter()
const userStore = useUserStore()
const form = ref({ username: '', nickname: '', password: '', location: '' })
const loading = ref(false)

async function submit() {
  if (!form.value.username.trim() || !form.value.nickname.trim() || form.value.password.length < 6) {
    ElMessage.warning('请完整填写注册信息，密码至少 6 位')
    return
  }
  loading.value = true
  try {
    await userStore.register({
      username: form.value.username.trim(),
      nickname: form.value.nickname.trim(),
      password: form.value.password,
      location: form.value.location.trim()
    })
    ElMessage.success('注册成功，欢迎来到星海')
    router.push('/')
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
  font-size: 32px;
  line-height: 1.4;
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
  margin: 8px 0 24px;
  font-size: 13px;
  color: var(--text-3);
}
.link {
  color: var(--brand);
}
.form-wrap :deep(.el-form-item) {
  margin-bottom: 14px;
}
.form-wrap :deep(.el-form-item__label) {
  font-size: 13px;
  color: var(--text-2);
  padding-bottom: 4px;
}
.submit-btn {
  width: 100%;
  height: 42px;
  margin-top: 6px;
  font-size: 15px;
}
.legal {
  margin-top: 24px;
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
