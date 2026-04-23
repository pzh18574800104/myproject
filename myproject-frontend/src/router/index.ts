import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/login/LoginView.vue'),
      meta: { public: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/login/RegisterView.vue'),
      meta: { public: true }
    },
    {
      path: '/',
      name: 'layout',
      component: () => import('../views/layout/MainLayout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('../views/DashboardView.vue'),
          meta: { title: '首页', icon: 'HomeFilled' }
        },
        {
          path: 'study/file',
          name: 'study-file',
          component: () => import('../views/study/FileManager.vue'),
          meta: { title: '文件管理', icon: 'Folder' }
        },

        {
          path: 'chat/ai',
          name: 'chat-ai',
          component: () => import('../views/chat/AIChat.vue'),
          meta: { title: 'AI对话', icon: 'ChatLineRound' }
        },
        {
          path: 'system/user',
          name: 'system-user',
          component: () => import('../views/system/UserManage.vue'),
          meta: { title: '用户管理', icon: 'User' }
        },
        {
          path: 'system/role',
          name: 'system-role',
          component: () => import('../views/system/RoleManage.vue'),
          meta: { title: '角色管理', icon: 'UserFilled' }
        },
        {
          path: 'system/menu',
          name: 'system-menu',
          component: () => import('../views/system/MenuManage.vue'),
          meta: { title: '菜单管理', icon: 'Menu' }
        }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.public) {
    next()
    return
  }
  
  if (!userStore.isLoggedIn) {
    next('/login')
    return
  }
  
  next()
})

export default router
