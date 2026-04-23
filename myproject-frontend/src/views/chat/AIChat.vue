<template>
  <div class="ai-chat">
    <el-card class="chat-card">
      <template #header>
        <div class="card-header">
          <span>AI 对话</span>
          <el-button type="primary" :icon="Plus" @click="showNewSessionDialog">新建会话</el-button>
        </div>
      </template>

      <div class="chat-container">
        <div class="session-list">
          <div
            v-for="session in sessions"
            :key="session.id"
            :class="['session-item', { active: currentSessionId === session.id }]"
            @click="selectSession(session)"
          >
            <el-icon><ChatLineRound /></el-icon>
            <span class="session-name">{{ session.sessionName }}</span>
            <el-icon class="delete-icon" @click.stop="deleteSession(session.id)"><Close /></el-icon>
          </div>
          <el-empty v-if="sessions.length === 0" description="暂无会话" />
        </div>

        <div class="chat-main">
          <div class="chat-toolbar" v-if="currentSession">
            <el-tag size="small" type="info">{{ currentSession.model }}</el-tag>
            <el-button size="small" text @click="showSettingsDialog">
              <el-icon><Setting /></el-icon> 设置
            </el-button>
          </div>
          <div class="messages" ref="messagesRef">
            <div
              v-for="msg in currentMessages"
              :key="msg.id"
              :class="['message', msg.role]"
            >
              <el-avatar :size="40" :src="msg.role === 'user' ? userAvatar : aiAvatar" />
              <div class="message-content">
                <div class="message-text">{{ msg.content }}</div>
                <div class="message-time">{{ formatTime(msg.createTime) }}</div>
              </div>
            </div>
            <el-empty v-if="currentMessages.length === 0" description="开始新对话吧" />
          </div>

          <div class="input-area">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="3"
              placeholder="输入消息... (Ctrl+Enter 发送)"
              @keyup.enter.ctrl="sendMessage"
            />
            <el-button type="primary" :icon="Promotion" @click="sendMessage" :loading="sending">
              发送
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- New Session Dialog -->
    <el-dialog v-model="newSessionVisible" title="新建会话" width="500px">
      <el-form label-width="80px">
        <el-form-item label="会话名称">
          <el-input v-model="newSessionForm.name" placeholder="输入会话名称" />
        </el-form-item>
        <el-form-item label="模型">
          <el-select v-model="newSessionForm.model" placeholder="选择模型" style="width: 100%">
            <el-option v-for="m in models" :key="m.value" :label="m.label" :value="m.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="提示词">
          <el-input
            v-model="newSessionForm.systemPrompt"
            type="textarea"
            :rows="4"
            placeholder="设置系统提示词（可选），例如：你是一个专业的编程助手"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="newSessionVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmNewSession">创建</el-button>
      </template>
    </el-dialog>

    <!-- Settings Dialog -->
    <el-dialog v-model="settingsVisible" title="会话设置" width="500px">
      <el-form label-width="80px">
        <el-form-item label="会话名称">
          <el-input v-model="settingsForm.name" placeholder="输入会话名称" />
        </el-form-item>
        <el-form-item label="模型">
          <el-select v-model="settingsForm.model" placeholder="选择模型" style="width: 100%">
            <el-option v-for="m in models" :key="m.value" :label="m.label" :value="m.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="提示词">
          <el-input
            v-model="settingsForm.systemPrompt"
            type="textarea"
            :rows="4"
            placeholder="设置系统提示词（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="settingsVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSettings">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { Plus, ChatLineRound, Promotion, Close, Setting } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { chatApi, type ChatSession, type ChatMessage } from '@/api/chat'

const userStore = useUserStore()
const userAvatar = userStore.userInfo.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const aiAvatar = 'https://img.alicdn.com/imgextra/i4/O1CN01ZJpsux1z0X6zR4h1A_!!6000000006645-55-tps-83-82.svg'

const sessions = ref<ChatSession[]>([])
const currentSessionId = ref<number>(0)
const currentMessages = ref<ChatMessage[]>([])
const inputMessage = ref('')
const sending = ref(false)
const messagesRef = ref<HTMLElement>()

const currentSession = computed(() => sessions.value.find(s => s.id === currentSessionId.value))

const models = [
  { value: 'qwen-turbo', label: '通义千问 Turbo' },
  { value: 'qwen-plus', label: '通义千问 Plus' },
  { value: 'qwen-max', label: '通义千问 Max' },
  { value: 'qwen-coder-plus', label: '通义千问 Coder' }
]

const newSessionVisible = ref(false)
const newSessionForm = ref({ name: '', model: 'qwen-turbo', systemPrompt: '' })

const settingsVisible = ref(false)
const settingsForm = ref({ name: '', model: 'qwen-turbo', systemPrompt: '' })

const loadSessions = async () => {
  try {
    const res = await chatApi.getSessions()
    sessions.value = res.data
    if (sessions.value.length > 0 && currentSessionId.value === 0) {
      const first = sessions.value[0]
      if (first) selectSession(first)
    }
  } catch {
    ElMessage.error('加载会话失败')
  }
}

const selectSession = async (session: ChatSession) => {
  currentSessionId.value = session.id
  try {
    const res = await chatApi.getMessages(session.id)
    currentMessages.value = res.data
    nextTick(() => scrollToBottom())
  } catch {
    ElMessage.error('加载消息失败')
  }
}

const showNewSessionDialog = () => {
  newSessionForm.value = { name: `新会话 ${sessions.value.length + 1}`, model: 'qwen-turbo', systemPrompt: '' }
  newSessionVisible.value = true
}

const confirmNewSession = async () => {
  try {
    const res = await chatApi.createSession(
      newSessionForm.value.name,
      newSessionForm.value.model,
      newSessionForm.value.systemPrompt
    )
    sessions.value.unshift(res.data)
    newSessionVisible.value = false
    selectSession(res.data)
  } catch {
    ElMessage.error('创建会话失败')
  }
}

const showSettingsDialog = () => {
  if (!currentSession.value) return
  settingsForm.value = {
    name: currentSession.value.sessionName,
    model: currentSession.value.model,
    systemPrompt: currentSession.value.systemPrompt || ''
  }
  settingsVisible.value = true
}

const confirmSettings = async () => {
  if (!currentSession.value) return
  try {
    await chatApi.updateSession(
      currentSession.value.id,
      settingsForm.value.name,
      settingsForm.value.model,
      settingsForm.value.systemPrompt
    )
    settingsVisible.value = false
    await loadSessions()
    ElMessage.success('设置已保存')
  } catch {
    ElMessage.error('保存失败')
  }
}

const deleteSession = async (sessionId: number) => {
  try {
    await chatApi.deleteSession(sessionId)
    sessions.value = sessions.value.filter(s => s.id !== sessionId)
    if (currentSessionId.value === sessionId) {
      currentSessionId.value = 0
      currentMessages.value = []
      if (sessions.value.length > 0) {
        const first = sessions.value[0]
        if (first) selectSession(first)
      }
    }
    ElMessage.success('删除成功')
  } catch {
    ElMessage.error('删除失败')
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || sending.value) return
  if (currentSessionId.value === 0) {
    ElMessage.warning('请先选择或创建一个会话')
    return
  }

  const content = inputMessage.value.trim()
  inputMessage.value = ''
  sending.value = true

  // Optimistically add user message
  const tempUserMsg: ChatMessage = {
    id: Date.now(),
    sessionId: currentSessionId.value,
    role: 'user',
    content,
    createTime: new Date().toISOString()
  }
  currentMessages.value.push(tempUserMsg)
  await nextTick()
  scrollToBottom()

  try {
    const res = await chatApi.sendMessage(currentSessionId.value, content)
    currentMessages.value.push(res.data)
    await nextTick()
    scrollToBottom()
  } catch {
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
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
  loadSessions()
})
</script>

<style scoped>
.ai-chat {
  padding: 20px;
}

.chat-card {
  height: calc(100vh - 100px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-container {
  display: flex;
  height: calc(100% - 60px);
  gap: 20px;
}

.session-list {
  width: 260px;
  min-width: 260px;
  border-right: 1px solid #e4e7ed;
  padding-right: 10px;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.3s;
  position: relative;
}

.session-item:hover,
.session-item.active {
  background-color: #e6f7ff;
}

.session-item:hover .delete-icon {
  opacity: 1;
}

.session-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.delete-icon {
  opacity: 0;
  transition: opacity 0.3s;
  color: #f56c6c;
  font-size: 14px;
  cursor: pointer;
}

.delete-icon:hover {
  color: #ff4d4f;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 10px;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 4px;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.message.assistant {
  flex-direction: row-reverse;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.message.assistant .message-content {
  background: #e6f7ff;
}

.message-text {
  line-height: 1.6;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.input-area {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.input-area .el-textarea {
  flex: 1;
}
</style>
