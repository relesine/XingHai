<template>
  <div class="page-container" v-if="shop">
    <div class="breadcrumb">
      <button class="crumb-link" @click="$router.back()">
        <AppIcon name="arrow-left" :size="15" />
        返回
      </button>
      <span class="crumb-sep">/</span>
      <router-link to="/mall" class="crumb-link">商城</router-link>
      <span class="crumb-sep">/</span>
      <span class="crumb-current">{{ shop.shopName }}</span>
    </div>

    <div class="shop-head surface-card">
      <img :src="shop.logo || '/upload/cover/shop1.svg'" class="s-logo" />
      <div class="shop-info">
        <h1>{{ shop.shopName }}</h1>
        <p>{{ shop.description || '暂无店铺简介' }}</p>
      </div>
      <span class="shop-tag">平台认证店铺</span>
    </div>

    <div class="section-title">
      <span>店铺商品</span>
      <span class="count">{{ shop.products?.length || 0 }} 件</span>
    </div>

    <div class="product-grid" v-if="shop.products && shop.products.length">
      <div v-for="p in shop.products" :key="p.id" class="product-card" @click="$router.push(`/product/${p.id}`)">
        <img :src="p.coverUrl" class="p-cover" loading="lazy" />
        <div class="p-info">
          <div class="p-name clamp-2">{{ p.productName }}</div>
          <div class="p-bottom">
            <span class="p-price"><em>¥</em>{{ p.price }}</span>
            <span class="p-sales">已售 {{ p.sales }}</span>
          </div>
        </div>
      </div>
    </div>
    <EmptyState v-else icon="shop" title="店铺暂无商品" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { apiShopDetail } from '../api'
import EmptyState from '../components/EmptyState.vue'
import AppIcon from '../components/AppIcon.vue'

const route = useRoute()
const shop = ref(null)

onMounted(async () => {
  shop.value = await apiShopDetail(route.params.id)
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
.shop-head {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  margin-bottom: 22px;
}
.s-logo {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}
.shop-info {
  flex: 1;
  min-width: 0;
}
.shop-info h1 {
  font-size: 19px;
  font-weight: 600;
}
.shop-info p {
  margin-top: 3px;
  font-size: 13px;
  color: var(--text-2);
}
.shop-tag {
  font-size: 12px;
  color: var(--text-2);
  background: #f4f5f7;
  border-radius: 4px;
  padding: 3px 8px;
  flex-shrink: 0;
}
.count {
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
.p-cover {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  background: #f4f5f7;
}
.p-info {
  padding: 10px 12px 12px;
}
.p-name {
  font-size: 13px;
  line-height: 19px;
  min-height: 38px;
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
