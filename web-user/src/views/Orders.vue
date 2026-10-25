<template>
  <div class="page-container orders-page">
    <div class="page-heading">
      <h1>我的订单</h1>
      <span class="sub">查看订单状态与物流信息</span>
    </div>

    <div class="orders-card surface-card">
      <div class="tabs">
        <button
          v-for="(label, key) in statusMap"
          :key="key"
          class="tab"
          :class="{ active: status === key }"
          @click="status = key; load()"
        >{{ label }}</button>
      </div>

      <div class="order-list" v-if="orders.length">
        <div v-for="o in orders" :key="o.id" class="order-item">
          <div class="order-top">
            <span class="order-no">订单号 {{ o.orderNo }}</span>
            <span class="order-time">{{ fmt(o.createTime) }}</span>
            <el-tag size="small" :type="statusType(o.status)">{{ statusMap[o.status] }}</el-tag>
          </div>
          <div class="order-body">
            <img :src="o.productCover" class="o-cover" />
            <div class="o-info">
              <div class="o-name">{{ o.productName }}</div>
              <div class="o-meta">数量 ×{{ o.quantity }}</div>
              <div class="o-meta">收货地址：{{ o.address }}</div>
            </div>
            <div class="o-right">
              <div class="o-amount"><em>¥</em>{{ o.totalAmount }}</div>
              <div class="o-actions">
                <el-button v-if="o.status === 0" size="small" type="primary" @click="pay(o)">去支付</el-button>
                <el-button v-if="o.status === 0" size="small" @click="cancel(o)">取消订单</el-button>
                <el-button v-if="o.status === 2" size="small" type="primary" @click="complete(o)">确认收货</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <EmptyState v-else icon="receipt" title="暂无相关订单" description="去商城挑选喜欢的好物吧" action-text="去商城" @action="$router.push('/mall')" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiMyOrders, apiPayOrder, apiCancelOrder, apiCompleteOrder } from '../api'
import EmptyState from '../components/EmptyState.vue'

const statusMap = { '': '全部', 0: '待付款', 1: '已付款', 2: '已发货', 3: '已完成', 4: '已取消' }
const status = ref('')
const orders = ref([])
const fmt = t => (t || '').replace('T', ' ').slice(0, 16)
const statusType = s => ({ 0: 'warning', 1: 'primary', 2: 'primary', 3: 'success', 4: 'info' }[s] || 'info')

async function load() {
  const res = await apiMyOrders(status.value || undefined, 1)
  orders.value = res.list
}

async function pay(o) {
  await ElMessageBox.confirm(`确认支付 ¥${o.totalAmount}？`, '订单支付', { confirmButtonText: '确认支付' })
  await apiPayOrder(o.id)
  ElMessage.success('支付成功')
  load()
}
async function cancel(o) {
  await apiCancelOrder(o.id)
  ElMessage.success('订单已取消')
  load()
}
async function complete(o) {
  await apiCompleteOrder(o.id)
  ElMessage.success('确认收货成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.orders-page {
  max-width: 1040px;
}
.orders-card {
  padding: 0 20px 20px;
}
.tabs {
  display: flex;
  gap: 6px;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 16px;
  overflow-x: auto;
}
.tab {
  height: 46px;
  padding: 0 14px;
  border: none;
  background: none;
  font-size: 14px;
  color: var(--text-2);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  white-space: nowrap;
}
.tab:hover {
  color: var(--text-1);
}
.tab.active {
  color: var(--brand);
  border-bottom-color: var(--brand);
  font-weight: 500;
}
.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.order-item {
  border: 1px solid var(--border-light);
  border-radius: 6px;
  overflow: hidden;
}
.order-top {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 14px;
  background: #fafbfc;
  font-size: 12px;
  color: var(--text-3);
}
.order-no {
  color: var(--text-2);
}
.order-time {
  margin-right: auto;
}
.order-body {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
}
.o-cover {
  width: 72px;
  height: 72px;
  border-radius: 6px;
  object-fit: cover;
  flex-shrink: 0;
}
.o-info {
  flex: 1;
  min-width: 0;
}
.o-name {
  font-size: 14px;
  font-weight: 500;
}
.o-meta {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-3);
}
.o-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
  flex-shrink: 0;
}
.o-amount {
  color: #f53f3f;
  font-size: 18px;
  font-weight: 600;
}
.o-amount em {
  font-size: 12px;
  font-style: normal;
}
.o-actions {
  display: flex;
  gap: 6px;
}
@media (max-width: 640px) {
  .orders-card {
    padding: 0 12px 12px;
  }
  .order-body {
    flex-wrap: wrap;
  }
  .o-right {
    width: 100%;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
