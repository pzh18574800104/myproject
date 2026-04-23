import { describe, it, expect, vi } from 'vitest'
import { fileApi, type StudyFile } from '../file'

vi.mock('element-plus', () => ({
  ElMessage: {
    error: vi.fn(),
    success: vi.fn()
  }
}))

vi.mock('@/stores/user', () => ({
  useUserStore: vi.fn(() => ({
    token: null,
    logout: vi.fn()
  }))
}))

describe('fileApi interfaces', () => {
  it('should export correct StudyFile interface', () => {
    const file: StudyFile = {
      id: 1,
      userId: 1,
      fileName: 'test.md',
      filePath: '/path/to/file',
      fileType: 'md',
      fileSize: 100,
      content: '# Hello',
      parentId: 0,
      isFolder: 0,
      createTime: '2024-01-01',
      updateTime: '2024-01-01'
    }

    expect(file.fileName).toBe('test.md')
    expect(file.fileType).toBe('md')
    expect(file.isFolder).toBe(0)
  })

  it('should have all required API methods', () => {
    expect(typeof fileApi.list).toBe('function')
    expect(typeof fileApi.upload).toBe('function')
    expect(typeof fileApi.createFolder).toBe('function')
    expect(typeof fileApi.createTextFile).toBe('function')
    expect(typeof fileApi.getContent).toBe('function')
    expect(typeof fileApi.updateContent).toBe('function')
    expect(typeof fileApi.moveFile).toBe('function')
    expect(typeof fileApi.download).toBe('function')
    expect(typeof fileApi.delete).toBe('function')
  })
})
