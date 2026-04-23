import request from './request'

export interface UserSearchResult {
  id: number
  username: string
  nickname: string
  avatar: string
}

export interface FriendRequest {
  id: number
  fromUserId: number
  toUserId: number
  status: number
  createTime: string
}

export interface Friend {
  id: number
  userId: number
  friendUserId: number
  remarkName: string
  username: string
  nickname: string
  avatar: string
  createTime: string
}

export interface PrivateMessage {
  id: number
  senderId: number
  receiverId: number
  content: string
  createTime: string
}

export const friendApi = {
  searchUsers: (keyword: string) =>
    request.get<UserSearchResult[]>('/friend/search', { params: { keyword } }),

  sendFriendRequest: (toUserId: number) =>
    request.post<void>('/friend/request', null, { params: { toUserId } }),

  listFriendRequests: () =>
    request.get<FriendRequest[]>('/friend/request'),

  acceptFriendRequest: (requestId: number) =>
    request.post<void>(`/friend/request/${requestId}/accept`),

  rejectFriendRequest: (requestId: number) =>
    request.post<void>(`/friend/request/${requestId}/reject`),

  listFriends: () =>
    request.get<Friend[]>('/friend/list'),

  listMessages: (friendId: number) =>
    request.get<PrivateMessage[]>(`/friend/message/${friendId}`),

  sendMessage: (receiverId: number, content: string) =>
    request.post<PrivateMessage>('/friend/message', { receiverId, content }),

  aiSuggestReply: (friendId: number) =>
    request.post<string>(`/friend/ai-suggest/${friendId}`)
}
