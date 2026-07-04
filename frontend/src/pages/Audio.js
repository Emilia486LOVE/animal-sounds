import { useState, useEffect, useRef } from 'react';
import { Table, Button, Modal, Form, Input, Select, Tag, message, Upload, Space, Popconfirm } from 'antd';
import { UploadOutlined, PlayCircleOutlined, EditOutlined, DeleteOutlined, SearchOutlined } from '@ant-design/icons';
import WaveSurfer from 'wavesurfer.js';
import { getAllAudioFiles, uploadAudioFiles, updateAudioFile, deleteAudioFile } from '../api/audio';
import { getAllDatasets } from '../api/dataset';

function AudioPage() {
  const [audioFiles, setAudioFiles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [datasets, setDatasets] = useState([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [playModalVisible, setPlayModalVisible] = useState(false);
  const [form] = Form.useForm();
  const [selectedAudio, setSelectedAudio] = useState(null);
  const [searchText, setSearchText] = useState('');
  const [selectedDataset, setSelectedDataset] = useState(null);
  const wavesurferRef = useRef(null);
  const waveformRef = useRef(null);

  useEffect(() => {
    loadAudioFiles();
    loadDatasets();
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
      container: '#waveform',
      waveColor: '#2A3344',
      progressColor: '#165DFF',
      cursorColor: '#165DFF',
      barWidth: 2,
      barGap: 3,
      barRadius: 3,
      responsive: true,
      height: 120
    });
    wavesurferRef.current.load(`/api/audio/download/${selectedAudio.filePath}`);
  };

  const destroyWaveSurfer = () => {
    if (wavesurferRef.current) {
      wavesurferRef.current.destroy();
      wavesurferRef.current = null;
    }
  };

  const loadAudioFiles = () => {
    setLoading(true);
    getAllAudioFiles().then(res => {
      setAudioFiles(res.data.data);
    }).finally(() => setLoading(false));
  };

  const loadDatasets = () => {
    getAllDatasets().then(res => setDatasets(res.data.data));
  };

  const handleAdd = () => {
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record) => {
    form.setFieldsValue({
      noiseLevel: record.noiseLevel,
      location: record.location,
      remark: record.remark
    });
    setSelectedAudio(record);
    setModalVisible(true);
  };

  const handleDelete = (id) => {
    deleteAudioFile(id).then(res => {
      message.success('删除成功');
      loadAudioFiles();
    }).catch(err => {
      message.error(err.response?.data?.message || '删除失败');
    });
  };

  const handleSubmit = (values) => {
    if (selectedAudio) {
      updateAudioFile(selectedAudio.audioId, values).then(res => {
        message.success('更新成功');
        setModalVisible(false);
        loadAudioFiles();
      }).catch(err => {
        message.error(err.response?.data?.message || '更新失败');
      });
    } else {
      message.error('请选择音频文件');
    }
  };

  const handleUpload = ({ file }) => {
    if (!selectedDataset) {
      message.error('请先选择数据集');
      return false;
    }
    
    const formData = new FormData();
    formData.append('files', file);
    formData.append('datasetId', selectedDataset);
    
    uploadAudioFiles(formData).then(res => {
      message.success('上传成功');
      loadAudioFiles();
    }).catch(err => {
      message.error(err.response?.data?.message || '上传失败');
    });
    
    return false;
  };

  const handlePlay = (record) => {
    setSelectedAudio(record);
    setPlayModalVisible(true);
  };

  const filteredAudioFiles = audioFiles.filter(item => {
    const matchesSearch = searchText === '' || 
      item.fileName.toLowerCase().includes(searchText.toLowerCase()) ||
      (item.location && item.location.toLowerCase().includes(searchText.toLowerCase()));
    const matchesDataset = selectedDataset === null || item.datasetId === selectedDataset;
    return matchesSearch && matchesDataset;
  });

  const noiseLevelColors = {
    low: 'green',
    medium: 'orange',
    high: 'red',
    unknown: 'blue'
  };

  const noiseLevelLabels = {
    low: '低',
    medium: '中',
    high: '高',
    unknown: '未知'
  };

  const columns = [
    { 
      title: '文件名', 
      dataIndex: 'fileName', 
      key: 'fileName',
      ellipsis: true,
      width: 200
    },
    { 
      title: '数据集', 
      dataIndex: 'datasetId', 
      key: 'datasetId',
      render: (id) => datasets.find(d => d.datasetId === id)?.datasetName || id,
      width: 120
    },
    { 
      title: '时长', 
      dataIndex: 'duration', 
      key: 'duration',
      render: (d) => <span className="number-font">{d != null ? `${d.toFixed(2)}s` : '-'}</span>,
      width: 80
    },
    { 
      title: '采样率', 
      dataIndex: 'sampleRate', 
      key: 'sampleRate',
      render: (s) => <span className="number-font">{s != null ? `${s} Hz` : '-'}</span>,
      width: 100
    },
    { 
      title: '噪声等级', 
      dataIndex: 'noiseLevel', 
      key: 'noiseLevel',
      render: (level) => (
        <Tag color={noiseLevelColors[level] || 'blue'}>
          {noiseLevelLabels[level] || level}
        </Tag>
      ),
      width: 80
    },
    { 
      title: '采集地点', 
      dataIndex: 'location', 
      key: 'location',
      ellipsis: true,
      width: 120
    },
    { 
      title: '文件大小', 
      dataIndex: 'fileSize', 
      key: 'fileSize',
      render: (size) => {
        if (!size) return '-';
        if (size < 1024) return `${size} B`;
        if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
        return `${(size / (1024 * 1024)).toFixed(2)} MB`;
      },
      width: 100
    },
    { 
      title: '上传时间', 
      dataIndex: 'uploadTime', 
      key: 'uploadTime',
      render: (time) => time ? new Date(time).toLocaleDateString('zh-CN') : '-',
      width: 120
    },
    { 
      title: '操作', 
      key: 'action', 
      render: (_, record) => (
        <Space size="middle">
          <Button 
            icon={<PlayCircleOutlined />} 
            onClick={() => handlePlay(record)}
            type="text"
          />
          <Button 
            icon={<EditOutlined />} 
            onClick={() => handleEdit(record)}
            type="text"
          />
          <Popconfirm title="确定删除？" onConfirm={() => handleDelete(record.audioId)}>
            <Button 
              icon={<DeleteOutlined />} 
              danger 
              type="text"
            />
          </Popconfirm>
        </Space>
      ),
      width: 140,
      fixed: 'right'
    }
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <div className="page-title">音频管理</div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <Select
            placeholder="选择数据集"
            value={selectedDataset}
            onChange={setSelectedDataset}
            style={{ width: 150 }}
          >
            <Select.Option value="">全部</Select.Option>
            {datasets.map(d => (
              <Select.Option key={d.datasetId} value={d.datasetId}>{d.datasetName}</Select.Option>
            ))}
          </Select>
          <Upload.Dragger
            name="files"
            accept=".wav,.mp3,.flac,.ogg"
            beforeUpload={handleUpload}
            fileList={[]}
            style={{ width: 200 }}
          >
            <p className="ant-upload-drag-icon">
              <UploadOutlined />
            </p>
            <p className="ant-upload-text">点击或拖拽上传</p>
          </Upload.Dragger>
        </div>
      </div>

      <div style={{ marginBottom: 16, display: 'flex', gap: 12 }}>
        <Input
          placeholder="搜索文件名或采集地点..."
          prefix={<SearchOutlined />}
          value={searchText}
          onChange={(e) => setSearchText(e.target.value)}
          style={{ width: 300 }}
        />
      </div>

      <div style={{ backgroundColor: '#1A2233', borderRadius: 8, border: '1px solid #2A3344', overflow: 'hidden' }}>
        <Table 
          dataSource={filteredAudioFiles} 
          columns={columns} 
          rowKey="audioId"
          loading={loading}
          pagination={{ pageSize: 10 }}
          scroll={{ x: 1000 }}
          rowClassName="hover-row"
        />
      </div>

      <Modal
        title={selectedAudio ? '编辑音频信息' : '音频信息'}
        open={modalVisible}
        onCancel={() => { setModalVisible(false); setSelectedAudio(null); }}
        footer={null}
        width={500}
      >
        <Form form={form} onFinish={handleSubmit} layout="vertical">
          <Form.Item name="noiseLevel" label="噪声等级">
            <Select placeholder="选择噪声等级">
              <Select.Option value="low">低</Select.Option>
              <Select.Option value="medium">中</Select.Option>
              <Select.Option value="high">高</Select.Option>
              <Select.Option value="unknown">未知</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="location" label="采集地点">
            <Input placeholder="请输入采集地点" />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea placeholder="请输入备注信息" rows={3} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit">提交</Button>
            <Button onClick={() => { setModalVisible(false); setSelectedAudio(null); }} style={{ marginLeft: 8 }}>取消</Button>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={selectedAudio?.fileName || '音频播放'}
        open={playModalVisible}
        onCancel={() => { setPlayModalVisible(false); setSelectedAudio(null); }}
        footer={null}
        width={600}
      >
        <div style={{ marginBottom: 16 }}>
          <div ref={waveformRef} id="waveform" style={{ backgroundColor: '#121826', borderRadius: 8 }} />
        </div>
        {wavesurferRef.current && (
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <Button 
              icon={<PlayCircleOutlined />} 
              onClick={() => wavesurferRef.current.playPause()}
              type="primary"
            >
              {wavesurferRef.current.isPlaying() ? '暂停' : '播放'}
            </Button>
            <span className="number-font" style={{ color: '#86909C' }}>
              {wavesurferRef.current.getCurrentTime().toFixed(2)} / {wavesurferRef.current.getDuration().toFixed(2)} s
            </span>
          </div>
        )}
        {selectedAudio && (
          <div style={{ marginTop: 16, paddingTop: 16, borderTop: '1px solid #2A3344' }}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
              <div>
                <span style={{ color: '#86909C', fontSize: 12 }}>采样率</span>
                <div className="number-font" style={{ color: '#E5E6EB', marginTop: 4 }}>{selectedAudio.sampleRate} Hz</div>
              </div>
              <div>
                <span style={{ color: '#86909C', fontSize: 12 }}>时长</span>
                <div className="number-font" style={{ color: '#E5E6EB', marginTop: 4 }}>{selectedAudio.duration?.toFixed(2)} s</div>
              </div>
              <div>
                <span style={{ color: '#86909C', fontSize: 12 }}>通道数</span>
                <div className="number-font" style={{ color: '#E5E6EB', marginTop: 4 }}>{selectedAudio.channels} 通道</div>
              </div>
              <div>
                <span style={{ color: '#86909C', fontSize: 12 }}>文件大小</span>
                <div className="number-font" style={{ color: '#E5E6EB', marginTop: 4 }}>
                  {selectedAudio.fileSize ? (selectedAudio.fileSize < 1024 * 1024 ? `${(selectedAudio.fileSize / 1024).toFixed(1)} KB` : `${(selectedAudio.fileSize / (1024 * 1024)).toFixed(2)} MB`) : '-'}
                </div>
              </div>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}

export default AudioPage;