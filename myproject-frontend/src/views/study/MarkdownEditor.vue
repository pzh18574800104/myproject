<template>
  <div class="markdown-editor">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>Markdown 编辑器</span>
          <div class="actions">
            <el-input v-model="fileName" placeholder="文件名" style="width: 200px; margin-right: 10px" />
            <el-button type="primary" :icon="DocumentChecked" @click="saveFile">保存</el-button>
          </div>
        </div>
      </template>
      
      <div class="editor-container">
        <el-input
          v-model="content"
          type="textarea"
          :rows="25"
          placeholder="在此输入 Markdown 内容..."
          class="editor-input"
        />
        <div class="preview-panel" v-html="renderedContent"></div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { DocumentChecked } from '@element-plus/icons-vue'

const fileName = ref('')
const content = ref('# Hello World\n\n开始编写你的 Markdown 文档...')

const renderedContent = computed(() => {
  // 简单的 Markdown 渲染，实际项目中可以使用 marked 库
  return content.value
    .replace(/# (.*)/g, '<h1>$1</h1>')
    .replace(/## (.*)/g, '<h2>$1</h2>')
    .replace(/### (.*)/g, '<h3>$1</h3>')
    .replace(/\*\*(.*)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*)\*/g, '<em>$1</em>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\n/g, '<br>')
})

const saveFile = () => {
  console.log('保存文件:', fileName.value, content.value)
}
</script>

<style scoped>
.markdown-editor {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.editor-container {
  display: flex;
  gap: 20px;
  height: calc(100vh - 300px);
}

.editor-input {
  flex: 1;
}

.editor-input :deep(.el-textarea__inner) {
  font-family: 'Consolas', 'Monaco', monospace;
  resize: none;
}

.preview-panel {
  flex: 1;
  padding: 20px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  overflow-y: auto;
}
</style>
