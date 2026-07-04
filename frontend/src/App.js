import { useState, useEffect } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Layout, Menu, Button, Avatar, Tooltip } from 'antd';
import { 
  LoginOutlined, LogoutOutlined, UserOutlined, DatabaseOutlined, 
  AudioOutlined, TagOutlined, EditOutlined, ExperimentOutlined, 
  BarChartOutlined, SettingOutlined, HomeOutlined
} from '@ant-design/icons';
import LoginPage from './pages/Login';
import RegisterPage from './pages/Register';
import DashboardPage from './pages/Dashboard';
import DatasetPage from './pages/Dataset';
import AudioPage from './pages/Audio';
import LabelPage from './pages/Label';
import AnnotationPage from './pages/Annotation';
import TrainPage from './pages/Train';
import EvaluationPage from './pages/Evaluation';
import UserPage from './pages/User';
import { getCurrentUser } from './api/auth';

const { Header, Sider, Content } = Layout;

const roleMenus = {
  admin: [
    { key: '/dashboard', icon: <HomeOutlined />, label: '数据看板' },
    { key: '/dataset', icon: <DatabaseOutlined />, label: '数据集管理' },
    { key: '/audio', icon: <AudioOutlined />, label: '音频管理' },
    { key: '/label', icon: <TagOutlined />, label: '标签管理' },
    { key: '/annotation', icon: <EditOutlined />, label: '标注工作台' },
    { key: '/train', icon: <ExperimentOutlined />, label: '训练任务' },
    { key: '/evaluation', icon: <BarChartOutlined />, label: '模型评估' },
    { key: '/user', icon: <UserOutlined />, label: '用户管理' },
  ],
  annotator: [
    { key: '/dashboard', icon: <HomeOutlined />, label: '数据看板' },
    { key: '/dataset', icon: <DatabaseOutlined />, label: '数据集管理' },
    { key: '/audio', icon: <AudioOutlined />, label: '音频管理' },
    { key: '/label', icon: <TagOutlined />, label: '标签管理' },
    { key: '/annotation', icon: <EditOutlined />, label: '标注工作台' },
  ],
  algorithm: [
    { key: '/dashboard', icon: <HomeOutlined />, label: '数据看板' },
    { key: '/dataset', icon: <DatabaseOutlined />, label: '数据集管理' },
    { key: '/audio', icon: <AudioOutlined />, label: '音频管理' },
    { key: '/label', icon: <TagOutlined />, label: '标签管理' },
    { key: '/train', icon: <ExperimentOutlined />, label: '训练任务' },
    { key: '/evaluation', icon: <BarChartOutlined />, label: '模型评估' },
  ],
  guest: [
    { key: '/dashboard', icon: <HomeOutlined />, label: '数据看板' },
    { key: '/dataset', icon: <DatabaseOutlined />, label: '数据集浏览' },
    { key: '/audio', icon: <AudioOutlined />, label: '音频浏览' },
    { key: '/label', icon: <TagOutlined />, label: '标签浏览' },
  ],
};

function App() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [collapsed, setCollapsed] = useState(false);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      getCurrentUser()
        .then(res => {
          setUser(res.data.data);
        })
        .catch(() => {
          localStorage.removeItem('token');
          localStorage.removeItem('user');
        })
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
    window.location.href = '/login';
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', backgroundColor: '#121826' }}>
        <div style={{ textAlign: 'center' }}>
          <div style={{ 
            width: 40, 
            height: 40, 
            border: '2px solid #2A3344', 
            borderTopColor: '#165DFF', 
            borderRadius: '50%', 
            animation: 'spin 1s linear infinite',
            margin: '0 auto 16px'
          }} />
          <div style={{ color: '#86909C', fontSize: 14 }}>加载中...</div>
        </div>
        <style>{`
          @keyframes spin {
            to { transform: rotate(360deg); }
          }
        `}</style>
      </div>
    );
  }

  if (!user) {
    return (
      <Routes>
        <Route path="/login" element={<LoginPage setUser={setUser} />} />
        <Route path="/register" element={<RegisterPage setUser={setUser} />} />
        <Route path="*" element={<Navigate to="/login" />} />
      </Routes>
    );
  }

  const menus = roleMenus[user.role] || roleMenus.guest;

  return (
    <Layout style={{ minHeight: '100vh', backgroundColor: '#121826' }}>
      <Sider 
        collapsible 
        collapsed={collapsed} 
        onCollapse={setCollapsed} 
        style={{ 
          backgroundColor: '#1A2233', 
          borderRight: '1px solid #2A3344',
          position: 'fixed',
          left: 0,
          top: 0,
          bottom: 0,
          zIndex: 100
        }}
      >
        <div style={{ 
          height: '64px', 
          display: 'flex', 
          alignItems: 'center', 
          justifyContent: 'center', 
          color: '#E5E6EB', 
          fontSize: '16px', 
          fontWeight: 600,
          borderBottom: '1px solid #2A3344'
        }}>
          {!collapsed && (
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <div style={{ width: 32, height: 32, backgroundColor: '#165DFF', borderRadius: 8, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <AudioOutlined style={{ color: '#fff', fontSize: 16 }} />
              </div>
              <span>动物声纹系统</span>
            </div>
          )}
          {collapsed && (
            <div style={{ width: 32, height: 32, backgroundColor: '#165DFF', borderRadius: 8, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <AudioOutlined style={{ color: '#fff', fontSize: 16 }} />
            </div>
          )}
        </div>
        <Menu 
          theme="dark" 
          mode="inline" 
          selectedKeys={[window.location.pathname]}
          style={{ 
            backgroundColor: 'transparent',
            borderRight: 'none',
            marginTop: 8
          }}
          items={menus.map(menu => ({
            key: menu.key,
            icon: menu.icon,
            label: (
              <a href={menu.key} style={{ color: '#86909C' }}>
                {menu.label}
              </a>
            )
          }))}
        />
      </Sider>
      <Layout style={{ marginLeft: collapsed ? 80 : 200, transition: 'margin-left 0.2s ease' }}>
        <Header style={{ 
          display: 'flex', 
          justifyContent: 'space-between', 
          alignItems: 'center', 
          background: '#1A2233', 
          padding: '0 24px',
          borderBottom: '1px solid #2A3344',
          position: 'fixed',
          right: 0,
          left: collapsed ? 80 : 200,
          top: 0,
          zIndex: 99
        }}>
          <div style={{ fontSize: '18px', fontWeight: 600, color: '#E5E6EB' }}>动物声纹数据标注与多级分类训练系统</div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <span style={{ color: '#86909C', fontSize: 14 }}>{user.realName || user.username}</span>
            <Tooltip title={`角色: ${user.role}`}>
              <Avatar 
                icon={<UserOutlined />} 
                style={{ 
                  backgroundColor: '#165DFF', 
                  border: '1px solid #2A3344' 
                }} 
              />
            </Tooltip>
            <Button 
              icon={<LogoutOutlined />} 
              onClick={handleLogout}
              style={{ 
                borderColor: '#2A3344',
                color: '#86909C'
              }}
            >
              退出登录
            </Button>
          </div>
        </Header>
        <Content style={{ 
          margin: '88px 16px 24px', 
          padding: 24, 
          background: '#121826',
          minHeight: 280
        }}>
          <Routes>
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/dataset" element={<DatasetPage />} />
            <Route path="/audio" element={<AudioPage />} />
            <Route path="/label" element={<LabelPage />} />
            <Route path="/annotation" element={<AnnotationPage />} />
            <Route path="/train" element={<TrainPage />} />
            <Route path="/evaluation" element={<EvaluationPage />} />
            <Route path="/user" element={<UserPage />} />
            <Route path="*" element={<Navigate to="/dashboard" />} />
          </Routes>
        </Content>
        <footer style={{ 
          textAlign: 'center', 
          padding: '16px', 
          color: '#646D7A', 
          fontSize: 12,
          borderTop: '1px solid #2A3344',
          marginLeft: collapsed ? 80 : 200
        }}>
          © 2026 动物声纹数据标注与多级分类训练系统
        </footer>
      </Layout>
    </Layout>
  );
}

export default App;