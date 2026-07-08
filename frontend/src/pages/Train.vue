<template>
  <div>
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">训练任务管理</h1>
      </div>
      <div class="page-header-right">
        <el-button type="primary" icon="Plus" @click="handleAdd"
          >创建训练任务</el-button
        >
      </div>
    </div>

    <div class="table-container">
      <el-table :data="tasks" row-key="taskId" :loading="loading" size="small">
        <el-table-column
          prop="taskName"
          label="任务名称"
          show-overflow-tooltip
        />
        <el-table-column prop="datasetId" label="数据集" width="120">
          <template #default="scope">
            {{
              datasets.find((d) => d.datasetId === scope.row.datasetId)
                ?.datasetName || scope.row.datasetId
            }}
          </template>
        </el-table-column>
        <el-table-column prop="modelType" label="模型类型" width="120" />
        <el-table-column
          prop="enableHierarchicalLoss"
          label="层级损失"
          width="100"
        >
          <template #default="scope">
            {{ scope.row.enableHierarchicalLoss ? "启用" : "禁用" }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag
              :type="statusConfig[scope.row.status]?.color || 'primary'"
              >{{
                statusConfig[scope.row.status]?.text || scope.row.status
              }}</el-tag
            >
          </template>
        </el-table-column>
        <el-table-column prop="currentEpoch" label="当前轮次" width="100">
          <template #default="scope">
            <span class="number-font">{{
              scope.row.currentEpoch != null
                ? `${scope.row.currentEpoch}轮`
                : "-"
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="bestValMetric" label="最佳指标" width="100">
          <template #default="scope">
            <span class="number-font">{{
              scope.row.bestValMetric != null
                ? `${(scope.row.bestValMetric * 100).toFixed(1)}%`
                : "-"
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="150">
          <template #default="scope">
            {{ dayjs(scope.row.createTime).format("YYYY-MM-DD HH:mm") }}
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="150">
          <template #default="scope">
            {{
              scope.row.startTime
                ? dayjs(scope.row.startTime).format("YYYY-MM-DD HH:mm")
                : "-"
            }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="150">
          <template #default="scope">
            {{
              scope.row.endTime
                ? dayjs(scope.row.endTime).format("YYYY-MM-DD HH:mm")
                : "-"
            }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 'pending'"
              type="text"
              @click="handleStart(scope.row.taskId)"
              >启动</el-button
            >
            <el-button v-if="scope.row.status === 'running'" type="text" loading
              >训练中</el-button
            >
            <el-button
              v-if="scope.row.status === 'success'"
              type="text"
              @click="handleViewEvaluation(scope.row.taskId)"
              >查看评估</el-button
            >
            <el-button
              v-if="scope.row.status !== 'running'"
              type="text"
              @click="handleDelete(scope.row.taskId)"
              style="color: #f53f3f"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog title="创建训练任务" v-model="modalVisible" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="form.taskName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="数据集" prop="datasetId">
          <el-select
            v-model="form.datasetId"
            placeholder="选择数据集"
            teleported
          >
            <el-option
              v-for="d in datasets"
              :key="d.datasetId"
              :label="d.datasetName"
              :value="d.datasetId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="模型类型" prop="modelType">
          <el-select
            v-model="form.modelType"
            placeholder="选择模型类型"
            teleported
          >
            <el-option label="KNN动物声纹分类模型" value="KNN" />
          </el-select>
        </el-form-item>
        <el-form-item label="距离度量" prop="distanceMetric">
          <el-select
            v-model="form.distanceMetric"
            placeholder="选择距离度量"
            teleported
          >
            <el-option label="欧氏距离" value="euclidean" />
            <el-option label="余弦距离" value="cosine" />
          </el-select>
          <span style="margin-left: 8px; color: #86909c; font-size: 12px"
            >计算样本间距离的方法</span
          >
        </el-form-item>
        <el-form-item label="距离加权投票">
          <el-switch v-model="form.useDistanceWeighting" />
          <span style="margin-left: 8px; color: #86909c; font-size: 12px"
            >启用后，距离越近的邻居权重越大</span
          >
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">提交</el-button>
          <el-button @click="modalVisible = false" style="margin-left: 8px"
            >取消</el-button
          >
        </el-form-item>
      </el-form>
    </el-dialog>

    <el-dialog title="模型评估结果" v-model="evaluationVisible" width="800px">
      <div v-if="evaluation" class="evaluation-content">
        <div class="evaluation-cards">
          <div class="eval-card">
            <div class="eval-label">准确率 (Accuracy)</div>
            <div class="eval-value">
              {{ (evaluation.accuracy * 100).toFixed(2) }}%
            </div>
          </div>
          <div class="eval-card">
            <div class="eval-label">精确率 (Precision)</div>
            <div class="eval-value">
              {{ (evaluation.precision * 100).toFixed(2) }}%
            </div>
          </div>
          <div class="eval-card">
            <div class="eval-label">召回率 (Recall)</div>
            <div class="eval-value">
              {{ (evaluation.recall * 100).toFixed(2) }}%
            </div>
          </div>
          <div class="eval-card">
            <div class="eval-label">F1分数 (F1-Score)</div>
            <div class="eval-value">
              {{ (evaluation.f1Score * 100).toFixed(2) }}%
            </div>
          </div>
          <div class="eval-card">
            <div class="eval-label">宏平均F1 (Macro-F1)</div>
            <div class="eval-value">
              {{ (evaluation.macroF1 * 100).toFixed(2) }}%
            </div>
          </div>
          <div class="eval-card">
            <div class="eval-label">微平均F1 (Micro-F1)</div>
            <div class="eval-value">
              {{ (evaluation.microF1 * 100).toFixed(2) }}%
            </div>
          </div>
        </div>

        <div class="evaluation-info">
          <div class="info-row">
            <span class="info-label">模型类型:</span>
            <span class="info-value">{{ evaluation.modelType }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">评估样本数:</span>
            <span class="info-value">{{ evaluation.sampleCount }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">类别数量:</span>
            <span class="info-value">{{ evaluation.classCount }}</span>
          </div>
        </div>

        <div v-if="evaluation.classificationReport" class="report-section">
          <h3>分类报告</h3>
          <pre class="report-pre">{{ evaluation.classificationReport }}</pre>
        </div>
      </div>
      <div v-else>
        <el-empty description="暂无评估数据" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  getAllTasks,
  createTask,
  startTask,
  deleteTask,
  getEvaluationByTaskId,
} from "../api/train";
import { getAllDatasets } from "../api/dataset";
import dayjs from "dayjs";

const tasks = ref([]);
const loading = ref(false);
const datasets = ref([]);
const modalVisible = ref(false);
const evaluationVisible = ref(false);
const evaluation = ref(null);
const formRef = ref(null);
let intervalId = null;

const form = reactive({
  taskName: "",
  datasetId: "",
  modelType: "",
  enableHierarchicalLoss: false,
  distanceMetric: "euclidean",
  useDistanceWeighting: true,
});

const rules = {
  taskName: [{ required: true, message: "请输入任务名称", trigger: "blur" }],
  datasetId: [{ required: true, message: "请选择数据集", trigger: "change" }],
  modelType: [{ required: true, message: "请选择模型类型", trigger: "change" }],
};

const statusConfig = {
  pending: { color: "primary", text: "等待中" },
  running: { color: "warning", text: "训练中" },
  success: { color: "success", text: "已完成" },
  failed: { color: "danger", text: "失败" },
};

const loadTasks = async () => {
  loading.value = true;
  try {
    const res = await getAllTasks();
    tasks.value = res.data.data;
  } finally {
    loading.value = false;
  }
};

const loadDatasets = async () => {
  const res = await getAllDatasets();
  datasets.value = res.data.data;
};

const handleAdd = () => {
  form.taskName = "";
  form.datasetId = "";
  form.modelType = "";
  form.enableHierarchicalLoss = false;
  form.distanceMetric = "euclidean";
  form.useDistanceWeighting = true;
  modalVisible.value = true;
};

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;

  const requestData = {
    taskName: form.taskName,
    datasetId: form.datasetId,
    modelType: form.modelType,
    enableHierarchicalLoss: form.enableHierarchicalLoss ? 1 : 0,
    trainParams: {
      distanceMetric: form.distanceMetric,
      useDistanceWeighting: form.useDistanceWeighting,
    },
  };

  try {
    await createTask(requestData);
    ElMessage.success("训练任务创建成功");
    modalVisible.value = false;
    loadTasks();
  } catch (err) {
    ElMessage.error(err.response?.data?.message || "创建失败");
  }
};

const handleStart = async (id) => {
  try {
    await startTask(id);
    ElMessage.success("训练任务已启动");
    loadTasks();
  } catch (err) {
    ElMessage.error(err.response?.data?.message || "启动失败");
  }
};

const handleViewEvaluation = async (taskId) => {
  try {
    const res = await getEvaluationByTaskId(taskId);
    if (res.data && res.data.data) {
      evaluation.value = res.data.data;
    } else {
      evaluation.value = null;
    }
    evaluationVisible.value = true;
  } catch (err) {
    ElMessage.error("获取评估数据失败");
    evaluation.value = null;
  }
};

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm("确定删除？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });
    await deleteTask(id);
    ElMessage.success("删除成功");
    loadTasks();
  } catch (err) {
    if (err !== "cancel") {
      ElMessage.error(err.response?.data?.message || "删除失败");
    }
  }
};

onMounted(() => {
  loadTasks();
  loadDatasets();

  intervalId = setInterval(() => {
    const runningTasks = tasks.value.filter((t) => t.status === "running");
    if (runningTasks.length > 0) {
      loadTasks();
    }
  }, 3000);
});

onUnmounted(() => {
  if (intervalId) {
    clearInterval(intervalId);
  }
});
</script>

<style scoped>
.evaluation-content {
  padding: 10px;
}

.evaluation-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.eval-card {
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  border-radius: 8px;
  padding: 12px;
  text-align: center;
}

.eval-label {
  font-size: 12px;
  color: #86909c;
  margin-bottom: 4px;
}

.eval-value {
  font-size: 20px;
  font-weight: bold;
  color: #10b981;
}

.evaluation-info {
  background: #0f172a;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  border-bottom: 1px solid #1e293b;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  color: #86909c;
  font-size: 13px;
}

.info-value {
  color: #e5e6eb;
  font-weight: 500;
}

.report-section {
  background: #0f172a;
  border-radius: 8px;
  padding: 12px;
}

.report-section h3 {
  color: #e5e6eb;
  font-size: 14px;
  margin-bottom: 10px;
}

.report-pre {
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
  color: #94a3b8;
  background: #1e293b;
  padding: 10px;
  border-radius: 4px;
  max-height: 300px;
  overflow-y: auto;
}
</style>
