import axios from 'axios'

const apiClient = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

export interface ApiResponse<T = any> {
  code: number
  data: T
  message: string
}

export const helloApi = {
  getHello: () => apiClient.get('/hello')
}

export default apiClient
