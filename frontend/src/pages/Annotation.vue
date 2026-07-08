<template>
  <div>
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">标注工作台</h1>
      </div>
      <div class="page-header-right">
        <el-button type="primary" icon="Plus" @click="handleAdd"
          >创建标注</el-button
        >
      </div>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchText"
        placeholder="搜索标注ID或备注..."
        prefix-icon="Search"
        style="width: 320px"
      />
    </div>

    <div class="table-container">
      <el-table
        :data="filteredAnnotations"
        row-key="annotationId"
        :loading="loading"
        size="small"
      >
        <el-table-column prop="annotationId" label="标注ID" width="80" />
        <el-table-column prop="audioId" label="音频文件" width="150">
          <template #default="scope">
            {{
              audioFiles.find((a) => a.audioId === scope.row.audioId)
                ?.fileName || scope.row.audioId
            }}
          </template>
        </el-table-column>
        <el-table-column prop="labelId" label="标签" width="120">
          <template #default="scope">
            {{
              labels.find((l) => l.labelId === scope.row.labelId)?.labelName ||
              scope.row.labelId
            }}
          </template>
        </el-table-column>
        <el-table-column prop="soundType" label="声音类型" width="80">
          <template #default="scope">
            <el-tag type="primary">{{
              soundTypeLabels[scope.row.soundType] || scope.row.soundType
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="100">
          <template #default="scope">
            <span class="number-font"
              >{{ scope.row.startTime?.toFixed(2) }}s</span
            >
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="100">
          <template #default="scope">
            <span class="number-font"
              >{{ scope.row.endTime?.toFixed(2) }}s</span
            >
          </template>
        </el-table-column>
        <el-table-column prop="confidence" label="置信度" width="80">
          <template #default="scope">
            <span class="number-font">{{
              scope.row.confidence != null
                ? `${(scope.row.confidence * 100).toFixed(0)}%`
                : "-"
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="statusColors[scope.row.status] || 'primary'">{{
              statusLabels[scope.row.status] || scope.row.status
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="150">
          <template #default="scope">
            {{
              scope.row.createTime
                ? new Date(scope.row.createTime).toLocaleString("zh-CN")
                : "-"
            }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button type="text" @click="handleEdit(scope.row)"
              >编辑</el-button
            >
            <el-button
              v-if="scope.row.status === 'draft'"
              type="text"
              @click="handleSubmit(scope.row.annotationId)"
              >提交审核</el-button
            >
            <el-button
              v-if="scope.row.status === 'submitted'"
              type="text"
              @click="handleReview(scope.row.annotationId)"
              >审核</el-button
            >
            <el-button
              type="text"
              @click="handleDelete(scope.row.annotationId)"
              style="color: #f53f3f"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      :title="selectedAnnotation ? '编辑标注' : '创建标注'"
      v-model="modalVisible"
      width="600px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="音频文件" prop="audioId">
          <el-select v-model="form.audioId" placeholder="选择音频文件">
            <el-option
              v-for="a in audioFiles"
              :key="a.audioId"
              :label="a.fileName"
              :value="a.audioId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间(秒)" prop="startTime">
          <el-input
            v-model.number="form.startTime"
            type="number"
            placeholder="请输入开始时间"
          />
        </el-form-item>
        <el-form-item label="结束时间(秒)" prop="endTime">
          <el-input
            v-model.number="form.endTime"
            type="number"
            placeholder="请输入结束时间"
          />
        </el-form-item>
        <el-form-item label="标签" prop="labelId">
          <el-select v-model="form.labelId" placeholder="选择标签">
            <el-option
              v-for="l in labels"
              :key="l.labelId"
              :label="l.labelName"
              :value="l.labelId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="声音类型" prop="soundType">
          <el-select v-model="form.soundType" placeholder="选择声音类型">
            <el-option label="叫声" value="call" />
            <el-option label="吼声" value="roar" />
            <el-option label="鸟鸣" value="birdcall" />
            <el-option label="虫鸣" value="insect" />
            <el-option label="其他" value="other" />
          </el-select>
          <div style="margin-top: 8px; font-size: 12px; color: #86909c">
            <span style="margin-right: 16px"
              ><strong>叫声</strong>：狗汪汪、猫喵、牛哞等日常交流声</span
            >
            <span style="margin-right: 16px"
              ><strong>吼声</strong>：虎啸、狼嚎等响亮嘶吼</span
            >
            <span style="margin-right: 16px"
              ><strong>鸟鸣</strong>：麻雀啾啾、鹰叫等鸟类声音</span
            >
            <span><strong>虫鸣</strong>：蜜蜂嗡嗡、蝉鸣等昆虫声音</span>
          </div>
        </el-form-item>
        <el-form-item label="置信度(0-100)" prop="confidence">
          <el-input
            v-model.number="form.confidence"
            type="number"
            :min="0"
            :max="100"
            placeholder="请输入置信度"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit" @click="handleSubmit"
            >提交</el-button
          >
          <el-button @click="closeModal" style="marginleft: 8">取消</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>

    <el-dialog
      :title="`审核标注 - ID: ${reviewingAnnotation?.annotationId}`"
      v-model="reviewModalVisible"
      width="600px"
    >
      <div v-if="reviewingAnnotation" style="marginbottom: 16px">
        <div
          style="
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 12px;
            marginbottom: 16px;
          "
        >
          <div>
            <span style="color: #86909c; font-size: 12px">音频文件</span>
            <div style="color: #e5e6eb; margin-top: 4px">
              {{
                audioFiles.find(
                  (a) => a.audioId === reviewingAnnotation.audioId,
                )?.fileName || reviewingAnnotation.audioId
              }}
            </div>
          </div>
          <div>
            <span style="color: #86909c; font-size: 12px">标签</span>
            <div style="color: #e5e6eb; margin-top: 4px">
              {{
                labels.find((l) => l.labelId === reviewingAnnotation.labelId)
                  ?.labelName || reviewingAnnotation.labelId
              }}
            </div>
          </div>
          <div>
            <span style="color: #86909c; font-size: 12px">声音类型</span>
            <div style="color: #e5e6eb; margin-top: 4px">
              {{
                soundTypeLabels[reviewingAnnotation.soundType] ||
                reviewingAnnotation.soundType
              }}
            </div>
          </div>
          <div>
            <span style="color: #86909c; font-size: 12px">置信度</span>
            <div style="color: #e5e6eb; margin-top: 4px">
              {{
                reviewingAnnotation.confidence != null
                  ? `${reviewingAnnotation.confidence}%`
                  : "-"
              }}
            </div>
          </div>
          <div>
            <span style="color: #86909c; font-size: 12px">时间范围</span>
            <div style="color: #e5e6eb; margin-top: 4px">
              {{ reviewingAnnotation.startTime?.toFixed(2) }}s -
              {{ reviewingAnnotation.endTime?.toFixed(2) }}s
            </div>
          </div>
          <div>
            <span style="color: #86909c; font-size: 12px">标注备注</span>
            <div style="color: #e5e6eb; margin-top: 4px">
              {{ reviewingAnnotation.remark || "-" }}
            </div>
          </div>
        </div>

        <el-divider />

        <el-form-item label="审核结果">
          <el-radio-group v-model="reviewForm.status">
            <el-radio-button value="approved">通过</el-radio-button>
            <el-radio-button value="rejected">拒绝</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="审核备注">
          <el-input
            v-model="reviewForm.reviewRemark"
            type="textarea"
            :rows="3"
            placeholder="请输入审核意见（拒绝时必填）"
          />
        </el-form-item>
      </div>

      <template #footer>
        <el-button @click="reviewModalVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="submitReview"
          :disabled="!reviewForm.status"
          >提交审核</el-button
        >
      </template>
    </el-dialog>

    <el-dialog
      :title="selectedAudio?.fileName || '音频标注'"
      v-model="playModalVisible"
      width="800px"
    >
      <div
        style="
          display: &quot;flex&quot;;
          alignitems: &quot;center&quot;;
          gap: 12;
          marginbottom: 16;
        "
      >
        <el-button
          icon="ArrowLeft"
          @click="prevAudio"
          :disabled="audioFiles.length <= 1"
        />
        <span class="number-font" style="color: &quot;#86909C&quot;">
          {{ currentAudioIndex + 1 }} / {{ audioFiles.length }}
        </span>
        <el-button
          icon="ArrowRight"
          @click="nextAudio"
          :disabled="audioFiles.length <= 1"
        />
      </div>

      <div style="marginbottom: 16">
        <div
          ref="waveformRef"
          id="waveform"
          style="
            backgroundcolor: &quot;#121826&quot;;
            borderradius: 8;
            height: 150;
          "
        />
      </div>

      <div
        v-if="wavesurfer"
        style="
          display: &quot;flex&quot;;
          alignitems: &quot;center&quot;;
          gap: 12;
        "
      >
        <el-button
          type="primary"
          icon="VideoPlay"
          @click="wavesurfer.playPause()"
        >
          {{ wavesurfer.isPlaying() ? "暂停" : "播放" }}
        </el-button>
        <span class="number-font" style="color: &quot;#86909C&quot;">
          {{ wavesurfer.getCurrentTime().toFixed(2) }} /
          {{ wavesurfer.getDuration().toFixed(2) }} s
        </span>
      </div>

      <div
        v-if="selectedAudio"
        style="
          margintop: 16;
          paddingtop: 16;
          bordertop: &quot;1px solid #2A3344&quot;;
        "
      >
        <div
          style="
            display: &quot;grid&quot;;
            gridtemplatecolumns: &quot;repeat(3, 1fr)&quot;;
            gap: 12;
          "
        >
          <div>
            <span style="color: &quot;#86909C&quot;; fontsize: 12">采样率</span>
            <div
              class="number-font"
              style="color: &quot;#E5E6EB&quot;; margintop: 4"
            >
              {{ selectedAudio.sampleRate }} Hz
            </div>
          </div>
          <div>
            <span style="color: &quot;#86909C&quot;; fontsize: 12">时长</span>
            <div
              class="number-font"
              style="color: &quot;#E5E6EB&quot;; margintop: 4"
            >
              {{ selectedAudio.duration?.toFixed(2) }} s
            </div>
          </div>
          <div>
            <span style="color: &quot;#86909C&quot;; fontsize: 12"
              >噪声等级</span
            >
            <div style="color: &quot;#E5E6EB&quot;; margintop: 4">
              <el-tag :type="noiseLevelType(selectedAudio.noiseLevel)">
                {{ noiseLevelLabel(selectedAudio.noiseLevel) }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import WaveSurfer from "wavesurfer.js";
import {
  getAllAnnotations,
  createAnnotation,
  updateAnnotation,
  deleteAnnotation,
  submitAnnotation,
  reviewAnnotation,
} from "../api/annotation";
import { getAllAudioFiles } from "../api/audio";
import { getAllLabels } from "../api/label";

const annotations = ref([]);
const audioFiles = ref([]);
const labels = ref([]);
const loading = ref(false);
const modalVisible = ref(false);
const playModalVisible = ref(false);
const reviewModalVisible = ref(false);
const formRef = ref(null);
const selectedAnnotation = ref(null);
const reviewingAnnotation = ref(null);
const selectedAudio = ref(null);
const searchText = ref("");
const currentAudioIndex = ref(0);
const waveformRef = ref(null);
let wavesurfer = null;

const form = reactive({
  audioId: "",
  startTime: null,
  endTime: null,
  labelId: "",
  soundType: "",
  confidence: null,
  remark: "",
});

const reviewForm = reactive({
  status: "",
  reviewRemark: "",
});

const rules = {
  audioId: [{ required: true, message: "请选择音频文件", trigger: "change" }],
  startTime: [{ required: true, message: "请输入开始时间", trigger: "blur" }],
  endTime: [{ required: true, message: "请输入结束时间", trigger: "blur" }],
  labelId: [{ required: true, message: "请选择标签", trigger: "change" }],
};

const statusConfig = {
  submitted: { color: "warning", text: "待审核" },
  approved: { color: "success", text: "已通过" },
  rejected: { color: "danger", text: "已拒绝" },
  draft: { color: "primary", text: "草稿" },
};

const soundTypeLabels = {
  call: "叫声",
  roar: "吼声",
  birdcall: "鸟鸣",
  insect: "虫鸣",
  other: "其他",
};

const statusColors = {
  submitted: "warning",
  approved: "success",
  rejected: "danger",
  draft: "primary",
};

const statusLabels = {
  submitted: "待审核",
  approved: "已通过",
  rejected: "已拒绝",
  draft: "草稿",
};

const noiseLevelType = (level) => {
  const types = { high: "danger", medium: "warning", low: "success" };
  return types[level] || "primary";
};

const noiseLevelLabel = (level) => {
  const labels = { high: "高", medium: "中", low: "低" };
  return labels[level] || "未知";
};

const filteredAnnotations = computed(() => {
  return annotations.value.filter((item) => {
    return (
      searchText.value === "" ||
      item.annotationId.toString().includes(searchText.value) ||
      (item.remark &&
        item.remark.toLowerCase().includes(searchText.value.toLowerCase()))
    );
  });
});

const initWaveSurfer = () => {
  if (wavesurfer) {
    wavesurfer.destroy();
  }
  nextTick(() => {
    wavesurfer = WaveSurfer.create({
      container: "#waveform",
      waveColor: "#2A3344",
      progressColor: "#165DFF",
      cursorColor: "#165DFF",
      barWidth: 2,
      barGap: 3,
      barRadius: 3,
      responsive: true,
      height: 150,
    });
    if (selectedAudio.value) {
      const apiUrl = import.meta.env.VITE_API_URL || "/api";
      wavesurfer.load(
        `${apiUrl}/audio/download/by-id/${selectedAudio.value.audioId}`,
      );
    }
  });
};

const destroyWaveSurfer = () => {
  if (wavesurfer) {
    wavesurfer.destroy();
    wavesurfer = null;
  }
};

const loadAnnotations = async () => {
  loading.value = true;
  try {
    const res = await getAllAnnotations();
    annotations.value = res.data.data;
  } finally {
    loading.value = false;
  }
};

const loadAudioFiles = async () => {
  const res = await getAllAudioFiles();
  audioFiles.value = res.data.data;
};

const loadLabels = async () => {
  const res = await getAllLabels();
  labels.value = res.data.data;
};

const handleAdd = () => {
  form.audioId = "";
  form.startTime = null;
  form.endTime = null;
  form.labelId = "";
  form.soundType = "";
  form.confidence = null;
  form.remark = "";
  selectedAnnotation.value = null;
  modalVisible.value = true;
};

const handleEdit = (record) => {
  form.audioId = record.audioId;
  form.startTime = record.startTime;
  form.endTime = record.endTime;
  form.labelId = record.labelId;
  form.soundType = record.soundType || "";
  form.confidence = record.confidence;
  form.remark = record.remark || "";
  selectedAnnotation.value = record;
  modalVisible.value = true;
};

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm("确定删除？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });
    await deleteAnnotation(id);
    ElMessage.success("删除成功");
    loadAnnotations();
  } catch (err) {
    if (err !== "cancel") {
      ElMessage.error(err.response?.data?.message || "删除失败");
    }
  }
};

const handleSubmit = async () => {
  const valid = await formRef.value.validate();
  if (!valid) return;

  try {
    if (selectedAnnotation.value) {
      await updateAnnotation(selectedAnnotation.value.annotationId, form);
      ElMessage.success("更新成功");
    } else {
      await createAnnotation(form);
      ElMessage.success("创建成功");
    }
    closeModal();
    loadAnnotations();
  } catch (err) {
    ElMessage.error(
      err.response?.data?.message ||
        (selectedAnnotation.value ? "更新失败" : "创建失败"),
    );
  }
};

const handleReview = (id) => {
  reviewingAnnotation.value = annotations.value.find(
    (a) => a.annotationId === id,
  );
  reviewForm.status = "";
  reviewForm.reviewRemark = "";
  reviewModalVisible.value = true;
};

const submitReview = async () => {
  if (!reviewForm.status) {
    ElMessage.warning("请选择审核结果");
    return;
  }

  if (reviewForm.status === "rejected" && !reviewForm.reviewRemark) {
    ElMessage.warning("拒绝时请填写审核备注");
    return;
  }

  try {
    await reviewAnnotation(reviewingAnnotation.value.annotationId, {
      status: reviewForm.status,
      reviewRemark: reviewForm.reviewRemark,
    });
    ElMessage.success(
      reviewForm.status === "approved" ? "审核通过" : "审核拒绝",
    );
    reviewModalVisible.value = false;
    reviewingAnnotation.value = null;
    loadAnnotations();
  } catch (err) {
    ElMessage.error(err.response?.data?.message || "审核失败");
  }
};

const handlePlay = (audioId) => {
  const audio = audioFiles.value.find((a) => a.audioId === audioId);
  if (audio) {
    const index = audioFiles.value.findIndex((a) => a.audioId === audioId);
    currentAudioIndex.value = index;
    selectedAudio.value = audio;
    playModalVisible.value = true;
  }
};

const prevAudio = () => {
  const prevIndex =
    currentAudioIndex.value > 0
      ? currentAudioIndex.value - 1
      : audioFiles.value.length - 1;
  currentAudioIndex.value = prevIndex;
  selectedAudio.value = audioFiles.value[prevIndex];
};

const nextAudio = () => {
  const nextIndex =
    currentAudioIndex.value < audioFiles.value.length - 1
      ? currentAudioIndex.value + 1
      : 0;
  currentAudioIndex.value = nextIndex;
  selectedAudio.value = audioFiles.value[nextIndex];
};

const closeModal = () => {
  modalVisible.value = false;
  selectedAnnotation.value = null;
};

watch(playModalVisible, (val) => {
  if (val && selectedAudio.value) {
    initWaveSurfer();
  } else {
    destroyWaveSurfer();
  }
});

watch(selectedAudio, () => {
  if (playModalVisible.value && selectedAudio.value) {
    initWaveSurfer();
  }
});

onMounted(() => {
  loadAnnotations();
  loadAudioFiles();
  loadLabels();
});
</script>
