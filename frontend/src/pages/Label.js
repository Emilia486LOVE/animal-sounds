import { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, Select, Tag, message, Popconfirm, Space, Tree } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined } from '@ant-design/icons';
import { getAllLabels, createLabel, updateLabel, deleteLabel } from '../api/label';

function LabelPage() {
  const [labels, setLabels] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [form] = Form.useForm();
  const [selectedLabel, setSelectedLabel] = useState(null);
  const [searchText, setSearchText] = useState('');

  useEffect(() => {
    loadLabels();
  }, []);

  const loadLabels = () => {
    setLoading(true);
    getAllLabels().then(res => {
      setLabels(res.data.data);
    }).finally(() => setLoading(false));
  };

  const handleAdd = () => {
    form.resetFields();
    setSelectedLabel(null);
    setModalVisible(true);
  };

  const handleEdit = (record) => {
    form.setFieldsValue({
      labelName: record.labelName,
      parentId: record.parentId,
      taxonRank: record.taxonRank,
      description: record.description
    });
    setSelectedLabel(record);
    setModalVisible(true);
  };

  const handleDelete = (id) => {
    deleteLabel(id).then(res => {
      message.success('删除成功');
      loadLabels();
    }).catch(err => {
      message.error(err.response?.data?.message || '删除失败');
    });
  };

  const handleSubmit = (values) => {
    if (selectedLabel) {
      updateLabel(selectedLabel.labelId, values).then(res => {
        message.success('更新成功');
        setModalVisible(false);
        loadLabels();
      }).catch(err => {
        message.error(err.response?.data?.message || '更新失败');
      });
    } else {
      createLabel(values).then(res => {
        message.success('创建成功');
        setModalVisible(false);
        loadLabels();
      }).catch(err => {
        message.error(err.response?.data?.message || '创建失败');
      });
    }
  };

  const buildTreeData = (items, parentId = 0) => {
    return items
      .filter(item => item.parentId === parentId)
      .map(item => ({
        title: (
          <span style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            {item.labelName}
            <Tag color="blue" style={{ fontSize: 10 }}>{item.taxonRank}</Tag>
          </span>
        ),
        key: item.labelId,
        children: buildTreeData(items, item.labelId)
      }));
  };

  const filteredLabels = labels.filter(item => {
    return searchText === '' || 
      item.labelName.toLowerCase().includes(searchText.toLowerCase()) ||
      (item.description && item.description.toLowerCase().includes(searchText.toLowerCase())) ||
      item.taxonRank.toLowerCase().includes(searchText.toLowerCase());
  });

  const rankOrder = ['kingdom', 'phylum', 'class', 'order', 'family', 'genus', 'species'];
  const rankNames = {
    kingdom: '界',
    phylum: '门',
    class: '纲',
    order: '目',
    family: '科',
    genus: '属',
    species: '种'
  };

  const columns = [
    { 
      title: '标签名称', 
      dataIndex: 'labelName', 
      key: 'labelName',
      width: 150
    },
    { 
      title: '分类层级', 
      dataIndex: 'taxonRank', 
      key: 'taxonRank',
      render: (rank) => <Tag color="blue">{rankNames[rank] || rank}</Tag>,
      width: 80
    },
    { 
      title: '父标签', 
      dataIndex: 'parentId', 
      key: 'parentId',
      render: (id) => {
        if (id === 0) return '-';
        const parent = labels.find(l => l.labelId === id);
        return parent ? parent.labelName : '-';
      },
      width: 150
    },
    { 
      title: '标签路径', 
      dataIndex: 'labelPath', 
      key: 'labelPath',
      ellipsis: true,
      width: 250
    },
    { 
      title: '描述', 
      dataIndex: 'description', 
      key: 'description',
      ellipsis: true,
      width: 200
    },
    { 
      title: '创建时间', 
      dataIndex: 'createTime', 
      key: 'createTime',
      render: (time) => time ? new Date(time).toLocaleString('zh-CN') : '-',
      width: 150
    },
    { 
      title: '操作', 
      key: 'action', 
      render: (_, record) => (
        <Space size="middle">
          <Button 
            icon={<EditOutlined />} 
            onClick={() => handleEdit(record)}
            type="text"
          />
          <Popconfirm title="确定删除？" onConfirm={() => handleDelete(record.labelId)}>
            <Button 
              icon={<DeleteOutlined />} 
              danger 
              type="text"
            />
          </Popconfirm>
        </Space>
      ),
      width: 100
    }
  ];

  const leafLabels = labels.filter(l => !labels.some(child => child.parentId === l.labelId));

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <div className="page-title">标签管理</div>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>创建标签</Button>
      </div>

      <div style={{ marginBottom: 16 }}>
        <Input
          placeholder="搜索标签名称、描述或层级..."
          prefix={<SearchOutlined />}
          value={searchText}
          onChange={(e) => setSearchText(e.target.value)}
          style={{ width: 300 }}
        />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
        <div style={{ backgroundColor: '#1A2233', borderRadius: 8, border: '1px solid #2A3344', overflow: 'hidden' }}>
          <div style={{ padding: 16, borderBottom: '1px solid #2A3344' }}>
            <div className="module-title">标签树</div>
          </div>
          <div style={{ padding: 16, height: 400, overflow: 'auto' }}>
            <Tree
              treeData={buildTreeData(labels)}
              defaultExpandAll={true}
              showLine
            />
          </div>
        </div>

        <div style={{ backgroundColor: '#1A2233', borderRadius: 8, border: '1px solid #2A3344', overflow: 'hidden' }}>
          <div style={{ padding: 16, borderBottom: '1px solid #2A3344' }}>
            <div className="module-title">标签列表</div>
          </div>
          <div style={{ height: 400, overflow: 'auto' }}>
            <Table 
              dataSource={filteredLabels} 
              columns={columns} 
              rowKey="labelId"
              loading={loading}
              pagination={{ pageSize: 10 }}
              scroll={{ x: 800 }}
            />
          </div>
        </div>
      </div>

      <div style={{ marginTop: 16, backgroundColor: '#1A2233', borderRadius: 8, border: '1px solid #2A3344', padding: 16 }}>
        <div className="module-title" style={{ marginBottom: 12 }}>统计信息</div>
        <div style={{ display: 'flex', gap: 24 }}>
          <div>
            <span style={{ color: '#86909C', fontSize: 12 }}>标签总数</span>
            <div className="number-font" style={{ color: '#E5E6EB', fontSize: 20, fontWeight: 600 }}>{labels.length}</div>
          </div>
          <div>
            <span style={{ color: '#86909C', fontSize: 12 }}>叶子节点数</span>
            <div className="number-font" style={{ color: '#E5E6EB', fontSize: 20, fontWeight: 600 }}>{leafLabels.length}</div>
          </div>
          <div>
            <span style={{ color: '#86909C', fontSize: 12 }}>层级数</span>
            <div className="number-font" style={{ color: '#E5E6EB', fontSize: 20, fontWeight: 600 }}>{rankOrder.length}</div>
          </div>
        </div>
      </div>

      <Modal
        title={selectedLabel ? '编辑标签' : '创建标签'}
        open={modalVisible}
        onCancel={() => { setModalVisible(false); setSelectedLabel(null); }}
        footer={null}
        width={500}
      >
        <Form form={form} onFinish={handleSubmit} layout="vertical">
          <Form.Item name="labelName" label="标签名称" rules={[{ required: true, message: '请输入标签名称' }]}>
            <Input placeholder="请输入标签名称" />
          </Form.Item>
          <Form.Item name="parentId" label="父标签">
            <Select placeholder="选择父标签（根节点留空）">
              <Select.Option value={0}>根节点</Select.Option>
              {labels.map(l => <Select.Option key={l.labelId} value={l.labelId}>{l.labelName}</Select.Option>)}
            </Select>
          </Form.Item>
          <Form.Item name="taxonRank" label="分类层级" rules={[{ required: true, message: '请选择分类层级' }]}>
            <Select placeholder="选择分类层级">
              {rankOrder.map(rank => <Select.Option key={rank} value={rank}>{rankNames[rank]}</Select.Option>)}
            </Select>
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea placeholder="请输入标签描述" rows={3} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit">提交</Button>
            <Button onClick={() => { setModalVisible(false); setSelectedLabel(null); }} style={{ marginLeft: 8 }}>取消</Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

export default LabelPage;