import { useState } from "react";
import { Form, Input, Button, message, Card } from "antd";
import { AudioOutlined, UserOutlined, LockOutlined } from "@ant-design/icons";
import { login } from "../api/auth";

function LoginPage({ setUser }) {
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm();

  const handleLogin = (values) => {
    setLoading(true);
    login(values)
      .then((res) => {
        const { token, user } = res.data.data;
        localStorage.setItem("token", token);
        localStorage.setItem("user", JSON.stringify(user));
        setUser(user);
        message.success("登录成功");
        window.location.href = "/dashboard";
      })
      .catch((err) => {
        message.error(err.response?.data?.message || "登录失败");
      })
      .finally(() => setLoading(false));
  };

  return (
    <div
      style={{
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        backgroundColor: "#121826",
        backgroundImage: `
        linear-gradient(rgba(42, 51, 68, 0.3) 1px, transparent 1px),
        linear-gradient(90deg, rgba(42, 51, 68, 0.3) 1px, transparent 1px)
      `,
        backgroundSize: "20px 20px",
      }}
    >
      <Card
        style={{
          width: 420,
          backgroundColor: "#1A2233",
          border: "1px solid #2A3344",
          borderRadius: 12,
          boxShadow: "0 8px 32px rgba(0, 0, 0, 0.3)",
        }}
        styles={{ body: { padding: "40px 32px" } }}
      >
        <div style={{ textAlign: "center", marginBottom: 32 }}>
          <div
            style={{
              width: 64,
              height: 64,
              backgroundColor: "#165DFF",
              borderRadius: 16,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              margin: "0 auto 16px",
            }}
          >
            <AudioOutlined style={{ color: "#fff", fontSize: 32 }} />
          </div>
          <h1
            style={{
              fontSize: 24,
              fontWeight: 600,
              color: "#E5E6EB",
              marginBottom: 8,
            }}
          >
            动物声纹系统
          </h1>
          <p style={{ fontSize: 14, color: "#86909C" }}>
            数据标注与多级分类训练平台
          </p>
        </div>

        <Form form={form} onFinish={handleLogin} layout="vertical">
          <Form.Item
            name="username"
            label="用户名"
            rules={[{ required: true, message: "请输入用户名" }]}
          >
            <Input
              prefix={<UserOutlined />}
              placeholder="请输入用户名"
              size="large"
            />
          </Form.Item>

          <Form.Item
            name="password"
            label="密码"
            rules={[{ required: true, message: "请输入密码" }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="请输入密码"
              size="large"
            />
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              size="large"
              style={{ width: "100%", height: 44 }}
            >
              登录
            </Button>
          </Form.Item>
        </Form>

        <div style={{ textAlign: "center", marginTop: 20 }}>
          <p style={{ fontSize: 12, color: "#646D7A", marginBottom: 8 }}>
            默认账号：admin / password
          </p>
          <p style={{ fontSize: 12, color: "#646D7A" }}>
            还没有账号？
            <a href="/register" style={{ color: "#165DFF" }}>
              立即注册
            </a>
          </p>
        </div>
      </Card>
    </div>
  );
}

export default LoginPage;
