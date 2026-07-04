import { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, Select, Tag, message, Popconfirm, Space } from 'antd';
import { PlusOutlined, PlayCircleOutlined, DeleteOutlined, ReloadOutlined } from '@ant-design/icons';
import { getAllTasks, createTask, startTask, deleteTask } from '../api/train';
import { getAllDatasets } from '../api/dataset';
import dayjs from 'dayjs';

function TrainPage() {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [datasets, setDatasets] = useState([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [form] = Form.useForm();

  useEffect(() => {
    loadTasks();
    loadDatasets();
    
    const interval = setInterval(() => {
      const runningTasks = tasks.filter(t => t.status === 'running');
      if (runningTasks.length > 0) {
        loadTasks();
      }
    }, 3000);
    
    return () => clearInterval(interval);
  }, [tasks.length]);

  const loadTasks = () => {
    setLoading(true);
    getAllTasks().then(res => {
      setTasks(res.data.data);
    }).finally(() => setLoading(false));
  };

  const loadDatasets = () => {
    getAllDatasets().then(res => setDatasets(res.data.data));
  };

  const handleAdd = () => {
    form.resetFields();
    setModalVisible(true);
  };

  const handleSubmit = (values) => {
    createTask(values).then(res => {
      message.success('训练任务创建成功');
      setModalVisible(false);
      loadTasks();
    }).catch(err => {
      message.error(err.response?.data?.message || '创建失败');
    });
  };

  const handleStart = (id) => {
    startTask(id).then(res => {
      message.success('训练任务已启动');
      loadTasks();
    }).catch(err => {
      message.error(err.response?.data?.message || '启动失败');
    });
  };

  const handleDelete = (id) => {
    deleteTask(id).then(res => {
      message.success('删除成功');
      loadTasks();
    }).catch(err => {
      message.error(err.response?.data?.message || '删除失败');
    });
  };

  const handleRefresh = () => {
    loadTasks();
  };

  const statusConfig = {
    pending: { color: 'blue', text: '等待中' },
    running: { color: 'orange', text: '训练中' },
    success: { color: 'green', text: '已完成' },
    failed: { color: 'red', text: '失败' },
  };

  const columns = [
    { title: '任务名称', dataIndex: 'taskName', key: 'taskName' },
    { title: '数据集', dataIndex: 'datasetId', key: 'datasetId',
      render: (id) => datasets.find(d => d.datasetId === id)?.datasetName || id
    },
    { title: '模型类型', dataIndex: 'modelType', key: 'modelType' },
    { title: '层级损失', dataIndex: 'enableHierarchicalLoss', key: 'enableHierarchicalLoss',
      render: (val) => val ? '启用' : '禁用'
    },
    { title: '状态', dataIndex: 'status', key: 'status',
      render: (status) => <Tag color={statusConfig[status]?.color}>{statusConfig[status]?.text}</Tag>
    },
    { title: '当前轮次', dataIndex: 'currentEpoch', key: 'currentEpoch',
      render: (epoch) => epoch != null ? <span className="number-font">{epoch}轮</span> : '-'
    },
    { title: '最佳指标', dataIndex: 'bestValMetric', key: 'bestValMetric',
      render: (val) => val != null ? <span className="number-font">{(val * 100).toFixed(1)}%</span> : '-'
    },
    { title: '创建时间', dataIndex: 'createTime', key: 'createTime',
      render: (time) => dayjs(time).format('YYYY-MM-DD HH:mm')
    },
    { title: '开始时间', dataIndex: 'startTime', key: 'startTime',
      render: (time) => time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-'
    },
    { title: '结束时间', dataIndex: 'endTime', key: 'endTime',
      render: (time) => time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-'
    },
    { 
      title: '操作', 
      key: 'action', 
      render: (_, record) => (
        <Space>
          {record.status === 'pending' && (
            <Button icon={<PlayCircleOutlined />} onClick={() => handleStart(record.taskId)}>启动</Button>
          )}
          {record.status === 'running' && (
            <Button icon={<ReloadOutlined />} onClick={handleRefresh} loading>训练中</Button>
          )}
          {record.status !== 'running' && (
            <Popconfirm title="确定删除？" onConfirm={() => handleDelete(record.taskId)}>
              <Button icon={<DeleteOutlined />} danger>删除</Button>
            </Popconfirm>
          )}
        </Space>
      )
    }
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <div className="page-title">训练任务管理</div>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>创建训练任务</Button>
      </div>

      <div style={{ backgroundColor: '#1A2233', borderRadius: 8, border: '1px solid #2A3344', overflow: 'hidden' }}>
        <Table 
          dataSource={tasks} 
          columns={columns} 
          rowKey="taskId"
          loading={loading}
          pagination={{ pageSize: 10 }}
        />
      </div>

      <Modal
        title="创建训练任务"
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        width={600}
      >
        <Form form={form} onFinish={handleSubmit} layout="vertical">
          <Form.Item name="taskName" label="任务名称" rules={[{ required: true, message: '请输入任务名称' }]}>
            <Input placeholder="请输入任务名称" />
          </Form.Item>
          <Form.Item name="datasetId" label="数据集" rules={[{ required: true, message: '请选择数据集' }]}>
            <Select placeholder="选择数据集">
              {datasets.map(d => <Select.Option key={d.datasetId} value={d.datasetId}>{d.datasetName}</Select.Option>)}
            </Select>
          </Form.Item>
          <Form.Item name="modelType" label="模型类型" rules={[{ required: true, message: '请选择模型类型' }]}>
            <Select placeholder="选择模型类型">
              <Select.Option value="RandomForest">随机森林</Select.Option>
              <Select.Option value="SVM">支持向量机</Select.Option>
              <Select.Option value="CNN">卷积神经网络</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="enableHierarchicalLoss" label="启用层级损失" valuePropName="checked">
            <Select placeholder="是否启用层级损失">
              <Select.Option value={true}>启用</Select.Option>
              <Select.Option value={false}>禁用</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="trainParams" label="训练参数">
            <Input.TextArea placeholder='{"n_estimators": 100, "max_depth": 10}' rows={3} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit">提交</Button>
            <Button onClick={() => setModalVisible(false)} style={{ marginLeft: 8 }}>取消</Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

export default TrainPage;