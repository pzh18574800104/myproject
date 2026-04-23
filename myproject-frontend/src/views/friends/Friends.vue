<template>
  <div class="friends-page">
    <!-- Left Sidebar -->
    <div class="left-sidebar">
      <!-- Search -->
      <div class="search-section">
        <el-input
          v-model="searchKeyword"
          placeholder="Search by ID or name"
          :prefix-icon="Search"
          @keyup.enter="handleSearch"
          clearable
        />
        <el-button type="primary" :icon="Search" @click="handleSearch" class="search-btn">Search</el-button>
      </div>

      <!-- Search Results -->
      <div v-if="searchResults.length > 0" class="search-results">
        <div class="section-title">Search Results</div>
        <div v-for="user in searchResults" :key="user.id" class="user-item">
          <el-avatar :size="36" :src="user.avatar || defaultAvatar" />
          <div class="user-info">
            <div class="user-name">{{ user.nickname || user.username }}</div>
            <div class="user-id">ID: {{ user.id }}</div>
          </div>
          <el-button type="primary" size="small" @click="addFriend(user.id)">Add</el-button>
        </div>
      </div>

      <!-- Friend Requests -->
      <div v-if="friendRequests.length > 0" class="requests-section">
        <div class="section-title">
          Friend Requests
          <el-tag type="warning" size="small">{{ friendRequests.length }}</el-tag>
        </div>
        <div v-for="req in friendRequests" :key="req.id" class="request-item">
          <div class="request-info">
            <div>User ID: {{ req.fromUserId }}</div>
          </div>
          <div class="request-actions">
            <el-button type="success" size="small" @click="acceptRequest(req.id)">Accept</el-button>
            <el-button type="danger" size="small" @click="rejectRequest(req.id)">Reject</el-button>
          </div>
        </div>
      </div>

      <!-- Friend List -->
      <div class="friends-section">
        <div class="section-title">My Friends</div>
        <div
          v-for="friend in friends"
          :key="friend.id"
          class="friend-item"
          :class="{ active: selectedFriend?.friendUserId === friend.friendUserId }"
          @click="selectFriend(friend)"
        >
          <el-avatar :size="40" :src="friend.avatar || defaultAvatar" />
          <div class="friend-info">
            <div class="friend-name">{{ friend.remarkName || friend.nickname || friend.username || 'User ' + friend.friendUserId }}</div>
          </div>
        </div>
        <el-empty v-if="friends.length === 0" description="No friends yet" />
      </div>
    </div>

    <!-- Right Chat Area -->
    <div class="chat-area">
      <template v-if="selectedFriend">
        <!-- Chat Header -->
        <div class="chat-header">
          <el-avatar :size="36" :src="selectedFriend.avatar || defaultAvatar" />
          <span class="chat-title">{{ selectedFriend.remarkName || selectedFriend.nickname || selectedFriend.username || 'User ' + selectedFriend.friendUserId }}</span>
        </div>

        <!-- Messages -->
        <div ref="messagesRef" class="messages-container">
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="message-row"
            :class="{ self: msg.senderId === currentUserId }"
          >
            <el-avatar
              :size="32"
              :src="msg.senderId === currentUserId ? currentUserAvatar : (selectedFriend.avatar || defaultAvatar)"
            />
            <div class="message-bubble">
              <div class="message-content">{{ msg.content }}</div>
              <div class="message-time">{{ formatTime(msg.createTime) }}</div>
            </div>
          </div>
          <el-empty v-if="messages.length === 0" description="Start a conversation!" />
        </div>

        <!-- Input Area -->
        <div class="input-area">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            placeholder="Type a message..."
            @keyup.ctrl.enter="sendMessage"
          />
          <div class="input-actions">
            <el-button
              type="warning"
              :icon="MagicStick"
              :loading="aiLoading"
              @click="aiSuggest"
            >
              AI Suggest
            </el-button>
            <el-button type="primary" :icon="Promotion" :loading="sending" @click="sendMessage">
              Send (Ctrl+Enter)
            </el-button>
          </div>
        </div>
      </template>

      <el-empty v-else description="Select a friend to start chatting" class="empty-chat" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Promotion, MagicStick } from '@element-plus/icons-vue'
import { friendApi } from '@/api/friend'
import { useUserStore } from '@/stores/user'
import type { Friend, FriendRequest, PrivateMessage, UserSearchResult } from '@/api/friend'

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const userStore = useUserStore()
const currentUserId = userStore.userInfo.id
const currentUserAvatar = userStore.userInfo.avatar || defaultAvatar

const searchKeyword = ref('')
const searchResults = ref<UserSearchResult[]>([])
const friends = ref<Friend[]>([])
const friendRequests = ref<FriendRequest[]>([])
const selectedFriend = ref<Friend | null>(null)
const messages = ref<PrivateMessage[]>([])
const inputMessage = ref('')
const sending = ref(false)
const aiLoading = ref(false)
const messagesRef = ref<HTMLDivElement>()

const loadFriends = async () => {
  try {
    const res = await friendApi.listFriends()
    friends.value = res.data
  } catch {
    ElMessage.error('Failed to load friends')
  }
}

const loadFriendRequests = async () => {
  try {
    const res = await friendApi.listFriendRequests()
    friendRequests.value = res.data
  } catch {
    // ignore
  }
}

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    searchResults.value = []
    return
  }
  try {
    const res = await friendApi.searchUsers(searchKeyword.value.trim())
    searchResults.value = res.data
  } catch {
    ElMessage.error('Search failed')
  }
}

const addFriend = async (userId: number) => {
  try {
    await friendApi.sendFriendRequest(userId)
    ElMessage.success('Friend request sent')
    searchResults.value = searchResults.value.filter(u => u.id !== userId)
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || 'Failed to send request')
  }
}

const acceptRequest = async (requestId: number) => {
  try {
    await friendApi.acceptFriendRequest(requestId)
    ElMessage.success('Friend request accepted')
    loadFriendRequests()
    loadFriends()
  } catch {
    ElMessage.error('Failed to accept request')
  }
}

const rejectRequest = async (requestId: number) => {
  try {
    await friendApi.rejectFriendRequest(requestId)
    ElMessage.success('Friend request rejected')
    loadFriendRequests()
  } catch {
    ElMessage.error('Failed to reject request')
  }
}

const selectFriend = async (friend: Friend) => {
  selectedFriend.value = friend
  try {
    const res = await friendApi.listMessages(friend.friendUserId)
    messages.value = res.data
    await nextTick()
    scrollToBottom()
  } catch {
    ElMessage.error('Failed to load messages')
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || !selectedFriend.value) return
  const content = inputMessage.value.trim()
  inputMessage.value = ''
  sending.value = true

  const tempMsg: PrivateMessage = {
    id: Date.now(),
    senderId: currentUserId,
    receiverId: selectedFriend.value.friendUserId,
    content,
    createTime: new Date().toISOString()
  }
  messages.value.push(tempMsg)
  await nextTick()
  scrollToBottom()

  try {
    const res = await friendApi.sendMessage(selectedFriend.value.friendUserId, content)
    messages.value.push(res.data)
    await nextTick()
    scrollToBottom()
  } catch {
    ElMessage.error('Failed to send message')
  } finally {
    sending.value = false
  }
}

const aiSuggest = async () => {
  if (!selectedFriend.value) return
  aiLoading.value = true
  try {
    const res = await friendApi.aiSuggestReply(selectedFriend.value.friendUserId)
    inputMessage.value = res.data
  } catch {
    ElMessage.error('AI suggestion failed')
  } finally {
    aiLoading.value = false
  }
}

const scrollToBottom = () => {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString()
}

onMounted(() => {
  loadFriends()
  loadFriendRequests()
})
</script>

<style scoped>
.friends-page {
  display: flex;
  height: calc(100vh - 60px);
  background-color: #fff;
}

.left-sidebar {
  width: 320px;
  min-width: 320px;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.search-section {
  padding: 16px;
  display: flex;
  gap: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.search-btn {
  flex-shrink: 0;
}

.search-results,
.requests-section,
.friends-section {
  padding: 12px 16px;
  overflow-y: auto;
}

.requests-section {
  border-bottom: 1px solid #e4e7ed;
  background-color: #fdf6ec;
}

.section-title {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-item,
.request-item,
.friend-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border-radius: 6px;
  margin-bottom: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.user-item:hover,
.friend-item:hover {
  background-color: #f5f7fa;
}

.friend-item.active {
  background-color: #ecf5ff;
}

.user-info,
.friend-info {
  flex: 1;
  min-width: 0;
}

.user-name,
.friend-name {
  font-size: 14px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-id {
  font-size: 12px;
  color: #909399;
}

.request-item {
  justify-content: space-between;
}

.request-info {
  font-size: 13px;
  color: #606266;
}

.request-actions {
  display: flex;
  gap: 4px;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  border-bottom: 1px solid #e4e7ed;
  background-color: #fff;
}

.chat-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: #f5f7fa;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 16px;
}

.message-row.self {
  flex-direction: row-reverse;
}

.message-bubble {
  max-width: 60%;
  background-color: #fff;
  padding: 10px 14px;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
}

.message-row.self .message-bubble {
  background-color: #409eff;
  color: #fff;
}

.message-content {
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.message-time {
  font-size: 11px;
  margin-top: 4px;
  opacity: 0.7;
}

.input-area {
  padding: 12px 20px;
  border-top: 1px solid #e4e7ed;
  background-color: #fff;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 10px;
}

.empty-chat {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
