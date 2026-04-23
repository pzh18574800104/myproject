<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { helloApi } from '@/api'

const message = ref('')
const timestamp = ref(0)
const loading = ref(false)
const error = ref('')

const fetchHello = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await helloApi.getHello()
    message.value = response.data.message
    timestamp.value = response.data.timestamp
  } catch (err: any) {
    error.value = err.message || '请求失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchHello()
})
</script>

<template>
  <div class="api-demo">
    <h1>API 演示</h1>
    <p>这是一个 Vue 3 + Spring Boot 3 前后端联调示例</p>
    
    <div class="card">
      <h2>后端响应</h2>
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="error" class="error">{{ error }}</div>
      <div v-else class="result">
        <p><strong>消息：</strong>{{ message }}</p>
        <p><strong>时间戳：</strong>{{ timestamp }}</p>
      </div>
      <button @click="fetchHello" :disabled="loading">
        {{ loading ? '请求中...' : '重新请求' }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.api-demo {
  padding: 2rem;
  max-width: 800px;
  margin: 0 auto;
}

h1 {
  color: #42b883;
}

.card {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1.5rem;
  margin-top: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.loading {
  color: #666;
}

.error {
  color: #dc3545;
}

.result p {
  margin: 0.5rem 0;
}

button {
  background: #42b883;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  cursor: pointer;
  margin-top: 1rem;
  font-size: 1rem;
}

button:hover:not(:disabled) {
  background: #369870;
}

button:disabled {
  background: #ccc;
  cursor: not-allowed;
}
</style>
