<template>
  <div>
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">模型评估结果</h1>
      </div>
      <div class="page-header-right">
        <el-select
          v-model="selectedTask"
          placeholder="选择训练任务"
          style="width: 280px"
          @change="handleTaskChange"
        >
          <el-option v-for="t in tasks" :key="t.taskId" :label="`${t.taskName} (${dayjs(t.endTime).format('MM-DD HH:mm')})`" :value="t.taskId" />
        </el-select>
      </div>
    </div>

    <el-card v-if="selectedTaskInfo" class="mb-lg">
      <div class="flex-between">
        <div>
          <h3 class="card-title">{{ selectedTaskInfo.taskName }}</h3>
          <p class="text-sm text-secondary">模型类型: {{ selectedTaskInfo.modelType }} | 完成时间: {{ dayjs(selectedTaskInfo.endTime).format('YYYY-MM-DD HH:mm') }}</p>
        </div>
        <el-tag type="success">评估完成</el-tag>
      </div>
    </el-card>

    <el-row v-if="summary" :gutter="[16, 16]" class="mb-lg">
      <el-col :span="6">
        <div class="stat-card stat-card--primary">
          <div class="stat-card__label">平均准确率</div>
          <div class="stat-card__value" style="color: var(--color-brand-primary)">
            {{ ((summary.avgAccuracy || 0) * 100).toFixed(2) }}%
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-card--success">
          <div class="stat-card__label">平均精确率</div>
          <div class="stat-card__value" style="color: var(--color-status-success)">
            {{ ((summary.avgPrecision || 0) * 100).toFixed(2) }}%
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-card--warning">
          <div class="stat-card__label">平均召回率</div>
          <div class="stat-card__value" style="color: var(--color-status-warning)">
            {{ ((summary.avgRecall || 0) * 100).toFixed(2) }}%
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-card--purple">
          <div class="stat-card__label">平均F1分数</div>
          <div class="stat-card__value" style="color: #722ED1">
            {{ ((summary.avgF1Score || 0) * 100).toFixed(2) }}%
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="[16, 16]">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span class="card-title">准确率柱状图</span>
          </template>
          <div ref="accuracyChartRef" class="chart-container" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span class="card-title">F1分数折线图</span>
          </template>
          <div ref="f1ChartRef" class="chart-container" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="[16, 16]" class="mt-lg">
      <el-col :span="24">
        <el-card class="chart-card">
          <template #header>
            <span class="card-title">各层级评估指标对比</span>
          </template>
          <div ref="metricsChartRef" class="chart-container-large" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="[16, 16]" class="mt-lg">
      <el-col :span="24">
        <el-card class="table-card">
          <template #header>
            <span class="card-title">详细评估数据</span>
          </template>
          <el-table
            :data="evaluations"
            row-key="evalId"
            :loading="loading"
            size="small"
          >
            <el-table-column prop="taxonRank" label="分类层级" width="100">
              <template #default="scope">
                {{ rankNames[scope.row.taxonRank] || scope.row.taxonRank }}
              </template>
            </el-table-column>
            <el-table-column prop="accuracy" label="准确率" width="120">
              <template #default="scope">
                <span class="number-font">{{ scope.row.accuracy != null ? `${(scope.row.accuracy * 100).toFixed(2)}%` : '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="precision" label="精确率" width="120">
              <template #default="scope">
                <span class="number-font">{{ scope.row.precision != null ? `${(scope.row.precision * 100).toFixed(2)}%` : '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="recall" label="召回率" width="120">
              <template #default="scope">
                <span class="number-font">{{ scope.row.recall != null ? `${(scope.row.recall * 100).toFixed(2)}%` : '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="f1Score" label="F1分数" width="120">
              <template #default="scope">
                <span class="number-font">{{ scope.row.f1Score != null ? `${(scope.row.f1Score * 100).toFixed(2)}%` : '-' }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getAllTasks } from '../api/train'
import { getEvaluationsByTaskId, getTaskEvaluationSummary } from '../api/evaluation'
import dayjs from 'dayjs'

const tasks = ref([])
const selectedTask = ref(null)
const evaluations = ref([])
const summary = ref(null)
const loading = ref(false)
const accuracyChartRef = ref(null)
const f1ChartRef = ref(null)
const metricsChartRef = ref(null)

let accuracyChart = null
let f1Chart = null
let metricsChart = null

const rankNames = {
  kingdom: '界',
  phylum: '门',
  class: '纲',
  order: '目',
  family: '科',
  genus: '属',
  species: '种'
}

const selectedTaskInfo = computed(() => {
  return tasks.value.find(t => t.taskId === selectedTask.value)
})

const initCharts = () => {
  const xAxisData = evaluations.value.map(e => rankNames[e.taxonRank] || e.taxonRank)

  if (accuracyChartRef.value) {
    if (accuracyChart) accuracyChart.dispose()
    accuracyChart = echarts.init(accuracyChartRef.value)
    accuracyChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: xAxisData, axisLabel: { color: '#4E5969' } },
      yAxis: { type: 'value', max: 1, axisLabel: { color: '#4E5969' } },
      series: [
        { name: '准确率', type: 'bar', data: evaluations.value.map(e => e.accuracy), itemStyle: { color: '#165DFF' } }
      ]
    })
    window.addEventListener('resize', () => accuracyChart.resize())
  }

  if (f1ChartRef.value) {
    if (f1Chart) f1Chart.dispose()
    f1Chart = echarts.init(f1ChartRef.value)
    f1Chart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: xAxisData, axisLabel: { color: '#4E5969' } },
      yAxis: { type: 'value', max: 1, axisLabel: { color: '#4E5969' } },
      series: [
        { name: 'F1分数', type: 'line', data: evaluations.value.map(e => e.f1Score), smooth: true, itemStyle: { color: '#165DFF' }, lineStyle: { width: 3 } }
      ]
    })
    window.addEventListener('resize', () => f1Chart.resize())
  }

  if (metricsChartRef.value) {
    if (metricsChart) metricsChart.dispose()
    metricsChart = echarts.init(metricsChartRef.value)
    metricsChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['准确率', '精确率', '召回率', 'F1分数'], textStyle: { color: '#4E5969' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: xAxisData, axisLabel: { color: '#4E5969' } },
      yAxis: { type: 'value', max: 1, axisLabel: { color: '#4E5969' } },
      series: [
        { name: '准确率', type: 'line', data: evaluations.value.map(e => e.accuracy), smooth: true, itemStyle: { color: '#165DFF' } },
        { name: '精确率', type: 'line', data: evaluations.value.map(e => e.precision), smooth: true, itemStyle: { color: '#00B42A' } },
        { name: '召回率', type: 'line', data: evaluations.value.map(e => e.recall), smooth: true, itemStyle: { color: '#FF7D00' } },
        { name: 'F1分数', type: 'line', data: evaluations.value.map(e => e.f1Score), smooth: true, itemStyle: { color: '#722ED1' } }
      ]
    })
    window.addEventListener('resize', () => metricsChart.resize())
  }
}

const loadTasks = async () => {
  const res = await getAllTasks()
  const completedTasks = res.data.data.filter(t => t.status === 'success')
  tasks.value = completedTasks
}

const loadEvaluations = async (taskId) => {
  loading.value = true
  try {
    const [evalRes, summaryRes] = await Promise.all([
      getEvaluationsByTaskId(taskId),
      getTaskEvaluationSummary(taskId)
    ])
    evaluations.value = evalRes.data.data || []
    summary.value = summaryRes.data.data || null
  } catch (err) {
    ElMessage.error('加载评估数据失败')
  } finally {
    loading.value = false
    nextTick(() => {
      initCharts()
    })
  }
}

const handleTaskChange = (taskId) => {
  selectedTask.value = taskId
}

watch(selectedTask, (val) => {
  if (val) {
    loadEvaluations(val)
  }
})

onMounted(() => {
  loadTasks()
})
</script>
