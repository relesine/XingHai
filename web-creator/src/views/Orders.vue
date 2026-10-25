<template>
  <div class="page-card">
    <div class="toolbar">
      <el-radio-group v-model="status" @change="load">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button :value="0">待付款</el-radio-button>
        <el-radio-button :value="1">已付款</el-radio-button>
        <el-radio-button :value="2">已发货</el-radio-button>
        <el-radio-button :value="3">已完成</el-radio-button>
        <el-radio-button :value="4">已取消</el-radio-button>
      </el-radio-group>
    </div>
    <el-table :data="orders" v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" width="200" />
      <el-table-column label="商品" min-width="160">
        <template #default="{ row }">
          <div class="prod"><img :src="row.productCover" class="p-cover" /><span>{{ row.productName }}</span></div>
        </template>
      </el-table-column>
      <el-table-column prop="buyerName" label="买家" width="100" />
      <el-table-column label="金额" width="100">
        <template #default="{ row }"><span class="price">¥{{ row.totalAmount }}</span></template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" width="70" />
      <el-table-column prop="address" label="收货地址" min-width="180" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" size="small" type="primary" @click="ship(row)">发货</el-button>
          <el-button v-if="row.status === 2" size="small" type="success" @click="complete(row)">标记完成</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiShopOrders, apiShipOrder, apiCompleteOrder } from '../api'

const orders = ref([])
const loading = ref(false)
const status = ref('')
const statusText = s => ({ 0: '待付款', 1: '已付款', 2: '已发货', 3: '已完成', 4: '已取消' }[s] || s)
const statusType = s => ({ 0: 'warning', 1: 'primary', 2: 'info', 3: 'success', 4: 'info' }[s] || 'info')

async function load() {
  loading.value = true
  try {
    const res = await apiShopOrders(status.value === '' ? undefined : status.value, 1)
    orders.value = res.list
  } finally { loading.value = false }
}
async function ship(row) {
  await ElMessageBox.confirm(`确认给订单 ${row.orderNo} 发货？`, '提示')
  await apiShipOrder(row.id)
  ElMessage.success('已发货')
  load()
}
async function complete(row) {
  await apiCompleteOrder(row.id)
  ElMessage.success('已标记完成')
  load()
}

onMounted(load)
</script>

<style scoped>
.prod {
  display: flex;
  gap: 8px;
  align-items: center;
}

.p-cover {
  width: 36px;
  height: 36px;
  border-radius: 4px;
  object-fit: cover;
}

.price {
  color: var(--danger);
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}
</style>
