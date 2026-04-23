import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '../user'

const mockLogin = vi.fn()
const mockGetInfo = vi.fn()

vi.mock('@/api/auth', () => ({
  authApi: {
    login: (...args: any[]) => mockLogin(...args),
    getInfo: () => mockGetInfo()
  }
}))

describe('useUserStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockLogin.mockReset()
    mockGetInfo.mockReset()
  })

  it('should have default state', () => {
    const store = useUserStore()
    expect(store.token).toBe('')
    expect(store.isLoggedIn).toBe(false)
    expect(store.userInfo.username).toBe('')
    expect(store.menus).toEqual([])
  })

  it('setToken should update token and isLoggedIn', () => {
    const store = useUserStore()
    store.setToken('test-token')
    expect(store.token).toBe('test-token')
    expect(store.isLoggedIn).toBe(true)
  })

  it('setUserInfo should update userInfo', () => {
    const store = useUserStore()
    store.setUserInfo({ id: 1, username: 'admin', nickname: 'Admin', avatar: '' })
    expect(store.userInfo.username).toBe('admin')
    expect(store.userInfo.nickname).toBe('Admin')
  })

  it('setMenus should build menu tree', () => {
    const store = useUserStore()
    const flatMenus = [
      { id: 1, parentId: 0, menuName: 'System', menuCode: 'system', path: '/system', component: '', icon: '', sortOrder: 1, status: 1 },
      { id: 2, parentId: 1, menuName: 'Users', menuCode: 'users', path: '/users', component: '', icon: '', sortOrder: 1, status: 1 }
    ]
    store.setMenus(flatMenus)
    expect(store.menus.length).toBe(1)
    const firstMenu = store.menus[0] as any
    expect(firstMenu.children).toBeDefined()
    expect(firstMenu.children.length).toBe(1)
    expect(firstMenu.children[0].menuName).toBe('Users')
  })

  it('login should call authApi.login and update state', async () => {
    const store = useUserStore()
    const mockData = {
      token: 'abc123',
      user: { id: 1, username: 'admin', nickname: 'Admin', avatar: '' },
      menus: []
    }
    mockLogin.mockResolvedValue({ data: mockData })

    const result = await store.login('admin', 'pass123')

    expect(mockLogin).toHaveBeenCalledWith({ username: 'admin', password: 'pass123' })
    expect(store.token).toBe('abc123')
    expect(store.userInfo.username).toBe('admin')
    expect(result.token).toBe('abc123')
  })

  it('getInfo should call authApi.getInfo and update state', async () => {
    const store = useUserStore()
    const mockData = {
      token: 'abc123',
      user: { id: 2, username: 'user', nickname: 'User', avatar: '' },
      menus: []
    }
    mockGetInfo.mockResolvedValue({ data: mockData })

    const result = await store.getInfo()

    expect(mockGetInfo).toHaveBeenCalled()
    expect(store.userInfo.username).toBe('user')
    expect(result.token).toBe('abc123')
  })

  it('logout should clear state', () => {
    const store = useUserStore()
    store.setToken('token')
    store.setUserInfo({ id: 1, username: 'admin', nickname: 'Admin', avatar: '' })
    store.setMenus([{ id: 1, parentId: 0, menuName: 'Home', menuCode: 'home', path: '/', component: '', icon: '', sortOrder: 1, status: 1 }])

    store.logout()

    expect(store.token).toBe('')
    expect(store.isLoggedIn).toBe(false)
    expect(store.userInfo.username).toBe('')
    expect(store.menus).toEqual([])
  })
})
