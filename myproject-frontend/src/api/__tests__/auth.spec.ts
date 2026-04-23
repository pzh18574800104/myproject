import { describe, it, expect, vi, beforeEach } from 'vitest'
import { authApi } from '../auth'

const mockPost = vi.fn()
const mockGet = vi.fn()

vi.mock('../request', () => ({
  default: {
    post: (...args: any[]) => mockPost(...args),
    get: (...args: any[]) => mockGet(...args)
  }
}))

describe('authApi', () => {
  beforeEach(() => {
    mockPost.mockReset()
    mockGet.mockReset()
  })

  describe('login', () => {
    it('should call POST /auth/login with correct params', async () => {
      const mockResponse = {
        code: 200,
        data: {
          token: 'test-token',
          user: { id: 1, username: 'admin', nickname: 'Admin', avatar: '' },
          menus: []
        }
      }
      mockPost.mockResolvedValue(mockResponse)

      const result = await authApi.login({ username: 'admin', password: 'admin123' })

      expect(mockPost).toHaveBeenCalledWith('/auth/login', { username: 'admin', password: 'admin123' })
      expect(result.data.token).toBe('test-token')
    })

    it('should handle login error', async () => {
      const mockError = new Error('Network error')
      mockPost.mockRejectedValue(mockError)

      await expect(authApi.login({ username: 'admin', password: 'wrong' })).rejects.toThrow('Network error')
    })
  })

  describe('register', () => {
    it('should call POST /auth/register with correct params', async () => {
      const mockResponse = { code: 200, data: null }
      mockPost.mockResolvedValue(mockResponse)

      const params = { username: 'newuser', password: 'pass123', nickname: 'New User' }
      await authApi.register(params)

      expect(mockPost).toHaveBeenCalledWith('/auth/register', params)
    })
  })

  describe('getInfo', () => {
    it('should call GET /auth/info', async () => {
      const mockResponse = {
        code: 200,
        data: {
          token: 'test-token',
          user: { id: 1, username: 'admin', nickname: 'Admin', avatar: '' },
          menus: []
        }
      }
      mockGet.mockResolvedValue(mockResponse)

      const result = await authApi.getInfo()

      expect(mockGet).toHaveBeenCalledWith('/auth/info')
      expect(result.data.user.username).toBe('admin')
    })
  })
})
