<template>
  <div v-if="data" class="dash">
    <div class="page-head">
      <div>
        <h2>数据大盘</h2>
        <p class="muted">平台用户、内容与交易的实时概况</p>
      </div>
      <el-button size="small" @click="reload">刷新数据</el-button>
    </div>

    <section class="stat-grid">
      <div v-for="s in stats" :key="s.label" class="stat-card">
        <div class="stat-top">
          <span class="stat-label">{{ s.label }}</span>
          <span class="stat-icon" :class="s.tone">
            <AppIcon :name="s.icon" :size="16" />
          </span>
        </div>
        <div class="stat-value" :class="s.tone">{{ s.value }}</div>
        <div class="stat-foot">{{ s.note }}</div>
      </div>
    </section>

    <section class="chart-grid">
      <div class="page-card">
        <div class="card-head">
          <h3>近 7 天新增趋势</h3>
          <span class="muted">新增用户 / 新增视频</span>
        </div>
        <div ref="trendEl" class="chart chart-lg"></div>
      </div>
      <div class="page-card">
        <div class="card-head">
          <h3>分类视频分布</h3>
          <span class="muted">按内容分区统计</span>
        </div>
        <div ref="pieEl" class="chart chart-lg"></div>
      </div>
    </section>

    <section class="table-grid">
      <div class="page-card">
        <div class="card-head">
          <h3>最新注册用户</h3>
          <router-link to="/users" class="more">查看全部</router-link>
        </div>
        <el-table :data="latestUsers" size="small" max-height="300">
          <el-table-column prop="id" label="ID" width="64" />
          <el-table-column prop="username" label="账号" min-width="110" show-overflow-tooltip />
          <el-table-column prop="nickname" label="昵称" min-width="110" show-overflow-tooltip />
          <el-table-column label="注册时间" min-width="140">
            <template #default="{ row }">{{ fmt(row.createTime) }}</template>
          </el-table-column>
        </el-table>
      </div>

      <div class="page-card">
        <div class="card-head">
          <h3>最新发布视频</h3>
          <router-link to="/audit" class="more">内容审核</router-link>
        </div>
        <el-table :data="latestVideos" size="small" max-height="300">
          <el-table-column prop="id" label="ID" width="64" />
          <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
          <el-table-column prop="authorName" label="作者" min-width="90" show-overflow-tooltip />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </section>
  </div>

  <div v-else class="page-card skeleton-card" v-loading="true"></div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { apiStats, apiUsers, apiAdminVideos } from '../api'
import AppIcon from '../components/AppIcon.vue'

const data = ref(null)
const trendEl = ref(null)
const pieEl = ref(null)
const latestUsers = ref([])
const latestVideos = ref([])
let trendChart = null
let pieChart = null

const fmt = t => (t || '').replace('T', ' ').slice(0, 16)
const statusText = s => ({ 0: '待审核', 1: '已发布', 2: '已下架', 3: '审核拒绝' }[s] || s)
const statusType = s => ({ 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }[s] || 'info')
const money = n => '¥' + Number(n || 0).toLocaleString('zh-CN', { maximumFractionDigits: 2 })

const stats = computed(() => {
  const d = data.value || {}
  return [
    { label: '用户总数', value: d.userCount ?? 0, icon: 'users', note: `今日新增 ${d.todayNewUser ?? 0}`, tone: '' },
    { label: '创作者', value: d.creatorCount ?? 0, icon: 'crown', note: '已认证创作者', tone: '' },
    { label: '视频总数', value: d.videoCount ?? 0, icon: 'film', note: `今日新增 ${d.todayNewVideo ?? 0}`, tone: '' },
    { label: '待审核视频', value: d.pendingVideoCount ?? 0, icon: 'clock', note: '需尽快处理', tone: 'warning' },
    { label: '订单总数', value: d.orderCount ?? 0, icon: 'bag', note: '全站累计订单', tone: '' },
    { label: '总成交金额', value: money(d.totalSales), icon: 'receipt', note: '全站累计成交', tone: 'danger' },
    { label: '待处理举报', value: d.pendingReport ?? 0, icon: 'flag', note: '内容安全待办', tone: 'warning' }
  ]
})

const AXIS = { axisLine: { lineStyle: { color: '#e5e6eb' } }, axisLabel: { color: '#86909c', fontSize: 12 }, splitLine: { lineStyle: { color: '#f2f3f5' } } }

function render() {
  trendChart = echarts.init(trendEl.value)
  trendChart.setOption({
    color: ['#1677ff', '#00b42a'],
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增用户', '新增视频'], top: 0, right: 0, icon: 'roundRect', itemWidth: 8, itemHeight: 8, textStyle: { color: '#4e5969', fontSize: 12 } },
    grid: { left: 44, right: 24, top: 40, bottom: 28 },
    xAxis: {
      type: 'category', boundaryGap: false, data: data.value.userTrend.map(t => t.date),
      axisLabel: { color: '#86909c', fontSize: 12, formatter: v => v.slice(5) },
      axisLine: { lineStyle: { color: '#e5e6eb' } },
      axisTick: { show: false }
    },
    yAxis: { type: 'value', minInterval: 1, ...AXIS },
    series: [
      {
        name: '新增用户', type: 'line', smooth: true, symbolSize: 5, lineStyle: { width: 2 },
        areaStyle: { opacity: .12 }, data: data.value.userTrend.map(t => t.value)
      },
      {
        name: '新增视频', type: 'line', smooth: true, symbolSize: 5, lineStyle: { width: 2 },
        data: data.value.videoTrend.map(t => t.value)
      }
    ]
  })

  pieChart = echarts.init(pieEl.value)
  pieChart.setOption({
    color: ['#1677ff', '#00b42a', '#ff7d00', '#f53f3f', '#722ed1', '#13c2c2', '#eb2f96', '#fa8c16'],
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: { color: '#4e5969', fontSize: 12 } },
    series: [{
      type: 'pie',
      radius: ['46%', '68%'],
      center: ['50%', '42%'],
      avoidLabelOverlap: true,
      itemStyle: { borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      labelLine: { show: false },
      data: data.value.categoryDist.map(c => ({ name: c.name, value: c.value }))
    }]
  })
}

async function reload() {
  data.value = await apiStats()
  await nextTick()
  if (!trendChart) render()
  else {
    trendChart.setOption({
      series: [
        { data: data.value.userTrend.map(t => t.value) },
        { data: data.value.videoTrend.map(t => t.value) }
      ]
    })
    pieChart.setOption({ series: [{ data: data.value.categoryDist.map(c => ({ name: c.name, value: c.value })) }] })
  }
  const [usersRes, videosRes] = await Promise.all([apiUsers(1, 5), apiAdminVideos(1, 5)])
  latestUsers.value = usersRes.list
  latestVideos.value = videosRes.list
}

function onResize() {
  trendChart && trendChart.resize()
  pieChart && pieChart.resize()
}

onMounted(async () => {
  await reload()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  trendChart && trendChart.dispose()
  pieChart && pieChart.dispose()
})
</script>

<style scoped>
.dash {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
}

.page-head h2 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-1);
}

.page-head .muted {
  margin-top: 4px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
  gap: 12px;
}

.stat-card {
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 16px 18px;
}

.stat-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-label {
  font-size: 13px;
  color: var(--text-3);
}

.stat-icon {
  width: 26px;
  height: 26px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f2f3f5;
  color: var(--text-2);
}

.stat-icon.warning {
  background: #fff7e8;
  color: var(--warning);
}

.stat-icon.danger {
  background: #ffece8;
  color: var(--danger);
}

.stat-value {
  margin-top: 10px;
  font-size: 26px;
  font-weight: 600;
  line-height: 1.2;
  color: var(--text-1);
  font-variant-numeric: tabular-nums;
  letter-spacing: -.3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stat-value.warning {
  color: var(--warning);
}

.stat-value.danger {
  color: var(--danger);
}

.stat-foot {
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-3);
}

.chart-grid {
  display: grid;
  grid-template-columns: 1.6fr 1fr;
  gap: 12px;
}

.table-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.card-head h3 {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-1);
}

.more {
  font-size: 12px;
  color: var(--text-3);
}

.more:hover {
  color: var(--primary);
}

.chart {
  width: 100%;
}

.chart-lg {
  height: 300px;
}

.skeleton-card {
  height: 320px;
}

@media (max-width: 1100px) {

  .chart-grid,
  .table-grid {
    grid-template-columns: 1fr;
  }
}
</style>
