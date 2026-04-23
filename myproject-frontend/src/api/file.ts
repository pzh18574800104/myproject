import request from './request'

export interface StudyFile {
  id: number
  userId: number
  fileName: string
  filePath: string
  fileType: string
  fileSize: number
  content: string
  parentId: number
  isFolder: number
  createTime: string
  updateTime: string
}

export const fileApi = {
  // 获取文件列表
  list: (parentId: number = 0) => {
    return request.get<StudyFile[]>('/file/list', { params: { parentId } })
  },

  // 上传文件
  upload: (file: File, parentId: number = 0) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('parentId', parentId.toString())
    return request.post<StudyFile>('/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 创建文件夹
  createFolder: (folderName: string, parentId: number = 0) => {
    return request.post('/file/folder', { folderName, parentId })
  },

  // 下载文件
  download: (fileId: number) => {
    return request.get(`/file/download/${fileId}`, {
      responseType: 'blob'
    })
  },

  // 删除文件
  delete: (fileId: number) => {
    return request.delete(`/file/${fileId}`)
  },

  // 创建文本文件
  createTextFile: (fileName: string, content: string, parentId: number = 0) => {
    return request.post<StudyFile>('/file/text', { fileName, content, parentId })
  },

  // 获取文件内容
  getContent: (fileId: number) => {
    return request.get<string>(`/file/${fileId}/content`)
  },

  // 更新文件内容
  updateContent: (fileId: number, content: string) => {
    return request.put(`/file/${fileId}/content`, { content })
  },

  // 移动文件
  moveFile: (fileId: number, targetParentId: number) => {
    return request.post(`/file/${fileId}/move`, { targetParentId })
  }
}
