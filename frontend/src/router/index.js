import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../pages/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../pages/Register.vue')
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('../components/Layout.vue'),
    children: [
      { path: '', redirect: '/dashboard' },
      { path: '/dashboard', name: 'Dashboard', component: () => import('../pages/Dashboard.vue') },
      { path: '/dataset', name: 'Dataset', component: () => import('../pages/Dataset.vue') },
      { path: '/audio', name: 'Audio', component: () => import('../pages/Audio.vue') },
      { path: '/label', name: 'Label', component: () => import('../pages/Label.vue') },
      { path: '/annotation', name: 'Annotation', component: () => import('../pages/Annotation.vue') },
      { path: '/train', name: 'Train', component: () => import('../pages/Train.vue') },
      { path: '/evaluation', name: 'Evaluation', component: () => import('../pages/Evaluation.vue') },
      { path: '/prediction', name: 'Prediction', component: () => import('../pages/Prediction.vue') },
      { path: '/user', name: 'User', component: () => import('../pages/User.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && to.path !== '/register' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
