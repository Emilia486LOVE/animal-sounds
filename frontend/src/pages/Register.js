import { useState, useEffect } from 'react';
import { Form, Input, Button, message, Card } from 'antd';
import { AudioOutlined, UserOutlined, LockOutlined, ReloadOutlined } from '@ant-design/icons';
import { register, getCaptcha } from '../api/auth';

function RegisterPage({ setUser }) {
  const [loading, setLoading] = useState(false);
  const [captchaLoading, setCaptchaLoading] = useState(false);
  const [captchaImage, setCaptchaImage] = useState('');
  const [captchaId, setCaptchaId] = useState('');
  const [form] = Form.useForm();

  useEffect(() => {
    refreshCaptcha();
  }, []);

  const refreshCaptcha = () => {
    setCaptchaLoading(true);
    getCaptcha()
      .then(res => {
        const { captchaId: id, imageBase64 } = res.data.data;
        setCaptchaId(id);
        setCaptchaImage(imageBase64);
      })
      .catch(err => {
        message.error('获取验证码失败');
      })
      .finally(() => setCaptchaLoading(false));
  };

  const handleRegister = (values) => {
    setLoading(true);
    register({
      ...values,
      captchaId,
      captchaCode: values.captchaCode
    })
      .then(res => {
        message.success('注册成功，请登录');
        setTimeout(() => {
          window.location.href = '/login';
        }, 1500);
      })
      .catch(err => {
        message.error(err.response?.data?.message || '注册失败');
        refreshCaptcha();
        form.setFieldsValue({ captchaCode: '' });
      })
      .finally(() => setLoading(false));
  };

  const validateConfirmPassword = ({ getFieldValue }) => ({
    validator(_, value) {
      if (!value || getFieldValue('password') === value) {
        return Promise.resolve();
      }
      return Promise.reject(new Error('两次输入的密码不一致'));
    }
  });

  const validatePassword = () => ({
    validator(_, value) {
      if (!value) {
        return Promise.resolve();
      }
      if (value.length < 6) {
        return Promise.reject(new Error('密码长度至少为6位'));
      }
      if (!/[a-zA-Z]/.test(value)) {
        return Promise.reject(new Error('密码必须包含字母'));
      }
      if (!/[0-9]/.test(value)) {
        return Promise.reject(new Error('密码必须包含数字'));
      }
      return Promise.resolve();
    }
  });

  const validateUsername = () => ({
    validator(_, value) {
      if (!value) {
        return Promise.resolve();
      }
      if (!/^[a-zA-Z0-9_]+$/.test(value)) {
        return Promise.reject(new Error('用户名只能包含字母、数字和下划线'));
      }
      return Promise.resolve();
    }
  });

  return (
    <div style={{ 
      minHeight: '100vh', 
      display: 'flex', 
      alignItems: 'center', 
      justifyContent: 'center',
      backgroundColor: '#121826',
      backgroundImage: `
        linear-gradient(rgba(42, 51, 68, 0.3) 1px, transparent 1px),
        linear-gradient(90deg, rgba(42, 51, 68, 0.3) 1px, transparent 1px)
      `,
      backgroundSize: '20px 20px'
    }}>
      <Card 
        style={{ 
          width: 460, 
          backgroundColor: '#1A2233', 
          border: '1px solid #2A3344',
          borderRadius: 12,
          boxShadow: '0 8px 32px rgba(0, 0, 0, 0.3)'
        }}
        styles={{ body: { padding: '40px 32px' } }}
      >
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <div style={{ 
            width: 64, 
            height: 64, 
            backgroundColor: '#165DFF', 
            borderRadius: 16, 
            display: 'flex', 
            alignItems: 'center', 
            justifyContent: 'center',
            margin: '0 auto 16px'
          }}>
            <AudioOutlined style={{ color: '#fff', fontSize: 32 }} />
          </div>
          <h1 style={{ fontSize: 24, fontWeight: 600, color: '#E5E6EB', marginBottom: 8 }}>注册账号</h1>
          <p style={{ fontSize: 14, color: '#86909C' }}>创建您的动物声纹系统账号</p>
        </div>

        <Form form={form} onFinish={handleRegister} layout="vertical">
          <Form.Item 
            name="username" 
            label="用户名" 
            rules={[
              { required: true, message: '请输入用户名' },
              { min: 3, max: 50, message: '用户名长度必须在3-50之间' },
              validateUsername()
            ]}
          >
            <Input 
              prefix={<UserOutlined />} 
              placeholder="请输入用户名（字母、数字、下划线）"
              size="large"
            />
          </Form.Item>
          
          <Form.Item 
            name="realName" 
            label="真实姓名" 
            rules={[{ max: 50, message: '姓名长度不能超过50' }]}
          >
            <Input 
              prefix={<UserOutlined />} 
              placeholder="请输入真实姓名（选填）"
              size="large"
            />
          </Form.Item>
          
          <Form.Item 
            name="password" 
            label="密码" 
            rules={[
              { required: true, message: '请输入密码' },
              validatePassword()
            ]}
          >
            <Input.Password 
              prefix={<LockOutlined />} 
              placeholder="请输入密码（至少6位，包含字母和数字）"
              size="large"
            />
          </Form.Item>
          
          <Form.Item 
            name="confirmPassword" 
            label="确认密码" 
            rules={[
              { required: true, message: '请确认密码' },
              validateConfirmPassword
            ]}
          >
            <Input.Password 
              prefix={<LockOutlined />} 
              placeholder="请再次输入密码"
              size="large"
            />
          </Form.Item>
          
          <Form.Item 
            name="captchaCode" 
            label="验证码" 
            rules={[{ required: true, message: '请输入验证码' }]}
          >
            <div style={{ display: 'flex', gap: 12 }}>
              <Input 
                placeholder="请输入图片中的字母"
                size="large"
                style={{ flex: 1 }}
              />
              <div style={{ display: 'flex', alignItems: 'center' }}>
                <Button 
                  type="text" 
                  icon={<ReloadOutlined />} 
                  onClick={refreshCaptcha}
                  loading={captchaLoading}
                  style={{ padding: 0, marginRight: 8 }}
                />
                <img 
                  src={captchaImage} 
                  alt="验证码" 
                  onClick={refreshCaptcha}
                  style={{ 
                    width: 120, 
                    height: 40, 
                    cursor: 'pointer',
                    borderRadius: 4,
                    border: '1px solid #2A3344'
                  }}
                />
              </div>
            </div>
          </Form.Item>
          
          <Form.Item>
            <Button 
              type="primary" 
              htmlType="submit" 
              loading={loading}
              size="large"
              style={{ width: '100%', height: 44 }}
            >
              注册
            </Button>
          </Form.Item>
        </Form>

        <div style={{ textAlign: 'center', marginTop: 20 }}>
          <p style={{ fontSize: 12, color: '#646D7A' }}>
            已有账号？<a href="/login" style={{ color: '#165DFF' }}>立即登录</a>
          </p>
        </div>
      </Card>
    </div>
  );
}

export default RegisterPage;