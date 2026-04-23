import request from './request'

export interface ChatSession {
  id: number
  userId: number
  sessionName: string
  model: string
  systemPrompt: string | null
  createTime: string
}

export interface ChatMessage {
  id: number
  sessionId: number
  role: 'user' | 'assistant'
  content: string
  createTime: string
}

export const chatApi = {
  getSessions: () => {
    return request.get<ChatSession[]>('/chat/session')
  },

  createSession: (sessionName: string, model?: string, systemPrompt?: string) => {
    return request.post<ChatSession>('/chat/session', { sessionName, model, systemPrompt })
  },

  updateSession: (sessionId: number, sessionName?: string, model?: string, systemPrompt?: string) => {
    return request.put<void>(`/chat/session/${sessionId}`, { sessionName, model, systemPrompt })
  },

  deleteSession: (sessionId: number) => {
    return request.delete<void>(`/chat/session/${sessionId}`)
  },

  getMessages: (sessionId: number) => {
    return request.get<ChatMessage[]>(`/chat/session/${sessionId}/message`)
  },

  sendMessage: (sessionId: number, content: string) => {
    return request.post<ChatMessage>(`/chat/session/${sessionId}/message`, { content })
  }
}
