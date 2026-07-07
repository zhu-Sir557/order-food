<template>
  <div class="dashboard">
    <!-- 数据卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #ff6034">
              <el-icon :size="28" color="#fff"><List /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">今日订单数</p>
              <p class="stat-value">{{ dashboard.todayOrderCount }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #07c160">
              <el-icon :size="28" color="#fff"><Money /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">今日营业额</p>
              <p class="stat-value">{{ formatPrice(dashboard.todayRevenue) }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #ff9f2c">
              <el-icon :size="28" color="#fff"><Grid /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">桌台使用率</p>
              <p class="stat-value">{{ dashboard.tableUsage }}%</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #7c4dff">
              <el-icon :size="28" color="#fff"><Food /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">总菜品数</p>
              <p class="stat-value">{{ dashboard.totalDishCount }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表 + 热销列表 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <span>近7天营业额趋势</span>
          </template>
          <v-chart
            class="chart"
            :option="chartOption"
            autoresize
          />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>热销Top10</span>
          </template>
          <el-table :data="dashboard.topDishes" stripe size="small">
            <el-table-column type="index" label="排名" width="60" align="center" />
            <el-table-column prop="dishName" label="菜品名称" />
            <el-table-column prop="orderCount" label="销量" width="80" align="center" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { getDashboard } from '@/api/stats'
import { formatPrice } from '@/utils'
import type { Dashboard } from '@/types'

use([CanvasRenderer, LineChart, TitleComponent, TooltipComponent, GridComponent, LegendComponent])

const dashboard = ref<Dashboard>({
  todayOrderCount: 0,
  todayRevenue: 0,
  tableUsage: 0,
  totalDishCount: 0,
  revenueTrend: [],
  topDishes: []
})

const chartOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    formatter: (params: any) => {
      const item = params[0]
      return `${item.axisValue}<br/>营业额：¥${item.value.toFixed(2)}`
    }
  },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: dashboard.value.revenueTrend.map((item) => item.date)
  },
  yAxis: {
    type: 'value',
    axisLabel: { formatter: '¥{value}' }
  },
  series: [
    {
      name: '营业额',
      type: 'line',
      smooth: true,
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(255,96,52,0.25)' },
            { offset: 1, color: 'rgba(255,96,52,0.01)' }
          ]
        }
      },
      itemStyle: { color: '#ff6034' },
      data: dashboard.value.revenueTrend.map((item) => item.revenue)
    }
  ]
}))

async function loadData(): Promise<void> {
  try {
    dashboard.value = await getDashboard()
  } catch {
    // 错误已在拦截器处理
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 12px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-card-hover);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-label {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.chart-row {
  margin-bottom: 20px;
}

.chart {
  height: 350px;
}

:deep(.el-card) {
  border-radius: 12px;
}
</style>
