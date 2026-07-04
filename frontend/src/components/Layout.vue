<template>
  <div v-if="loading" class="loading-overlay">
    <div class="loading-content">
      <div class="loading-spinner"></div>
      <div class="loading-text">加载中...</div>
    </div>
  </div>

  <el-container v-else class="app-container">
    <el-aside :width="collapsed ? '64px' : '220px'" class="sidebar">
      <div class="sidebar-header">
        <div v-if="!collapsed" class="sidebar-logo">
          <div class="logo-icon">
            <el-icon :size="18"><Mic /></el-icon>
          </div>
          <span class="logo-text">动物声纹系统</span>
        </div>
        <div v-else class="sidebar-logo-mini">
          <div class="logo-icon">
            <el-icon :size="18"><Mic /></el-icon>
          </div>
        </div>
      </div>
      
      <div class="sidebar-menu-toggle" @click="collapsed = !collapsed">
        <el-icon :size="16"><component :is="collapsed ? Expand : Fold" /></el-icon>
      </div>
      
      <el-menu
        mode="inline"
        :default-active="activeMenu"
        class="sidebar-menu"
        :collapse="collapsed"
        :collapse-transition="false"
      >
        <el-menu-item
          v-for="menu in menus"
          :key="menu.key"
          :index="menu.key"
          @click="handleMenuClick(menu.key)"
        >
          <el-icon :size="18">
            <component :is="menu.icon" />
          </el-icon>
          <template #title>{{ menu.label }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container :class="{ 'sidebar-collapsed': collapsed }" class="main-container">
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item v-for="(crumb, index) in breadcrumbs" :key="index">
              {{ crumb }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-button class="header-btn" :icon="FullScreen" circle @click="toggleFullscreen" />
          <el-badge :value="0" :hidden="true" class="notification-badge">
            <el-button class="header-btn" icon="Bell" circle />
          </el-badge>
          <span class="user-name">{{ user.realName || user.username }}</span>
          <el-tooltip :content="`角色: ${user.role}`" placement="bottom">
            <el-avatar :icon="User" class="user-avatar" />
          </el-tooltip>
          <el-dropdown @command="handleCommand">
            <el-button class="logout-btn" icon="Switch">
              退出登录
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>

      <el-footer class="footer">
        <span>© 2026 动物声纹数据标注与多级分类训练系统</span>
      </el-footer>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCurrentUser } from '../api/auth'
import {
  HomeFilled, DataBoard, Mic, PriceTag, EditPen, DataAnalysis, TrendCharts, MagicStick, User, Switch, Fold, Expand, Bell, FullScreen
} from '@element-plus/icons-vue'

const loading = ref(true)
const collapsed = ref(false)
const user = ref({})

const roleMenus = {
  admin: [
    { key: '/dashboard', icon: HomeFilled, label: '数据看板' },
    { key: '/dataset', icon: DataBoard, label: '数据集管理' },
    { key: '/audio', icon: Mic, label: '音频管理' },
    { key: '/label', icon: PriceTag, label: '标签管理' },
    { key: '/annotation', icon: EditPen, label: '标注工作台' },
    { key: '/train', icon: DataAnalysis, label: '训练任务' },
    { key: '/evaluation', icon: TrendCharts, label: '模型评估' },
    { key: '/prediction', icon: MagicStick, label: '模型预测' },
    { key: '/user', icon: User, label: '用户管理' },
  ],
  annotator: [
    { key: '/dashboard', icon: HomeFilled, label: '数据看板' },
    { key: '/dataset', icon: DataBoard, label: '数据集管理' },
    { key: '/audio', icon: Mic, label: '音频管理' },
    { key: '/label', icon: PriceTag, label: '标签管理' },
    { key: '/annotation', icon: EditPen, label: '标注工作台' },
    { key: '/prediction', icon: MagicStick, label: '模型预测' },
  ],
  algorithm: [
    { key: '/dashboard', icon: HomeFilled, label: '数据看板' },
    { key: '/dataset', icon: DataBoard, label: '数据集管理' },
    { key: '/audio', icon: Mic, label: '音频管理' },
    { key: '/label', icon: PriceTag, label: '标签管理' },
    { key: '/train', icon: DataAnalysis, label: '训练任务' },
    { key: '/evaluation', icon: TrendCharts, label: '模型评估' },
    { key: '/prediction', icon: MagicStick, label: '模型预测' },
  ],
  guest: [
    { key: '/dashboard', icon: HomeFilled, label: '数据看板' },
    { key: '/dataset', icon: DataBoard, label: '数据集浏览' },
    { key: '/audio', icon: Mic, label: '音频浏览' },
    { key: '/label', icon: PriceTag, label: '标签浏览' },
  ],
}

const menus = computed(() => {
  return roleMenus[user.value.role] || roleMenus.guest
})

const activeMenu = computed(() => {
  return window.location.pathname
})

const breadcrumbs = computed(() => {
  const path = window.location.pathname
  const menu = menus.value.find(m => m.key === path)
  return ['首页', menu ? menu.label : '']
})

const handleMenuClick = (key) => {
  window.location.href = key
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  ElMessage.success('退出成功')
  window.location.href = '/login'
}

const handleCommand = (command) => {
  if (command === 'logout') {
    handleLogout()
  }
}

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

onMounted(() => {
  const token = localStorage.getItem('token')
  if (token) {
    getCurrentUser()
      .then(res => {
        user.value = res.data.data
      })
      .catch(() => {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        window.location.href = '/login'
      })
      .finally(() => {
        loading.value = false
      })
  } else {
    loading.value = false
    window.location.href = '/login'
  }
})
</script>

<style scoped>
.loading-overlay {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: var(--color-bg-global);
}

.loading-content {
  text-align: center;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 2px solid var(--color-border);
  border-top-color: var(--color-brand-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  color: var(--color-text-secondary);
  font-size: var(--font-size-body);
}

.app-container {
  min-height: 100vh;
  background-color: var(--color-bg-global);
}

.sidebar {
  background-color: var(--color-bg-module);
  border-right: 1px solid var(--color-border);
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: var(--z-index-sidebar);
  transition: width var(--transition-normal);
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid var(--color-border);
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sidebar-logo-mini {
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-icon {
  width: 32px;
  height: 32px;
  background-color: var(--color-brand-primary);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.logo-text {
  font-size: 16px;
  font-weight: var(--font-weight-title);
  color: var(--color-text-primary);
}

.sidebar-menu-toggle {
  position: absolute;
  right: -12px;
  top: 80px;
  width: 24px;
  height: 24px;
  background-color: var(--color-bg-module);
  border: 1px solid var(--color-border);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-secondary);
  cursor: pointer;
  z-index: 101;
}

.sidebar-menu-toggle:hover {
  background-color: var(--color-bg-hover);
  color: var(--color-brand-primary);
}

.sidebar-menu {
  background-color: transparent;
  border-right: none;
  margin-top: 8px;
}

.main-container {
  margin-left: 220px;
  transition: margin-left var(--transition-normal);
}

.main-container.sidebar-collapsed {
  margin-left: 64px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--color-bg-module);
  padding: 0 24px;
  border-bottom: 1px solid var(--color-border);
  position: fixed;
  right: 0;
  left: 220px;
  top: 0;
  z-index: var(--z-index-header);
  height: 64px;
  transition: left var(--transition-normal);
}

.main-container.sidebar-collapsed .header {
  left: 64px;
}

.header-left {
  display: flex;
  align-items: center;
}

.breadcrumb {
  font-size: var(--font-size-body);
}

.breadcrumb :deep(.el-breadcrumb__item) {
  color: var(--color-text-secondary);
}

.breadcrumb :deep(.el-breadcrumb__item:last-child) {
  color: var(--color-text-primary);
}

.breadcrumb :deep(.el-breadcrumb__separator) {
  color: var(--color-text-muted);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-btn {
  background-color: transparent;
  border-color: transparent;
  color: var(--color-text-secondary);
}

.header-btn:hover {
  background-color: var(--color-bg-hover);
  color: var(--color-brand-primary);
}

.notification-badge :deep(.el-badge__content) {
  background-color: var(--color-status-danger);
}

.user-name {
  color: var(--color-text-secondary);
  font-size: var(--font-size-body);
}

.user-avatar {
  background-color: var(--color-brand-primary);
  border: 1px solid var(--color-border);
}

.logout-btn {
  border-color: var(--color-border-light);
  color: var(--color-text-secondary);
}

.logout-btn:hover {
  border-color: var(--color-brand-primary);
  color: var(--color-brand-primary);
}

.main-content {
  margin-top: 64px;
  padding: 24px;
  background-color: var(--color-bg-global);
  min-height: calc(100vh - 64px - 48px);
}

.footer {
  text-align: center;
  padding: 16px;
  color: var(--color-text-muted);
  font-size: var(--font-size-caption);
  border-top: 1px solid var(--color-border);
}
</style>
