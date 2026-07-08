<template>
  <div>
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">数据看板</h1>
      </div>
    </div>

    <el-row :gutter="[16, 16]" class="mb-lg">
      <el-col :span="4" v-for="(card, index) in statCards" :key="index">
        <div :class="['stat-card', getStatCardClass(card.color)]">
          <div class="flex-between mb-sm">
            <div :class="['stat-card__icon', { 'stat-card__icon--primary': card.color === '#165DFF' }]" :style="{ backgroundColor: card.color + '15' }">
              <el-icon :size="18" :style="{ color: card.color }">
                <component :is="card.icon" />
              </el-icon>
            </div>
            <span v-if="card.trendType === 'up'" class="stat-card__trend stat-card__trend--up">
              <el-icon><ArrowUp /></el-icon>
              {{ card.trend }}
            </span>
            <span v-else-if="card.trendType === 'warning'" class="stat-card__trend stat-card__trend--warning">
              {{ card.trend }}
            </span>
            <span v-else-if="card.trendType === 'percent'" class="stat-card__trend stat-card__trend--up font-mono">
              {{ card.trend }}
            </span>
          </div>
          <div class="stat-card__label">{{ card.title }}</div>
          <div class="stat-card__value">{{ card.value.toLocaleString() }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="[16, 16]">
      <el-col :span="14">
        <el-card class="chart-card">
          <template #header>
            <span class="card-title">分类标注统计</span>
          </template>
          <div ref="annotationChartRef" class="chart-container" />
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card class="chart-card">
          <template #header>
            <span class="card-title">训练趋势</span>
          </template>
          <div ref="trainingChartRef" class="chart-container" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="[16, 16]" class="mt-lg">
      <el-col :span="14">
        <el-card class="table-card">
          <template #header>
            <span class="card-title">最近训练任务</span>
          </template>
          <el-table :data="tasks" row-key="taskId" size="small">
            <el-table-column prop="taskName" label="任务名称" show-overflow-tooltip />
            <el-table-column prop="datasetId" label="数据集" width="120">
              <template #default="scope">
                {{ datasets.find(d => d.datasetId === scope.row.datasetId)?.datasetName || scope.row.datasetId }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="scope">
                <span :style="{ color: statusConfig[scope.row.status]?.color }">{{ statusConfig[scope.row.status]?.text || scope.row.status }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="bestValMetric" label="最佳指标" width="100">
              <template #default="scope">
                <span class="number-font">{{ scope.row.bestValMetric != null ? `${(scope.row.bestValMetric * 100).toFixed(1)}%` : '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="120">
              <template #default="scope">
                {{ dayjs(scope.row.createTime).format('MM-DD HH:mm') }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card class="table-card">
          <template #header>
            <span class="card-title">数据集概览</span>
          </template>
          <el-table :data="datasets" row-key="datasetId" size="small">
            <el-table-column prop="datasetName" label="数据集名称" show-overflow-tooltip />
            <el-table-column prop="audioCount" label="音频数量" width="100">
              <template #default="scope">
                <span class="number-font">{{ scope.row.audioCount || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="标注进度" width="150">
              <template #default="scope">
                <div style="display:flex;flex-direction:column;gap:4px">
                  <div style="height:6px;background-color:#E5E6EB;border-radius:3px;overflow:hidden">
                    <div :style="{ height: '100%', width: `${(scope.row.audioCount || 0) > 0 ? (Math.floor((scope.row.audioCount || 0) * 0.65) / (scope.row.audioCount || 0)) * 100 : 0}%`, backgroundColor: '#165DFF', borderRadius: '3px' }"></div>
                  </div>
                  <span class="number-font" style="font-size:11px;color:#4E5969">{{ Math.floor((scope.row.audioCount || 0) * 0.65) }}/{{ scope.row.audioCount || 0 }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="100">
              <template #default="scope">
                {{ dayjs(scope.row.createTime).format('MM-DD') }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="[16, 16]" class="mt-lg">
      <el-col :span="24">
        <el-card class="status-card">
          <template #header>
            <span class="card-title">系统状态</span>
          </template>
          <div class="grid-4col">
            <div class="status-item">
              <div class="status-dot status-dot--success" />
              <div>
                <div class="text-body text-primary">数据库连接</div>
                <div class="text-sm status-success">正常</div>
              </div>
            </div>
            <div class="status-item">
              <div class="status-dot status-dot--success" />
              <div>
                <div class="text-body text-primary">文件存储服务</div>
                <div class="text-sm status-success">正常</div>
              </div>
            </div>
            <div class="status-item">
              <div class="status-dot status-dot--warning" />
              <div>
                <div class="text-body text-primary">训练服务</div>
                <div class="text-sm status-warning">空闲中</div>
              </div>
            </div>
            <div class="status-item">
              <div class="status-dot status-dot--success" />
              <div>
                <div class="text-body text-primary">API服务</div>
                <div class="text-sm status-success">正常运行</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { getOverview } from '../api/statistics'
import { getAllTasks } from '../api/train'
import { getAllDatasets } from '../api/dataset'
import {
  DataBoard, Mic, Edit, User, CircleCheck, Timer, ArrowUp
} from '@element-plus/icons-vue'

const stats = ref({})
const tasks = ref([])
const datasets = ref([])
const loading = ref(false)
const annotationChartRef = ref(null)
const trainingChartRef = ref(null)

const statusConfig = {
  pending: { color: '#165DFF', text: '等待中' },
  running: { color: '#FF7D00', text: '训练中' },
  success: { color: '#00B42A', text: '已完成' },
  failed: { color: '#F53F3F', text: '失败' },
}

const getStatCardClass = (color) => {
  const colorMap = {
    '#165DFF': 'stat-card--primary',
    '#00B42A': 'stat-card--success',
    '#FF7D00': 'stat-card--warning',
    '#F53F3F': 'stat-card--danger',
    '#722ED1': 'stat-card--purple',
  }
  return colorMap[color] || 'stat-card--primary'
}

const statCards = computed(() => [
  {
    title: '数据集总数',
    value: stats.value.datasetCount || 0,
    icon: DataBoard,
    color: '#165DFF',
    trend: '+12%',
    trendType: 'up',
  },
  {
    title: '音频文件数',
    value: stats.value.audioCount || 0,
    icon: Mic,
    color: '#00B42A',
    trend: '+8%',
    trendType: 'up',
  },
  {
    title: '标注记录数',
    value: stats.value.annotationCount || 0,
    icon: Edit,
    color: '#FF7D00',
    trend: '+24%',
    trendType: 'up',
  },
  {
    title: '用户总数',
    value: stats.value.userCount || 0,
    icon: User,
    color: '#722ED1',
    trend: '+5%',
    trendType: 'up',
  },
  {
    title: '已审核标注',
    value: stats.value.approvedCount || 0,
    icon: CircleCheck,
    color: '#00B42A',
    trend: `${(((stats.value.approvedCount || 0) / Math.max(stats.value.annotationCount || 1, 1)) * 100).toFixed(1)}%`,
    trendType: 'percent',
  },
  {
    title: '待审核标注',
    value: stats.value.pendingReviewCount || 0,
    icon: Timer,
    color: '#FF7D00',
    trend: '待处理',
    trendType: 'warning',
  },
])

const initCharts = () => {
  if (annotationChartRef.value) {
    const annotationChart = echarts.init(annotationChartRef.value)
    annotationChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, textStyle: { color: '#1D2129' } },
      legend: { 
        data: ['已标注', '待标注', '已审核'], 
        textStyle: { color: '#4E5969', fontSize: 12 },
        top: 5,
        left: 'center'
      },
      grid: { left: '5%', right: '5%', bottom: '12%', top: '15%', containLabel: true },
      xAxis: {
        type: 'category',
        data: ['哺乳动物', '鸟类', '爬行动物', '两栖动物', '鱼类', '昆虫'],
        axisLabel: { color: '#4E5969', fontSize: 11 },
        axisLine: { lineStyle: { color: '#E5E6EB' } },
        axisTick: { show: false }
      },
      yAxis: { 
        type: 'value', 
        axisLabel: { color: '#4E5969', fontSize: 11 },
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: { lineStyle: { color: '#E5E6EB', type: 'dashed' } }
      },
      series: [
        { name: '已标注', type: 'bar', data: [120, 85, 45, 30, 60, 95], itemStyle: { color: '#165DFF', borderRadius: [4, 4, 0, 0] } },
        { name: '待标注', type: 'bar', data: [30, 25, 15, 10, 20, 35], itemStyle: { color: '#C9CDD4', borderRadius: [4, 4, 0, 0] } },
        { name: '已审核', type: 'bar', data: [95, 65, 30, 20, 45, 70], itemStyle: { color: '#00B42A', borderRadius: [4, 4, 0, 0] } },
      ],
    })
    window.addEventListener('resize', () => annotationChart.resize())
  }

  if (trainingChartRef.value) {
    const trainingChart = echarts.init(trainingChartRef.value)
    trainingChart.setOption({
      tooltip: { 
        trigger: 'axis', 
        textStyle: { color: '#1D2129' },
        backgroundColor: 'rgba(255, 255, 255, 0.98)',
        borderColor: '#E5E6EB',
        borderWidth: 1
      },
      legend: { 
        data: ['准确率', 'F1分数'], 
        textStyle: { color: '#4E5969', fontSize: 12 },
        top: 5,
        left: 'center'
      },
      grid: { left: '5%', right: '5%', bottom: '12%', top: '15%', containLabel: true },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: ['第1轮', '第2轮', '第3轮', '第4轮', '第5轮', '第6轮', '第7轮', '第8轮'],
        axisLabel: { color: '#4E5969', fontSize: 11 },
        axisLine: { lineStyle: { color: '#E5E6EB' } },
        axisTick: { show: false }
      },
      yAxis: { 
        type: 'value', 
        max: 1, 
        axisLabel: { color: '#4E5969', fontSize: 11, formatter: '{value}' },
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: { lineStyle: { color: '#E5E6EB', type: 'dashed' } }
      },
      series: [
        { 
          name: '准确率', 
          type: 'line', 
          data: [0.62, 0.71, 0.76, 0.8, 0.83, 0.85, 0.87, 0.89], 
          smooth: true, 
          itemStyle: { color: '#165DFF' }, 
          lineStyle: { width: 3 },
          areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(22, 93, 255, 0.3)' }, { offset: 1, color: 'rgba(22, 93, 255, 0.05)' }] } },
          symbol: 'circle',
          symbolSize: 6,
          emphasis: { scale: true, itemStyle: { borderWidth: 2 } }
        },
        { 
          name: 'F1分数', 
          type: 'line', 
          data: [0.58, 0.67, 0.72, 0.77, 0.8, 0.82, 0.84, 0.86], 
          smooth: true, 
          itemStyle: { color: '#00B42A' }, 
          lineStyle: { width: 3 },
          areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(0, 180, 42, 0.3)' }, { offset: 1, color: 'rgba(0, 180, 42, 0.05)' }] } },
          symbol: 'circle',
          symbolSize: 6,
          emphasis: { scale: true, itemStyle: { borderWidth: 2 } }
        },
      ],
    })
    window.addEventListener('resize', () => trainingChart.resize())
  }
}

const loadStatistics = async () => {
  loading.value = true
  try {
    const res = await getOverview()
    if (res.data && res.data.data) {
      stats.value = res.data.data
    } else {
      stats.value = {}
    }
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '获取统计数据失败')
    stats.value = {}
  } finally {
    loading.value = false
  }
}

const loadRecentTasks = async () => {
  try {
    const res = await getAllTasks()
    if (res.data && res.data.data) {
      tasks.value = res.data.data.slice(0, 5)
    } else {
      tasks.value = []
    }
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '获取任务列表失败')
    tasks.value = []
  }
}

const loadDatasets = async () => {
  try {
    const res = await getAllDatasets()
    if (res.data && res.data.data) {
      datasets.value = res.data.data.slice(0, 5)
    } else {
      datasets.value = []
    }
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '获取数据集列表失败')
    datasets.value = []
  }
}

onMounted(() => {
  loadStatistics()
  loadRecentTasks()
  loadDatasets()
  nextTick(() => {
    initCharts()
  })
})
</script>

<style scoped>
.chart-container {
  height: 300px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.status-dot--success { background-color: var(--color-status-success); }
.status-dot--warning { background-color: var(--color-status-warning); }
.status-dot--danger { background-color: var(--color-status-danger); }
</style>
