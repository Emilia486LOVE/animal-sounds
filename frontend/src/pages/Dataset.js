import { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, message, Popconfirm, Space } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined } from '@ant-design/icons';
import { getAllDatasets, createDataset, updateDataset, deleteDataset } from '../api/dataset';
import dayjs from 'dayjs';

function DatasetPage() {
  const [datasets, setDatasets] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [isEdit, setIsEdit] = useState(false);
  const [form] = Form.useForm();
  const [editingId, setEditingId] = useState(null);
  const [searchKeyword, setSearchKeyword] = useState('');

  useEffect(() => {
    loadDatasets();
  }, []);

  const loadDatasets = () => {
    setLoading(true);
    getAllDatasets().then(res => {
      setDatasets(res.data.data);
    }).finally(() => setLoading(false));
  };

  const handleAdd = () => {
    setIsEdit(false);
    setEditingId(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record) => {
    setIsEdit(true);
    setEditingId(record.datasetId);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  const handleDelete = (id) => {
    deleteDataset(id).then(res => {
      message.success('删除成功');
      loadDatasets();
    }).catch(err => {
      message.error(err.response?.data?.message || '删除失败');
    });
  };

  const handleSubmit = (values) => {
    if (isEdit) {
      updateDataset(editingId, values).then(res => {
        message.success('更新成功');
        setModalVisible(false);
        loadDatasets();
      }).catch(err => {
        message.error(err.response?.data?.message || '更新失败');
      });
    } else {
      createDataset(values).then(res => {
        message.success('创建成功');
        setModalVisible(false);
        loadDatasets();
      }).catch(err => {
        message.error(err.response?.data?.message || '创建失败');
      });
    }
  };

  const filteredDatasets = datasets.filter(d => 
    d.datasetName.toLowerCase().includes(searchKeyword.toLowerCase()) ||
    (d.description && d.description.toLowerCase().includes(searchKeyword.toLowerCase()))
  );

  const columns = [
    { title: '数据集名称', dataIndex: 'datasetName', key: 'datasetName' },
    { title: '描述', dataIndex: 'description', key: 'description', ellipsis: true },
    { title: '音频数量', dataIndex: 'audioCount', key: 'audioCount',
      render: (count) => <span className="number-font">{count || 0}</span>
    },
    { title: '创建时间', dataIndex: 'createTime', key: 'createTime',
      render: (time) => dayjs(time).format('YYYY-MM-DD HH:mm')
    },
    { 
      title: '操作', 
      key: 'action', 
      render: (_, record) => (
        <Space>
          <Button icon={<EditOutlined />} onClick={() => handleEdit(record)} type="text">编辑</Button>
          <Popconfirm title="确定删除？" onConfirm={() => handleDelete(record.datasetId)}>
            <Button icon={<DeleteOutlined />} danger type="text">删除</Button>
          </Popconfirm>
        </Space>
      )
    }
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <div className="page-title">数据集管理</div>
        <div style={{ display: 'flex', gap: 16 }}>
          <Input 
            prefix={<SearchOutlined />} 
            placeholder="搜索数据集" 
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            style={{ width: 200 }}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>创建数据集</Button>
        </div>
      </div>

      <div style={{ backgroundColor: '#1A2233', borderRadius: 8, border: '1px solid #2A3344', overflow: 'hidden' }}>
        <Table 
          dataSource={filteredDatasets} 
          columns={columns} 
          rowKey="datasetId"
          loading={loading}
          pagination={{ pageSize: 10 }}
        />
      </div>

      <Modal
        title={isEdit ? '编辑数据集' : '创建数据集'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        width={500}
      >
        <Form form={form} onFinish={handleSubmit} layout="vertical">
          <Form.Item name="datasetName" label="数据集名称" rules={[{ required: true, message: '请输入数据集名称' }]}>
            <Input placeholder="请输入数据集名称" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea placeholder="请输入数据集描述" rows={3} />
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

export default DatasetPage;