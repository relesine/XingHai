<template>
  <div class="page-card">
    <div v-if="!shop" class="no-shop">
      <h3>还没有店铺</h3>
      <p>开通店铺后，你可以在视频下方挂载商品，实现带货变现。</p>
      <el-form :model="form" label-width="80px" class="create-form">
        <el-form-item label="店铺名称"><el-input v-model="form.shopName" placeholder="如：星海优选" /></el-form-item>
        <el-form-item label="店铺简介"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-button type="primary" @click="create">立即开店</el-button>
      </el-form>
    </div>

    <div v-else>
      <div class="shop-info">
        <img :src="shop.logo || '/upload/cover/shop1.svg'" class="s-logo" />
        <div class="s-detail">
          <h3>{{ shop.shopName }}</h3>
          <p>{{ shop.description }}</p>
          <el-tag size="small" :type="shop.status === 1 ? 'success' : 'info'">
            {{ shop.status === 1 ? '营业中' : '已关闭' }}
          </el-tag>
        </div>
      </div>
      <el-form :model="form" label-width="80px" class="edit-form">
        <el-form-item label="店铺名称"><el-input v-model="form.shopName" /></el-form-item>
        <el-form-item label="店铺简介"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-button type="primary" @click="update">保存修改</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { apiMyShop, apiCreateShop, apiUpdateShop } from '../api'

const shop = ref(null)
const form = ref({ shopName: '', description: '' })

async function load() {
  shop.value = await apiMyShop()
  if (shop.value) {
    form.value = { shopName: shop.value.shopName, description: shop.value.description || '' }
  }
}
async function create() {
  await apiCreateShop(form.value)
  ElMessage.success('店铺创建成功！')
  load()
}
async function update() {
  await apiUpdateShop(shop.value.id, form.value)
  ElMessage.success('保存成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.no-shop { text-align: center; padding: 40px 0; }
.no-shop h3 { font-size: 16px; font-weight: 600; color: var(--text-1); }
.no-shop p { color: var(--text-3); font-size: 13px; margin: 8px 0 22px; }
.create-form, .edit-form { max-width: 480px; margin: 0 auto; text-align: left; }
.shop-info { display: flex; gap: 16px; align-items: center; margin-bottom: 24px; }
.s-logo { width: 64px; height: 64px; border-radius: 8px; object-fit: cover; border: 1px solid var(--border-light); }
.s-detail h3 { font-size: 16px; font-weight: 600; margin-bottom: 6px; }
.s-detail p { color: var(--text-3); margin: 0 0 8px; font-size: 13px; }
</style>
