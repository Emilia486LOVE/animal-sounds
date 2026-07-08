<template>
  <div>
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">数据集管理</h1>
      </div>
      <div class="page-header-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索数据集"
          style="width: 280px"
          prefix-icon="Search"
        />
        <el-button type="primary" icon="Plus" @click="handleAdd"
          >创建数据集</el-button
        >
      </div>
    </div>

    <div class="table-container">
      <el-table
        :data="filteredDatasets"
        row-key="datasetId"
        :loading="loading"
        size="small"
      >
        <el-table-column
          prop="datasetName"
          label="数据集名称"
          show-overflow-tooltip
        />
        <el-table-column
          prop="description"
          label="描述"
          show-overflow-tooltip
        />
        <el-table-column prop="audioCount" label="音频数量" width="100">
          <template #default="scope">
            <span class="number-font">{{ scope.row.audioCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="150">
          <template #default="scope">
            {{ dayjs(scope.row.createTime).format("YYYY-MM-DD HH:mm") }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="handleManageAudio(scope.row)"
              >管理音频</el-button
            >
            <el-button link type="primary" @click="handleEdit(scope.row)"
              >编辑</el-button
            >
            <el-button
              link
              type="warning"
              @click="handleRefreshCount(scope.row)"
              >刷新数量</el-button
            >
            <el-button
              link
              type="danger"
              @click="handleDelete(scope.row.datasetId)"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 创建/编辑数据集对话框 -->
    <el-dialog
      :title="isEdit ? '编辑数据集' : '创建数据集'"
      v-model="modalVisible"
      width="500px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="数据集名称" prop="datasetName">
          <el-input v-model="form.datasetName" placeholder="请输入数据集名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入数据集描述"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">提交</el-button>
          <el-button @click="modalVisible = false" style="margin-left: 8px"
            >取消</el-button
          >
        </el-form-item>
      </el-form>
    </el-dialog>

    <!-- 音频管理对话框 -->
    <el-dialog
      :title="`音频管理 - ${currentDataset?.datasetName || ''}`"
      v-model="audioModalVisible"
      width="900px"
      top="5vh"
    >
      <div
        style="
          margin-bottom: 12px;
          display: flex;
          justify-content: space-between;
          align-items: center;
        "
      >
        <div>
          <span style="margin-right: 16px"
            >共 <strong>{{ datasetAudioList.length }}</strong> 个音频文件</span
          >
          <el-button
            size="small"
            type="success"
            @click="showUploadDialog = true"
            >上传音频</el-button
          >
        </div>
        <div>
          <el-button
            size="small"
            type="warning"
            :disabled="selectedAudioIds.length === 0"
            @click="showBatchMoveDialog = true"
          >
            批量转移 ({{ selectedAudioIds.length }})
          </el-button>
          <el-button
            size="small"
            type="danger"
            :disabled="selectedAudioIds.length === 0"
            @click="handleBatchDelete"
          >
            批量删除 ({{ selectedAudioIds.length }})
          </el-button>
        </div>
      </div>

      <el-table
        :data="datasetAudioList"
        :loading="audioLoading"
        size="small"
        @selection-change="handleSelectionChange"
        max-height="400"
      >
        <el-table-column type="selection" width="40" />
        <el-table-column prop="fileName" label="文件名" show-overflow-tooltip />
        <el-table-column prop="duration" label="时长(秒)" width="90">
          <template #default="scope">
            {{
              scope.row.duration ? Number(scope.row.duration).toFixed(1) : "-"
            }}
          </template>
        </el-table-column>
        <el-table-column prop="sampleRate" label="采样率" width="90">
          <template #default="scope">
            {{ scope.row.sampleRate || "-" }}
          </template>
        </el-table-column>
        <el-table-column prop="noiseLevel" label="噪声等级" width="90" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="handleMoveSingle(scope.row)"
              >转移</el-button
            >
            <el-button
              link
              type="danger"
              @click="handleDeleteSingle(scope.row.audioId)"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 上传音频对话框 -->
    <el-dialog
      title="上传音频文件"
      v-model="showUploadDialog"
      width="500px"
      append-to-body
    >
      <el-upload
        ref="uploadRef"
        :action="`${apiBaseUrl}/audio/upload`"
        :data="{ datasetId: currentDataset?.datasetId || '' }"
        :headers="uploadHeaders"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :before-upload="beforeUpload"
        multiple
        :auto-upload="false"
        accept=".wav,.mp3,.flac,.ogg"
      >
        <el-button type="primary">选择音频文件</el-button>
        <template #tip>
          <div style="color: #86909c; font-size: 12px; margin-top: 8px">
            支持 WAV、MP3、FLAC、OGG 格式，可多选
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="showUploadDialog = false">取消</el-button>
        <el-button
          type="primary"
          @click="confirmUpload"
          :disabled="!currentDataset?.datasetId"
          >开始上传</el-button
        >
      </template>
    </el-dialog>

    <!-- 转移到数据集对话框 -->
    <el-dialog
      title="选择目标数据集"
      v-model="showMoveDialog"
      width="400px"
      append-to-body
    >
      <el-select
        v-model="targetDatasetId"
        placeholder="选择目标数据集"
        style="width: 100%"
      >
        <el-option
          v-for="d in moveableDatasets"
          :key="d.datasetId"
          :label="d.datasetName"
          :value="d.datasetId"
        />
      </el-select>
      <template #footer>
        <el-button @click="showMoveDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmMove">确认转移</el-button>
      </template>
    </el-dialog>

    <!-- 批量转移到数据集对话框 -->
    <el-dialog
      title="批量转移到数据集"
      v-model="showBatchMoveDialog"
      width="400px"
      append-to-body
    >
      <p style="margin-bottom: 12px; color: #86909c">
        将选中的 {{ selectedAudioIds.length }} 个音频文件转移到：
      </p>
      <el-select
        v-model="targetDatasetId"
        placeholder="选择目标数据集"
        style="width: 100%"
      >
        <el-option
          v-for="d in moveableDatasets"
          :key="d.datasetId"
          :label="d.datasetName"
          :value="d.datasetId"
        />
      </el-select>
      <template #footer>
        <el-button @click="showBatchMoveDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmBatchMove">确认转移</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  getAllDatasets,
  createDataset,
  updateDataset,
  deleteDataset,
  refreshAudioCount,
} from "../api/dataset";
import {
  getAudioFilesByDataset,
  deleteAudioFile,
  moveAudioToDataset,
  batchMoveAudio,
  batchDeleteAudio,
} from "../api/audio";
import dayjs from "dayjs";

const apiBaseUrl = import.meta.env.VITE_API_URL || "/api";

const datasets = ref([]);
const loading = ref(false);
const modalVisible = ref(false);
const isEdit = ref(false);
const formRef = ref(null);
const editingId = ref(null);
const searchKeyword = ref("");

const form = reactive({
  datasetName: "",
  description: "",
});

const rules = {
  datasetName: [
    { required: true, message: "请输入数据集名称", trigger: "blur" },
  ],
};

const filteredDatasets = computed(() => {
  return datasets.value.filter(
    (d) =>
      d.datasetName.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      (d.description &&
        d.description
          .toLowerCase()
          .includes(searchKeyword.value.toLowerCase())),
  );
});

const loadDatasets = async () => {
  loading.value = true;
  try {
    const res = await getAllDatasets();
    if (res.data && res.data.data) {
      datasets.value = res.data.data;
    } else {
      ElMessage.error("获取数据失败");
    }
  } catch (err) {
    ElMessage.error(err.response?.data?.message || "获取数据集失败");
  } finally {
    loading.value = false;
  }
};

const handleAdd = () => {
  isEdit.value = false;
  editingId.value = null;
  form.datasetName = "";
  form.description = "";
  modalVisible.value = true;
};

const handleEdit = async (record) => {
  isEdit.value = true;
  editingId.value = record.datasetId;
  await nextTick();
  form.datasetName = record.datasetName;
  form.description = record.description || "";
  await nextTick();
  modalVisible.value = true;
};

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm(
      "删除数据集不会删除关联的音频文件，但音频将不再关联任何数据集。确认删除？",
      "提示",
      {
        type: "warning",
      },
    );
    await deleteDataset(id);
    ElMessage.success("删除成功");
    loadDatasets();
  } catch (err) {
    if (err !== "cancel") {
      ElMessage.error(err.response?.data?.message || "删除失败");
    }
  }
};

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;

  try {
    if (isEdit.value) {
      await updateDataset(editingId.value, form);
      ElMessage.success("更新成功");
    } else {
      await createDataset(form);
      ElMessage.success("创建成功");
    }
    modalVisible.value = false;
    loadDatasets();
  } catch (err) {
    ElMessage.error(
      err.response?.data?.message || (isEdit.value ? "更新失败" : "创建失败"),
    );
  }
};

// ===== 音频管理功能 =====
const audioModalVisible = ref(false);
const audioLoading = ref(false);
const currentDataset = ref(null);
const datasetAudioList = ref([]);
const selectedAudioIds = ref([]);

// 上传相关
const showUploadDialog = ref(false);
const uploadRef = ref(null);
const uploadHeaders = computed(() => {
  const token = localStorage.getItem("token");
  return token ? { Authorization: `Bearer ${token}` } : {};
});

// 转移相关
const showMoveDialog = ref(false);
const showBatchMoveDialog = ref(false);
const targetDatasetId = ref(null);
const movingAudioId = ref(null);
const moveableDatasets = computed(() => {
  if (!currentDataset.value) return datasets.value;
  return datasets.value.filter(
    (d) => d.datasetId !== currentDataset.value.datasetId,
  );
});

const handleManageAudio = async (record) => {
  currentDataset.value = record;
  audioModalVisible.value = true;
  await loadDatasetAudio(record.datasetId);
};

const loadDatasetAudio = async (datasetId) => {
  audioLoading.value = true;
  try {
    const res = await getAudioFilesByDataset(datasetId);
    if (res.data && res.data.data) {
      datasetAudioList.value = res.data.data;
    } else {
      datasetAudioList.value = [];
    }
  } catch (err) {
    ElMessage.error("获取音频列表失败");
    datasetAudioList.value = [];
  } finally {
    audioLoading.value = false;
  }
};

const handleSelectionChange = (selection) => {
  selectedAudioIds.value = selection.map((item) => item.audioId);
};

const beforeUpload = (file) => {
  const validExts = ["wav", "mp3", "flac", "ogg"];
  const ext = file.name.split(".").pop().toLowerCase();
  if (!validExts.includes(ext)) {
    ElMessage.error("不支持的文件格式");
    return false;
  }
  return true;
};

const confirmUpload = () => {
  if (!uploadRef.value) return;
  uploadRef.value.submit();
};

const handleUploadSuccess = () => {
  ElMessage.success("上传成功");
  showUploadDialog.value = false;
  loadDatasetAudio(currentDataset.value.datasetId);
  loadDatasets();
};

const handleUploadError = () => {
  ElMessage.error("上传失败");
};

const handleMoveSingle = (row) => {
  movingAudioId.value = row.audioId;
  targetDatasetId.value = null;
  showMoveDialog.value = true;
};

const confirmMove = async () => {
  if (!targetDatasetId.value) {
    ElMessage.warning("请选择目标数据集");
    return;
  }
  try {
    await moveAudioToDataset(movingAudioId.value, targetDatasetId.value);
    ElMessage.success("转移成功");
    showMoveDialog.value = false;
    loadDatasetAudio(currentDataset.value.datasetId);
    loadDatasets();
  } catch (err) {
    ElMessage.error(err.response?.data?.message || "转移失败");
  }
};

const confirmBatchMove = async () => {
  if (!targetDatasetId.value) {
    ElMessage.warning("请选择目标数据集");
    return;
  }
  try {
    await batchMoveAudio(selectedAudioIds.value, targetDatasetId.value);
    ElMessage.success("批量转移成功");
    showBatchMoveDialog.value = false;
    selectedAudioIds.value = [];
    loadDatasetAudio(currentDataset.value.datasetId);
    loadDatasets();
  } catch (err) {
    ElMessage.error(err.response?.data?.message || "批量转移失败");
  }
};

const handleDeleteSingle = async (audioId) => {
  try {
    await ElMessageBox.confirm("确认删除该音频文件？删除后不可恢复。", "提示", {
      type: "warning",
    });
    await deleteAudioFile(audioId);
    ElMessage.success("删除成功");
    loadDatasetAudio(currentDataset.value.datasetId);
    loadDatasets();
  } catch (err) {
    if (err !== "cancel") {
      ElMessage.error(err.response?.data?.message || "删除失败");
    }
  }
};

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确认删除选中的 ${selectedAudioIds.value.length} 个音频文件？`,
      "提示",
      { type: "warning" },
    );
    await batchDeleteAudio(selectedAudioIds.value);
    ElMessage.success("批量删除成功");
    selectedAudioIds.value = [];
    loadDatasetAudio(currentDataset.value.datasetId);
    loadDatasets();
  } catch (err) {
    if (err !== "cancel") {
      ElMessage.error(err.response?.data?.message || "批量删除失败");
    }
  }
};

const handleRefreshCount = async (record) => {
  try {
    await refreshAudioCount(record.datasetId);
    ElMessage.success("音频数量已刷新");
    loadDatasets();
  } catch (err) {
    ElMessage.error(err.response?.data?.message || "刷新失败");
  }
};

onMounted(() => {
  loadDatasets();
});
</script>
