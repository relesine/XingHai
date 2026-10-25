<template>
  <div class="page-container" v-if="data">
    <div class="breadcrumb">
      <button class="crumb-link" @click="$router.back()">
        <AppIcon name="arrow-left" :size="15" />
        返回
      </button>
      <span class="crumb-sep">/</span>
      <router-link to="/mall" class="crumb-link">商城</router-link>
      <span class="crumb-sep">/</span>
      <span class="crumb-current">{{ data.product.productName }}</span>
    </div>

    <div class="detail-card surface-card">
      <div class="image-col">
        <img :src="data.product.coverUrl" class="main-img" />
      </div>
      <div class="info-col">
        <h1>{{ data.product.productName }}</h1>
        <p class="desc">{{ data.product.description || '暂无商品描述' }}</p>

        <div class="price-box">
          <span class="price"><em>¥</em>{{ data.product.price }}</span>
          <span class="sales">已售 {{ data.product.sales }} 件</span>
        </div>

        <div class="meta-list">
          <div class="meta-line">
            <span class="meta-label">库存</span>
            <span>{{ data.product.stock }} 件</span>
          </div>
          <div class="meta-line">
            <span class="meta-label">配送</span>
            <span>平台合作物流，48 小时内发货</span>
          </div>
          <div class="meta-line">
            <span class="meta-label">保障</span>
            <span>正品保障 · 7 天无理由退换</span>
          </div>
        </div>

        <div class="buy-row">
          <span class="qty-label">数量</span>
          <el-input-number v-model="quantity" :min="1" :max="Math.max(1, data.product.stock)" />
          <el-button type="primary" size="large" :disabled="!userStore.isLogin" @click="orderVisible = true">
            立即购买
          </el-button>
          <el-button v-if="!userStore.isLogin" text @click="$router.push('/login')">登录后购买</el-button>
        </div>

        <div class="shop-info" v-if="data.shop" @click="$router.push(`/shop/${data.shop.id}`)">
          <img :src="data.shop.logo || '/upload/cover/shop1.svg'" class="s-logo" />
          <div class="s-info">
            <div class="s-name">{{ data.shop.shopName }}</div>
            <div class="s-desc ellipsis">{{ data.shop.description }}</div>
          </div>
          <span class="s-enter">进店逛逛</span>
        </div>
      </div>
    </div>

    <el-dialog v-model="orderVisible" title="确认订单" width="440">
      <el-form label-width="80px">
        <el-form-item label="商品">{{ data.product.productName }} × {{ quantity }}</el-form-item>
        <el-form-item label="金额">
          <b class="dialog-price">¥{{ (data.product.price * quantity).toFixed(2) }}</b>
        </el-form-item>
        <el-form-item label="收货地址">
          <el-input v-model="address" type="textarea" :rows="2" placeholder="请输入收货地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="orderVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOrder">提交订单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiProductDetail, apiCreateOrder, apiPayOrder } from '../api'
import { useUserStore } from '../stores/user'
import AppIcon from '../components/AppIcon.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const data = ref(null)
const quantity = ref(1)
const orderVisible = ref(false)
const address = ref('')

async function submitOrder() {
  if (!address.value.trim()) {
    ElMessage.warning('请填写收货地址')
    return
  }
  try {
    const orderId = await apiCreateOrder({
      productId: data.value.product.id,
      quantity: quantity.value,
      address: address.value.trim()
    })
    orderVisible.value = false
    await ElMessageBox.confirm('订单已创建，是否立即支付？', '订单支付', {
      confirmButtonText: '立即支付',
      cancelButtonText: '稍后支付'
    })
    await apiPayOrder(orderId)
    ElMessage.success('支付成功，可在「我的订单」查看')
    router.push('/orders')
  } catch (e) {
    /* 取消支付或拦截器已提示 */
  }
}

onMounted(async () => {
  data.value = await apiProductDetail(route.params.id)
})
</script>

<style scoped>
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--text-3);
  margin: 2px 0 12px;
}
.crumb-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: none;
  background: none;
  color: var(--text-2);
  cursor: pointer;
  font-size: 12px;
}
.crumb-link:hover {
  color: var(--brand);
}
.crumb-sep {
  color: #c9ccd0;
}
.crumb-current {
  max-width: 420px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.detail-card {
  display: grid;
  grid-template-columns: 420px minmax(0, 1fr);
  gap: 32px;
  padding: 24px;
}
.image-col {
  background: #f4f5f7;
  border-radius: 8px;
  overflow: hidden;
}
.main-img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
}
.info-col h1 {
  font-size: 21px;
  line-height: 30px;
  font-weight: 600;
}
.desc {
  margin-top: 8px;
  font-size: 13px;
  line-height: 21px;
  color: var(--text-2);
}
.price-box {
  display: flex;
  align-items: baseline;
  gap: 14px;
  margin: 18px 0;
  padding: 14px 16px;
  background: #fff7f5;
  border-radius: 6px;
}
.price {
  font-size: 28px;
  line-height: 1;
  font-weight: 600;
  color: #f53f3f;
}
.price em {
  font-size: 15px;
  font-style: normal;
  margin-right: 2px;
}
.sales {
  font-size: 12px;
  color: var(--text-3);
}
.meta-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--border-light);
  font-size: 13px;
  color: var(--text-2);
}
.meta-line {
  display: flex;
  gap: 16px;
}
.meta-label {
  width: 38px;
  color: var(--text-3);
  flex-shrink: 0;
}
.buy-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 18px 0;
  flex-wrap: wrap;
}
.qty-label {
  font-size: 13px;
  color: var(--text-3);
}
.buy-row :deep(.el-button--large) {
  height: 40px;
  padding: 0 28px;
}
.shop-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: #fafbfc;
  border: 1px solid var(--border-light);
  border-radius: 8px;
  cursor: pointer;
}
.shop-info:hover {
  border-color: var(--border);
}
.s-logo {
  width: 44px;
  height: 44px;
  border-radius: 6px;
  object-fit: cover;
}
.s-info {
  min-width: 0;
  flex: 1;
}
.s-name {
  font-size: 13px;
  font-weight: 500;
}
.s-desc {
  font-size: 12px;
  color: var(--text-3);
}
.s-enter {
  font-size: 12px;
  color: var(--brand);
}
.dialog-price {
  color: #f53f3f;
  font-size: 16px;
}
@media (max-width: 900px) {
  .detail-card {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  .image-col {
    max-width: 420px;
  }
}
</style>
