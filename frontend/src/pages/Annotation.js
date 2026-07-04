import { useState, useEffect, useRef } from "react";
import {
  Table,
  Button,
  Modal,
  Form,
  Input,
  Select,
  Tag,
  message,
  Space,
  Popconfirm,
} from "antd";
import {
  PlayCircleOutlined,
  EditOutlined,
  DeleteOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  SearchOutlined,
  ArrowLeftOutlined,
  ArrowRightOutlined,
} from "@ant-design/icons";
import WaveSurfer from "wavesurfer.js";
import RegionsPlugin from "wavesurfer.js/dist/plugins/regions.esm.js";
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

function AnnotationPage() {
  const [annotations, setAnnotations] = useState([]);
  const [audioFiles, setAudioFiles] = useState([]);
  const [labels, setLabels] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [playModalVisible, setPlayModalVisible] = useState(false);
  const [form] = Form.useForm();
  const [selectedAnnotation, setSelectedAnnotation] = useState(null);
  const [selectedAudio, setSelectedAudio] = useState(null);
  const [searchText, setSearchText] = useState("");
  const [currentAudioIndex, setCurrentAudioIndex] = useState(0);
  const wavesurferRef = useRef(null);
  const regionsRef = useRef(null);

  useEffect(() => {
    loadAnnotations();
    loadAudioFiles();
    loadLabels();
  }, []);

  useEffect(() => {
    if (playModalVisible && selectedAudio) {
      initWaveSurfer();
    } else {
      destroyWaveSurfer();
    }
  }, [playModalVisible, selectedAudio]);

  const initWaveSurfer = () => {
    if (wavesurferRef.current) {
      wavesurferRef.current.destroy();
    }

    wavesurferRef.current = WaveSurfer.create({
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

    regionsRef.current = wavesurferRef.current.registerPlugin(
      RegionsPlugin.create({
        dragSelection: true,
        regions: [],
      }),
    );

    wavesurferRef.current.load(`/api/audio/download/${selectedAudio.filePath}`);

    regionsRef.current.on("region-created", (region) => {
      region.update({
        color: "rgba(22, 93, 255, 0.3)",
        drag: true,
        resize: true,
      });
    });

    regionsRef.current.on("region-clicked", (region, e) => {
      e.stopPropagation();
      region.play();
    });
  };

  const destroyWaveSurfer = () => {
    if (wavesurferRef.current) {
      wavesurferRef.current.destroy();
      wavesurferRef.current = null;
    }
  };

  const loadAnnotations = () => {
    setLoading(true);
    getAllAnnotations()
      .then((res) => {
        setAnnotations(res.data.data);
      })
      .finally(() => setLoading(false));
  };

  const loadAudioFiles = () => {
    getAllAudioFiles().then((res) => setAudioFiles(res.data.data));
  };

  const loadLabels = () => {
    getAllLabels().then((res) => setLabels(res.data.data));
  };

  const handleAdd = () => {
    form.resetFields();
    setSelectedAnnotation(null);
    setModalVisible(true);
  };

  const handleEdit = (record) => {
    form.setFieldsValue({
      audioId: record.audioId,
      startTime: record.startTime,
      endTime: record.endTime,
      labelId: record.labelId,
      soundType: record.soundType,
      confidence: record.confidence,
      remark: record.remark,
    });
    setSelectedAnnotation(record);
    setModalVisible(true);
  };

  const handleDelete = (id) => {
    deleteAnnotation(id)
      .then((res) => {
        message.success("删除成功");
        loadAnnotations();
      })
      .catch((err) => {
        message.error(err.response?.data?.message || "删除失败");
      });
  };

  const handleSubmit = (values) => {
    if (selectedAnnotation) {
      updateAnnotation(selectedAnnotation.annotationId, values)
        .then((res) => {
          message.success("更新成功");
          setModalVisible(false);
          loadAnnotations();
        })
        .catch((err) => {
          message.error(err.response?.data?.message || "更新失败");
        });
    } else {
      createAnnotation(values)
        .then((res) => {
          message.success("创建成功");
          setModalVisible(false);
          loadAnnotations();
        })
        .catch((err) => {
          message.error(err.response?.data?.message || "创建失败");
        });
    }
  };

  const handleSubmitAnnotation = (id) => {
    submitAnnotation(id)
      .then((res) => {
        message.success("已提交审核");
        loadAnnotations();
      })
      .catch((err) => {
        message.error(err.response?.data?.message || "提交失败");
      });
  };

  const handleReview = (id, approved) => {
    reviewAnnotation(id, approved)
      .then((res) => {
        message.success(approved ? "审核通过" : "审核拒绝");
        loadAnnotations();
      })
      .catch((err) => {
        message.error(err.response?.data?.message || "审核失败");
      });
  };

  const handlePlay = (record) => {
    const audio = audioFiles.find((a) => a.audioId === record.audioId);
    if (audio) {
      setSelectedAudio(audio);
      setPlayModalVisible(true);
    }
  };

  const filteredAnnotations = annotations.filter((item) => {
    return (
      searchText === "" ||
      item.annotationId.toString().includes(searchText) ||
      (item.remark &&
        item.remark.toLowerCase().includes(searchText.toLowerCase()))
    );
  });

  const statusConfig = {
    submitted: { color: "orange", text: "待审核" },
    approved: { color: "green", text: "已通过" },
    rejected: { color: "red", text: "已拒绝" },
    draft: { color: "blue", text: "草稿" },
  };

  const roleOptions = [
    { value: "admin", label: "管理员" },
    { value: "annotator", label: "标注员" },
    { value: "algorithm", label: "算法工程师" },
    { value: "guest", label: "访客" },
  ];

  const columns = [
    {
      title: "ID",
      dataIndex: "annotationId",
      key: "annotationId",
      render: (id) => <span className="number-font">{id}</span>,
      width: 80,
    },
    {
      title: "音频文件",
      dataIndex: "audioId",
      key: "audioId",
      render: (id) => {
        const audio = audioFiles.find((a) => a.audioId === id);
        return audio ? (
          <Button type="text" onClick={() => handlePlay({ audioId: id })}>
            {audio.fileName}
          </Button>
        ) : (
          "-"
        );
      },
      width: 200,
    },
    {
      title: "时间范围",
      key: "timeRange",
      render: (record) => (
        <span className="number-font">
          {record.startTime?.toFixed(2)}s - {record.endTime?.toFixed(2)}s
        </span>
      ),
      width: 150,
    },
    {
      title: "标签",
      dataIndex: "labelId",
      key: "labelId",
      render: (id) => {
        const label = labels.find((l) => l.labelId === id);
        return label ? label.labelName : "-";
      },
      width: 120,
    },
    {
      title: "置信度",
      dataIndex: "confidence",
      key: "confidence",
      render: (conf) =>
        conf != null ? <span className="number-font">{conf}%</span> : "-",
      width: 80,
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      render: (status) => (
        <Tag color={statusConfig[status]?.color}>
          {statusConfig[status]?.text}
        </Tag>
      ),
      width: 80,
    },
    {
      title: "操作",
      key: "action",
      render: (_, record) => (
        <Space size="middle">
          <Button
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
            type="text"
            disabled={record.status === "approved"}
          />
          {record.status === "submitted" && (
            <>
              <Button
                icon={<CheckCircleOutlined />}
                onClick={() => handleReview(record.annotationId, true)}
                type="text"
                style={{ color: "#00B42A" }}
              />
              <Button
                icon={<CloseCircleOutlined />}
                onClick={() => handleReview(record.annotationId, false)}
                type="text"
                style={{ color: "#F53F3F" }}
              />
            </>
          )}
          {record.status !== "approved" && (
            <Popconfirm
              title="确定删除？"
              onConfirm={() => handleDelete(record.annotationId)}
            >
              <Button icon={<DeleteOutlined />} danger type="text" />
            </Popconfirm>
          )}
        </Space>
      ),
      width: 200,
    },
  ];

  return (
    <div>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: 20,
        }}
      >
        <div className="page-title">标注工作台</div>
        <Button type="primary" onClick={handleAdd}>
          创建标注
        </Button>
      </div>

      <div style={{ marginBottom: 16 }}>
        <Input
          placeholder="搜索标注ID或备注..."
          prefix={<SearchOutlined />}
          value={searchText}
          onChange={(e) => setSearchText(e.target.value)}
          style={{ width: 300 }}
        />
      </div>

      <div
        style={{
          backgroundColor: "#1A2233",
          borderRadius: 8,
          border: "1px solid #2A3344",
          overflow: "hidden",
        }}
      >
        <Table
          dataSource={filteredAnnotations}
          columns={columns}
          rowKey="annotationId"
          loading={loading}
          pagination={{ pageSize: 10 }}
        />
      </div>

      <Modal
        title={selectedAnnotation ? "编辑标注" : "创建标注"}
        open={modalVisible}
        onCancel={() => {
          setModalVisible(false);
          setSelectedAnnotation(null);
        }}
        footer={null}
        width={600}
      >
        <Form form={form} onFinish={handleSubmit} layout="vertical">
          <Form.Item
            name="audioId"
            label="音频文件"
            rules={[{ required: true, message: "请选择音频文件" }]}
          >
            <Select placeholder="选择音频文件">
              {audioFiles.map((a) => (
                <Select.Option key={a.audioId} value={a.audioId}>
                  {a.fileName}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="startTime"
            label="开始时间(秒)"
            rules={[{ required: true, message: "请输入开始时间" }]}
          >
            <Input type="number" placeholder="请输入开始时间" />
          </Form.Item>
          <Form.Item
            name="endTime"
            label="结束时间(秒)"
            rules={[{ required: true, message: "请输入结束时间" }]}
          >
            <Input type="number" placeholder="请输入结束时间" />
          </Form.Item>
          <Form.Item
            name="labelId"
            label="标签"
            rules={[{ required: true, message: "请选择标签" }]}
          >
            <Select placeholder="选择标签">
              {labels.map((l) => (
                <Select.Option key={l.labelId} value={l.labelId}>
                  {l.labelName}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="soundType" label="声音类型">
            <Select placeholder="选择声音类型">
              <Select.Option value="call">鸣叫</Select.Option>
              <Select.Option value="song">歌唱</Select.Option>
              <Select.Option value="alarm">警报</Select.Option>
              <Select.Option value="other">其他</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="confidence" label="置信度(0-100)">
            <Input type="number" min={0} max={100} placeholder="请输入置信度" />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea placeholder="请输入备注信息" rows={3} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit">
              提交
            </Button>
            <Button
              onClick={() => {
                setModalVisible(false);
                setSelectedAnnotation(null);
              }}
              style={{ marginLeft: 8 }}
            >
              取消
            </Button>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={selectedAudio?.fileName || "音频标注"}
        open={playModalVisible}
        onCancel={() => {
          setPlayModalVisible(false);
          setSelectedAudio(null);
        }}
        footer={null}
        width={800}
      >
        <div
          style={{
            display: "flex",
            alignItems: "center",
            gap: 12,
            marginBottom: 16,
          }}
        >
          <Button
            icon={<ArrowLeftOutlined />}
            onClick={() => {
              const prevIndex =
                currentAudioIndex > 0
                  ? currentAudioIndex - 1
                  : audioFiles.length - 1;
              setCurrentAudioIndex(prevIndex);
              setSelectedAudio(audioFiles[prevIndex]);
            }}
            disabled={audioFiles.length <= 1}
          />
          <span className="number-font" style={{ color: "#86909C" }}>
            {currentAudioIndex + 1} / {audioFiles.length}
          </span>
          <Button
            icon={<ArrowRightOutlined />}
            onClick={() => {
              const nextIndex =
                currentAudioIndex < audioFiles.length - 1
                  ? currentAudioIndex + 1
                  : 0;
              setCurrentAudioIndex(nextIndex);
              setSelectedAudio(audioFiles[nextIndex]);
            }}
            disabled={audioFiles.length <= 1}
          />
        </div>

        <div style={{ marginBottom: 16 }}>
          <div
            id="waveform"
            style={{ backgroundColor: "#121826", borderRadius: 8 }}
          />
        </div>

        {wavesurferRef.current && (
          <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
            <Button
              icon={<PlayCircleOutlined />}
              onClick={() => wavesurferRef.current.playPause()}
              type="primary"
            >
              {wavesurferRef.current.isPlaying() ? "暂停" : "播放"}
            </Button>
            <span className="number-font" style={{ color: "#86909C" }}>
              {wavesurferRef.current.getCurrentTime().toFixed(2)} /{" "}
              {wavesurferRef.current.getDuration().toFixed(2)} s
            </span>
          </div>
        )}

        {selectedAudio && (
          <div
            style={{
              marginTop: 16,
              paddingTop: 16,
              borderTop: "1px solid #2A3344",
            }}
          >
            <div
              style={{
                display: "grid",
                gridTemplateColumns: "repeat(3, 1fr)",
                gap: 12,
              }}
            >
              <div>
                <span style={{ color: "#86909C", fontSize: 12 }}>采样率</span>
                <div
                  className="number-font"
                  style={{ color: "#E5E6EB", marginTop: 4 }}
                >
                  {selectedAudio.sampleRate} Hz
                </div>
              </div>
              <div>
                <span style={{ color: "#86909C", fontSize: 12 }}>时长</span>
                <div
                  className="number-font"
                  style={{ color: "#E5E6EB", marginTop: 4 }}
                >
                  {selectedAudio.duration?.toFixed(2)} s
                </div>
              </div>
              <div>
                <span style={{ color: "#86909C", fontSize: 12 }}>噪声等级</span>
                <div style={{ color: "#E5E6EB", marginTop: 4 }}>
                  <Tag
                    color={
                      selectedAudio.noiseLevel === "high"
                        ? "red"
                        : selectedAudio.noiseLevel === "medium"
                          ? "orange"
                          : "green"
                    }
                  >
                    {selectedAudio.noiseLevel === "high"
                      ? "高"
                      : selectedAudio.noiseLevel === "medium"
                        ? "中"
                        : "低"}
                  </Tag>
                </div>
              </div>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}

export default AnnotationPage;
