import { useState, useEffect } from "react";
import {
  Table,
  Button,
  Modal,
  Form,
  Input,
  Select,
  Tag,
  message,
  Popconfirm,
  Space,
} from "antd";
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  LockOutlined,
  UnlockOutlined,
} from "@ant-design/icons";
import { getAllUsers, createUser, updateUser, deleteUser } from "../api/user";

function UserPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [form] = Form.useForm();
  const [editingId, setEditingId] = useState(null);
  const [passwordModalVisible, setPasswordModalVisible] = useState(false);
  const [passwordForm] = Form.useForm();
  const [changingUserId, setChangingUserId] = useState(null);

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = () => {
    setLoading(true);
    getAllUsers()
      .then((res) => {
        setUsers(res.data.data);
      })
      .finally(() => setLoading(false));
  };

  const handleAdd = () => {
    setEditingId(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record) => {
    setEditingId(record.userId);
    form.setFieldsValue({
      username: record.username,
      realName: record.realName,
      role: record.role,
      status: record.status,
    });
    setModalVisible(true);
  };

  const handleDelete = (id) => {
    deleteUser(id)
      .then((res) => {
        message.success("删除成功");
        loadUsers();
      })
      .catch((err) => {
        message.error(err.response?.data?.message || "删除失败");
      });
  };

  const handleSubmit = (values) => {
    if (editingId) {
      updateUser(editingId, values)
        .then((res) => {
          message.success("更新成功");
          setModalVisible(false);
          loadUsers();
        })
        .catch((err) => {
          message.error(err.response?.data?.message || "更新失败");
        });
    } else {
      createUser(values)
        .then((res) => {
          message.success("创建成功");
          setModalVisible(false);
          loadUsers();
        })
        .catch((err) => {
          message.error(err.response?.data?.message || "创建失败");
        });
    }
  };

  const handleChangePassword = () => {
    updateUser(changingUserId, {
      password: passwordForm.getFieldValue("newPassword"),
    })
      .then((res) => {
        message.success("密码修改成功");
        setPasswordModalVisible(false);
        passwordForm.resetFields();
      })
      .catch((err) => {
        message.error(err.response?.data?.message || "密码修改失败");
      });
  };

  const handleOpenPasswordModal = (userId, username) => {
    setChangingUserId(userId);
    passwordForm.setFieldsValue({ username });
    setPasswordModalVisible(true);
  };

  const handleToggleStatus = (userId, currentStatus) => {
    const newStatus = currentStatus === 1 ? 0 : 1;
    updateUser(userId, { status: newStatus })
      .then((res) => {
        message.success(newStatus === 1 ? "用户已启用" : "用户已禁用");
        loadUsers();
      })
      .catch((err) => {
        message.error("操作失败");
      });
  };

  const roleOptions = [
    { value: "admin", label: "管理员" },
    { value: "annotator", label: "标注员" },
    { value: "algorithm", label: "算法工程师" },
    { value: "guest", label: "访客" },
  ];

  const columns = [
    { title: "用户名", dataIndex: "username", key: "username" },
    { title: "真实姓名", dataIndex: "realName", key: "realName" },
    {
      title: "角色",
      dataIndex: "role",
      key: "role",
      render: (role) => {
        const colors = {
          admin: "red",
          annotator: "blue",
          algorithm: "orange",
          guest: "gray",
        };
        return (
          <Tag color={colors[role]}>
            {roleOptions.find((r) => r.value === role)?.label || role}
          </Tag>
        );
      },
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      render: (status) =>
        status === 1 ? (
          <Tag color="green">启用</Tag>
        ) : (
          <Tag color="red">禁用</Tag>
        ),
    },
    {
      title: "创建时间",
      dataIndex: "createTime",
      key: "createTime",
      render: (time) => new Date(time).toLocaleString("zh-CN"),
    },
    {
      title: "操作",
      key: "action",
      render: (_, record) => (
        <Space>
          <Button
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
            type="text"
          >
            编辑
          </Button>
          <Button
            onClick={() =>
              handleOpenPasswordModal(record.userId, record.username)
            }
            type="text"
          >
            修改密码
          </Button>
          {record.status === 1 ? (
            <Button
              icon={<LockOutlined />}
              onClick={() => handleToggleStatus(record.userId, 1)}
              type="text"
            >
              禁用
            </Button>
          ) : (
            <Button
              icon={<UnlockOutlined />}
              onClick={() => handleToggleStatus(record.userId, 0)}
              type="text"
            >
              启用
            </Button>
          )}
          {record.role !== "admin" && (
            <Popconfirm
              title="确定删除？"
              onConfirm={() => handleDelete(record.userId)}
            >
              <Button icon={<DeleteOutlined />} danger type="text">
                删除
              </Button>
            </Popconfirm>
          )}
        </Space>
      ),
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
        <div className="page-title">用户管理</div>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          创建用户
        </Button>
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
          dataSource={users}
          columns={columns}
          rowKey="userId"
          loading={loading}
          pagination={{ pageSize: 10 }}
        />
      </div>

      <Modal
        title={editingId ? "编辑用户" : "创建用户"}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        width={500}
      >
        <Form form={form} onFinish={handleSubmit} layout="vertical">
          <Form.Item
            name="username"
            label="用户名"
            rules={[{ required: true, message: "请输入用户名" }]}
          >
            <Input placeholder="请输入用户名" disabled={editingId !== null} />
          </Form.Item>
          {!editingId && (
            <Form.Item
              name="password"
              label="密码"
              rules={[{ required: true, message: "请输入密码" }]}
            >
              <Input.Password placeholder="请输入密码" />
            </Form.Item>
          )}
          <Form.Item
            name="realName"
            label="真实姓名"
            rules={[{ required: true, message: "请输入真实姓名" }]}
          >
            <Input placeholder="请输入真实姓名" />
          </Form.Item>
          <Form.Item
            name="role"
            label="角色"
            rules={[{ required: true, message: "请选择角色" }]}
          >
            <Select placeholder="选择角色">
              {roleOptions.map((r) => (
                <Select.Option key={r.value} value={r.value}>
                  {r.label}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select placeholder="选择状态">
              <Select.Option value={1}>启用</Select.Option>
              <Select.Option value={0}>禁用</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit">
              提交
            </Button>
            <Button
              onClick={() => setModalVisible(false)}
              style={{ marginLeft: 8 }}
            >
              取消
            </Button>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="修改密码"
        open={passwordModalVisible}
        onCancel={() => setPasswordModalVisible(false)}
        footer={null}
        width={400}
      >
        <Form
          form={passwordForm}
          onFinish={handleChangePassword}
          layout="vertical"
        >
          <Form.Item name="username" label="用户名">
            <Input disabled />
          </Form.Item>
          <Form.Item
            name="newPassword"
            label="新密码"
            rules={[{ required: true, message: "请输入新密码" }]}
          >
            <Input.Password placeholder="请输入新密码" />
          </Form.Item>
          <Form.Item
            name="confirmPassword"
            label="确认密码"
            dependencies={["newPassword"]}
            rules={[
              { required: true, message: "请确认新密码" },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue("newPassword") === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error("两次输入的密码不一致"));
                },
              }),
            ]}
          >
            <Input.Password placeholder="请再次输入新密码" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit">
              确认修改
            </Button>
            <Button
              onClick={() => setPasswordModalVisible(false)}
              style={{ marginLeft: 8 }}
            >
              取消
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

export default UserPage;
