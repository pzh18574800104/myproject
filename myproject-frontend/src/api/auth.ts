import request from './request'

export interface LoginParams {
  username: string
  password: string
}

export interface RegisterParams {
  username: string
  password: string
  nickname: string
}

export interface LoginResult {
  token: string
  user: {
    id: number
    username: string
    nickname: string
    avatar: string
  }
  menus: MenuItem[]
}

export interface MenuItem {
  id: number
  parentId: number
  menuName: string
  menuCode: string
  path: string
  component: string
  icon: string
  sortOrder: number
  status: number
  children?: MenuItem[]
}

export const authApi = {
  login: (params: LoginParams) => {
    return request.post<LoginResult>('/auth/login', params)
  },
  
  register: (params: RegisterParams) => {
    return request.post('/auth/register', params)
  },
  
  getInfo: () => {
    return request.get<LoginResult>('/auth/info')
  }
}
