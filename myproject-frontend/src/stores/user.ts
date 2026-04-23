import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { MenuItem } from '@/api/auth'
import { authApi } from '@/api/auth'

export const useUserStore = defineStore(
  'user',
  () => {
    const token = ref('')
    const userInfo = ref({
      id: 0,
      username: '',
      nickname: '',
      avatar: ''
    })
    const menus = ref<MenuItem[]>([])

    const isLoggedIn = computed(() => !!token.value)

    const setToken = (newToken: string) => {
      token.value = newToken
    }

    const setUserInfo = (info: typeof userInfo.value) => {
      userInfo.value = info
    }

    const setMenus = (newMenus: MenuItem[]) => {
      menus.value = buildMenuTree(newMenus)
    }

    const buildMenuTree = (flatMenus: MenuItem[]): MenuItem[] => {
      const map = new Map<number, MenuItem>()
      const roots: MenuItem[] = []

      flatMenus.forEach((menu) => {
        map.set(menu.id, { ...menu, children: [] })
      })

      flatMenus.forEach((menu) => {
        const node = map.get(menu.id)!
        if (menu.parentId === 0) {
          roots.push(node)
        } else {
          const parent = map.get(menu.parentId)
          if (parent) {
            parent.children = parent.children || []
            parent.children.push(node)
          }
        }
      })

      return roots.sort((a, b) => a.sortOrder - b.sortOrder)
    }

    const login = async (username: string, password: string) => {
      const res = await authApi.login({ username, password })
      const data = res.data
      setToken(data.token)
      setUserInfo(data.user)
      setMenus(data.menus)
      return data
    }

    const getInfo = async () => {
      const res = await authApi.getInfo()
      const data = res.data
      setUserInfo(data.user)
      setMenus(data.menus)
      return data
    }

    const logout = () => {
      token.value = ''
      userInfo.value = {
        id: 0,
        username: '',
        nickname: '',
        avatar: ''
      }
      menus.value = []
    }

    return {
      token,
      userInfo,
      menus,
      isLoggedIn,
      setToken,
      setUserInfo,
      setMenus,
      login,
      getInfo,
      logout
    }
  },
  {
    persist: true
  }
)
