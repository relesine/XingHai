<template>
  <div v-if="data" class="dash">
    <div class="page-head">
      <div>
        <h2>数据看板</h2>
        <p class="muted">作品播放、互动与带货数据汇总</p>
      </div>
      <el-button size="small" @click="reload">刷新数据</el-button>
    </div>

    <section class="stat-grid">
      <div v-for="s in stats" :key="s.label" class="stat-card">
        <div class="stat-top">
          <span class="stat-label">{{ s.label }}</span>
          <span class="stat-icon" :class="s.tone"><AppIcon :name="s.icon" :size="16" /></span>
        </div>
        <div class="stat-value" :class="s.tone">{{ s.value }}</div>
        <div class="stat-foot">{{ s.note }}</div>
      </div>
    </section>

    <section class="page-card">
      <div class="card-head">
        <h3>近 7 天播放与获赞趋势</h3>
        <span class="muted">数据每日 00:00 更新</span>
      </div>
      <div ref="chartEl" class="chart"></div>
    </section>

    <section class="page-card today-row">
      <div v-for="t in todayItems" :key="t.label" class="today-item">
        <span class="today-label">{{ t.label }}</span>
        <span class="today-value">{{ t.value }}</span>
      </div>
    </section>
  </div>

  <div v-else class="page-card skeleton-card" v-loading="true"></div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { apiDashboard } from '../api'
import AppIcon from '../components/AppIcon.vue'

const data = ref(null)
const chartEl = ref(null)
let chart = null

const fmt = n => {
  const v = Number(n) || 0
  if (v >= 100000000) return (v / 100000000).toFixed(1) + '亿'
  if (v >= 10000) return (v / 10000).toFixed(1) + '万'
  return String(v)
}
const money = n => '¥' + Number(n || 0).toLocaleString('zh-CN', { maximumFractionDigits: 2 })

const stats = computed(() => {
  const d = data.value || {}
  return [
    { label: '累计播放', value: fmt(d.totalViews), icon: 'play', note: '全部作品累计', tone: '' },
    { label: '累计获赞', value: fmt(d.totalLikes), icon: 'heart', note: '全部作品累计', tone: '' },
    { label: '粉丝总数', value: fmt(d.totalFans), icon: 'users', note: '关注你的用户', tone: '' },
    { label: '作品总数', value: d.totalVideos ?? 0, icon: 'film', note: '含审核中作品', tone: '' },
    { label: '累计销售额', value: money(d.totalSales), icon: 'receipt', note: '店铺成交总额', tone: 'danger' }
  ]
})

const todayItems = computed(() => {
  const d = data.value || {}
  return [
    { label: '今日播放', value: fmt(d.todayViews) },
    { label: '今日获赞', value: fmt(d.todayLikes) },
    { label: '今日评论', value: fmt(d.todayComments) }
  ]
})

const AXIS = {
  axisLine: { lineStyle: { color: '#e3e5e7' } },
  axisLabel: { color: '#86909c', fontSize: 12 },
  splitLine: { lineStyle: { color: '#f1f2f3' } }
}

function renderChart() {
  chart = echarts.init(chartEl.value)
  chart.setOption({
    color: ['#fb7299', '#00aeec'],
    tooltip: { trigger: 'axis' },
    legend: {
      data: ['播放量', '获赞数'],
      top: 0,
      right: 0,
      icon: 'roundRect',
      itemWidth: 8,
      itemHeight: 8,
      textStyle: { color: '#4e5969', fontSize: 12 }
    },
    grid: { left: 44, right: 16, top: 40, bottom: 28 },
    xAxis: { type: 'category', boundaryGap: false, data: data.value.trend.map(t => t.date), ...AXIS },
    yAxis: { type: 'value', minInterval: 1, ...AXIS },
    series: [
      {
        name: '播放量', type: 'line', smooth: true, symbolSize: 5, lineStyle: { width: 2 },
        areaStyle: { opacity: .1 }, data: data.value.trend.map(t => t.views)
      },
      {
        name: '获赞数', type: 'line', smooth: true, symbolSize: 5, lineStyle: { width: 2 },
        data: data.value.trend.map(t => t.likes)
      }
    ]
  })
}

async function reload() {
  data.value = await apiDashboard()
  await nextTick()
  if (!chart) renderChart()
  else {
    chart.setOption({
      xAxis: { data: data.value.trend.map(t => t.date) },
      series: [
        { data: data.value.trend.map(t => t.views) },
        { data: data.value.trend.map(t => t.likes) }
      ]
    })
  }
}

function onResize() { chart && chart.resize() }

onMounted(async () => {
  await reload()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  chart && chart.dispose()
})
</script>

<style scoped>
.dash { display: flex; flex-direction: column; gap: 16px; }
.page-head { display: flex; align-items: flex-end; justify-content: space-between; }
.page-head h2 { font-size: 18px; font-weight: 600; color: var(--text-1); }
.page-head .muted { margin-top: 4px; }

.stat-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(170px, 1fr)); gap: 12px; }
.stat-card {
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 16px 18px;
}
.stat-top { display: flex; align-items: center; justify-content: space-between; }
.stat-label { font-size: 13px; color: var(--text-3); }
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
.stat-icon.danger { background: #ffece8; color: var(--danger); }
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
.stat-value.danger { color: var(--danger); }
.stat-foot { margin-top: 6px; font-size: 12px; color: var(--text-3); }

.card-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.card-head h3 { font-size: 14px; font-weight: 600; color: var(--text-1); }
.chart { height: 300px; width: 100%; }

.today-row { display: flex; gap: 0; padding: 0; }
.today-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 16px 20px;
  border-right: 1px solid var(--border-light);
}
.today-item:last-child { border-right: 0; }
.today-label { font-size: 12px; color: var(--text-3); }
.today-value { font-size: 20px; font-weight: 600; color: var(--text-1); font-variant-numeric: tabular-nums; }

.skeleton-card { height: 320px; }
</style>
