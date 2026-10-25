<template>
  <div class="page-container mall-page">
    <div class="page-heading">
      <h1>星海商城</h1>
      <span class="sub">创作者好物，平台担保交易</span>
      <div class="heading-actions">
        <el-button @click="$router.push('/orders')">
          <AppIcon name="receipt" :size="15" />
          <span class="btn-text">我的订单</span>
        </el-button>
      </div>
    </div>

    <div class="shop-grid" v-if="shops.length">
      <div v-for="s in shops" :key="s.id" class="shop-card" @click="$router.push(`/shop/${s.id}`)">
        <img :src="s.logo || '/upload/cover/shop1.svg'" class="shop-logo" />
        <div class="shop-info">
          <div class="shop-name ellipsis">{{ s.shopName }}</div>
          <div class="shop-desc ellipsis">{{ s.description }}</div>
        </div>
        <span class="shop-enter">进店</span>
      </div>
    </div>

    <div class="section-title goods-title">
      <span>全部好物</span>
      <span class="goods-count">{{ products.length }} 件</span>
    </div>

    <div class="product-grid" v-if="products.length">
      <div v-for="p in products" :key="p.id" class="product-card" @click="$router.push(`/product/${p.id}`)">
        <div class="p-cover-wrap">
          <img :src="p.coverUrl" class="p-cover" loading="lazy" />
        </div>
        <div class="p-info">
          <div class="p-name clamp-2">{{ p.productName }}</div>
          <div class="p-bottom">
            <span class="p-price"><em>¥</em>{{ p.price }}</span>
            <span class="p-sales">已售 {{ p.sales }}</span>
          </div>
        </div>
      </div>
    </div>

    <EmptyState v-else icon="shop" title="商城暂无商品" description="创作者上新后会在这里展示" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { apiProducts, apiShopList } from '../api'
import EmptyState from '../components/EmptyState.vue'
import AppIcon from '../components/AppIcon.vue'

const products = ref([])
const shops = ref([])

onMounted(async () => {
  const [productRes, shopRes] = await Promise.allSettled([apiProducts(1), apiShopList()])
  products.value = productRes.status === 'fulfilled' ? productRes.value.list || [] : []
  shops.value = shopRes.status === 'fulfilled' ? shopRes.value || [] : []
})
</script>

<style scoped>
.mall-page {
  max-width: 1240px;
}
.btn-text {
  margin-left: 5px;
}
.shop-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 26px;
}
.shop-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: 8px;
  cursor: pointer;
}
.shop-card:hover {
  border-color: #e0c3cc;
}
.shop-logo {
  width: 52px;
  height: 52px;
  border-radius: 6px;
  object-fit: cover;
  flex-shrink: 0;
}
.shop-info {
  min-width: 0;
  flex: 1;
}
.shop-name {
  font-size: 14px;
  font-weight: 500;
}
.shop-desc {
  margin-top: 2px;
  font-size: 12px;
  color: var(--text-3);
}
.shop-enter {
  font-size: 12px;
  color: var(--brand);
  flex-shrink: 0;
}
.goods-title {
  margin-bottom: 14px;
}
.goods-count {
  font-size: 12px;
  font-weight: 400;
  color: var(--text-3);
}
.product-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 18px 14px;
}
.product-card {
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
}
.product-card:hover {
  border-color: #e0c3cc;
}
.p-cover-wrap {
  background: #f4f5f7;
}
.p-cover {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
}
.p-info {
  padding: 10px 12px 12px;
}
.p-name {
  font-size: 13px;
  line-height: 19px;
  min-height: 38px;
  color: var(--text-1);
}
.p-bottom {
  display: flex;
  align-items: baseline;
  margin-top: 6px;
}
.p-price {
  color: #f53f3f;
  font-size: 17px;
  font-weight: 600;
}
.p-price em {
  font-size: 12px;
  font-style: normal;
  margin-right: 1px;
}
.p-sales {
  margin-left: auto;
  font-size: 11px;
  color: var(--text-3);
}
@media (max-width: 1000px) {
  .product-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}
@media (max-width: 760px) {
  .shop-grid {
    grid-template-columns: 1fr;
  }
  .product-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
@media (max-width: 560px) {
  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
