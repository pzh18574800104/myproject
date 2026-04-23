<template>
  <div class="file-manager">
    <!-- File List View -->
    <div v-if="!editorVisible">
      <el-card>
        <template #header>
          <div class="card-header">
            <div class="header-left">
              <div class="current-path">
                <el-icon><Folder /></el-icon>
                <span class="path-text">
                  当前位置：{{ currentPathName }}
                </span>
              </div>
              <el-breadcrumb separator="/" class="breadcrumb">
                <el-breadcrumb-item @click="goToFolder(0)">根目录</el-breadcrumb-item>
                <el-breadcrumb-item v-for="(folder, index) in breadcrumb" :key="index" @click="goToFolder(folder.id)">
                  {{ folder.name }}
                </el-breadcrumb-item>
              </el-breadcrumb>
            </div>
            <div class="actions">
              <el-button type="primary" :icon="Plus" @click="showCreateFolderDialog">新建文件夹</el-button>
              <el-button type="warning" :icon="Document" @click="showCreateFileDialog">新建文件</el-button>
              <el-upload
                :show-file-list="true"
                :file-list="uploadFileList"
                :http-request="handleUpload"
                :before-upload="beforeUpload"
                :on-remove="handleRemove"
                style="display: inline-block;"
              >
                <el-button type="success" :icon="Upload">上传文件</el-button>
              </el-upload>
            </div>
          </div>
        </template>

        <el-table :data="fileList" style="width: 100%" v-loading="loading">
          <el-table-column prop="fileName" label="文件名">
            <template #default="{ row }">
              <div class="file-name-cell" @click="row.isFolder ? enterFolder(row) : openFile(row)">
                <el-icon v-if="row.isFolder" class="file-icon folder"><Folder /></el-icon>
                <el-icon v-else class="file-icon file"><Document /></el-icon>
                <span class="file-name" :class="{ 'editable': !row.isFolder }">{{ row.fileName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="fileType" label="类型" width="120">
            <template #default="{ row }">
              {{ row.isFolder ? '文件夹' : row.fileType.toUpperCase() }}
            </template>
          </el-table-column>
          <el-table-column prop="fileSize" label="大小" width="120">
            <template #default="{ row }">
              {{ formatFileSize(row.fileSize) }}
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column label="操作" width="320">
            <template #default="{ row }">
              <el-button v-if="!row.isFolder" link type="info" :icon="View" @click="viewFile(row)">查看</el-button>
              <el-button v-if="!row.isFolder && isEditable(row)" link type="primary" :icon="Edit" @click="openFile(row)">编辑</el-button>
              <el-button v-if="!row.isFolder" link type="success" :icon="Download" @click="downloadFile(row)">下载</el-button>
              <el-button link type="warning" :icon="Rank" @click="showMoveDialog(row)">移动</el-button>
              <el-button link type="danger" :icon="Delete" @click="deleteFile(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- Editor View -->
    <div v-else class="editor-container">
      <el-card>
        <template #header>
          <div class="editor-header">
            <div class="editor-title">
              <el-button :icon="ArrowLeft" @click="closeEditor">返回</el-button>
              <span class="filename">{{ editingFile?.fileName }}</span>
              <el-tag v-if="isModified" type="warning" size="small">已修改</el-tag>
            </div>
            <div class="editor-actions">
              <el-button type="primary" :icon="DocumentChecked" @click="saveFile" :loading="saving">保存</el-button>
            </div>
          </div>
        </template>

        <div class="editor-body">
          <div class="editor-pane">
            <div class="pane-title">编辑</div>
            <el-input
              v-model="fileContent"
              type="textarea"
              :rows="24"
              placeholder="请输入内容..."
              class="markdown-input"
              @input="onContentChange"
            />
          </div>
          <div class="preview-pane">
            <div class="pane-title">预览</div>
            <div class="markdown-preview" v-html="renderedContent" />
          </div>
        </div>
      </el-card>
    </div>

    <!-- Create Folder Dialog -->
    <el-dialog v-model="folderDialogVisible" title="新建文件夹" width="400px">
      <el-input v-model="newFolderName" placeholder="请输入文件夹名称" />
      <template #footer>
        <el-button @click="folderDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createFolder" :loading="creating">确定</el-button>
      </template>
    </el-dialog>

    <!-- Create File Dialog -->
    <el-dialog v-model="fileDialogVisible" title="新建文件" width="400px">
      <el-form :model="newFileForm" label-width="80px">
        <el-form-item label="文件名">
          <el-input v-model="newFileForm.fileName" placeholder="请输入文件名，如：note.md" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="fileDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createFile" :loading="creatingFile">确定</el-button>
      </template>
    </el-dialog>

    <!-- Move File Dialog -->
    <el-dialog v-model="moveDialogVisible" title="移动到" width="400px">
      <div class="folder-tree">
        <div
          class="folder-item"
          :class="{ 'selected': moveTargetId === 0 }"
          @click="moveTargetId = 0"
        >
          <el-icon><Folder /></el-icon>
          <span>根目录</span>
        </div>
        <div
          v-for="folder in allFolders"
          :key="folder.id"
          class="folder-item"
          :class="{ 'selected': moveTargetId === folder.id, 'disabled': folder.id === movingFile?.id || folder.id === movingFile?.parentId }"
          @click="folder.id !== movingFile?.id && folder.id !== movingFile?.parentId ? moveTargetId = folder.id : null"
        >
          <el-icon><Folder /></el-icon>
          <span>{{ folder.fileName }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="moveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmMove" :loading="moving">确定</el-button>
      </template>
    </el-dialog>

    <!-- View File Dialog -->
    <el-dialog
      v-model="viewDialogVisible"
      :title="viewingFile?.fileName || '查看文件'"
      width="900px"
      :before-close="closeViewDialog"
    >
      <div v-loading="viewLoading" class="view-dialog-content">
        <div v-if="isEditable(viewingFile)" class="view-preview" v-html="viewRenderedContent" />
        <pre v-else class="view-plain">{{ viewContent }}</pre>
      </div>
      <template #footer>
        <el-button @click="closeViewDialog">关闭</el-button>
        <el-button v-if="isEditable(viewingFile)" type="primary" @click="() => { closeViewDialog(); openFile(viewingFile!); }">
          编辑
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Upload, Folder, Document, Edit, Download,
  Delete, Rank, ArrowLeft, DocumentChecked, View
} from '@element-plus/icons-vue'
import { fileApi, type StudyFile } from '@/api/file'

// marked for markdown rendering
import { marked } from 'marked'

const fileList = ref<StudyFile[]>([])
const loading = ref(false)
const currentFolderId = ref(0)
const breadcrumb = ref<{ id: number; name: string }[]>([])
const uploadFileList = ref<any[]>([])
const allFolders = ref<StudyFile[]>([])

const currentPathName = computed(() => {
  if (breadcrumb.value.length === 0) {
    return '根目录'
  }
  const last = breadcrumb.value[breadcrumb.value.length - 1]
  return last ? last.name : '根目录'
})

// Editor state
const editorVisible = ref(false)
const editingFile = ref<StudyFile | null>(null)
const fileContent = ref('')
const originalContent = ref('')
const saving = ref(false)

const isModified = computed(() => {
  return fileContent.value !== originalContent.value
})

const renderedContent = computed(() => {
  try {
    return marked(fileContent.value || '')
  } catch (e) {
    return fileContent.value || ''
  }
})

// Dialog states
const folderDialogVisible = ref(false)
const newFolderName = ref('')
const creating = ref(false)

const fileDialogVisible = ref(false)
const newFileForm = ref({ fileName: '' })
const creatingFile = ref(false)

const moveDialogVisible = ref(false)
const movingFile = ref<StudyFile | null>(null)
const moveTargetId = ref(0)
const moving = ref(false)

// View dialog
const viewDialogVisible = ref(false)
const viewingFile = ref<StudyFile | null>(null)
const viewContent = ref('')
const viewLoading = ref(false)

const viewRenderedContent = computed(() => {
  try {
    return marked(viewContent.value || '')
  } catch (e) {
    return viewContent.value || ''
  }
})

const loadFiles = async () => {
  loading.value = true
  try {
    const res = await fileApi.list(currentFolderId.value)
    fileList.value = res.data
  } catch (error) {
    ElMessage.error('加载文件列表失败')
  } finally {
    loading.value = false
  }
}

const loadAllFolders = async () => {
  try {
    const res = await fileApi.list(0)
    // Get all folders recursively would be better, but for now just root folders
    allFolders.value = res.data.filter((f: StudyFile) => f.isFolder === 1)
  } catch (error) {
    console.error('加载文件夹列表失败', error)
  }
}

const formatFileSize = (size: number) => {
  if (!size || size === 0) return '-'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
  if (size < 1024 * 1024 * 1024) return (size / (1024 * 1024)).toFixed(2) + ' MB'
  return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
}

const isEditable = (file: StudyFile | null) => {
  if (!file || !file.fileType) return false
  const editableTypes = ['md', 'txt', 'json', 'xml', 'html', 'css', 'js', 'ts', 'java', 'py', 'sql', 'yml', 'yaml']
  return editableTypes.includes(file.fileType.toLowerCase())
}

const enterFolder = (folder: StudyFile) => {
  currentFolderId.value = folder.id
  breadcrumb.value.push({ id: folder.id, name: folder.fileName })
  loadFiles()
}

const goToFolder = (folderId: number) => {
  currentFolderId.value = folderId
  const index = breadcrumb.value.findIndex(item => item.id === folderId)
  if (index === -1) {
    breadcrumb.value = []
  } else {
    breadcrumb.value = breadcrumb.value.slice(0, index + 1)
  }
  loadFiles()
}

// File operations
const openFile = async (file: StudyFile) => {
  if (file.isFolder) {
    enterFolder(file)
    return
  }
  editingFile.value = file
  editorVisible.value = true
  fileContent.value = ''
  originalContent.value = ''

  try {
    const res = await fileApi.getContent(file.id)
    fileContent.value = res.data || ''
    originalContent.value = fileContent.value
  } catch (error) {
    ElMessage.error('加载文件内容失败')
  }
}

const viewFile = async (file: StudyFile) => {
  viewingFile.value = file
  viewDialogVisible.value = true
  viewContent.value = ''
  viewLoading.value = true

  try {
    const res = await fileApi.getContent(file.id)
    viewContent.value = res.data || ''
  } catch (error) {
    ElMessage.error('加载文件内容失败')
  } finally {
    viewLoading.value = false
  }
}

const closeViewDialog = () => {
  viewDialogVisible.value = false
  viewingFile.value = null
  viewContent.value = ''
}

const closeEditor = () => {
  if (isModified.value) {
    ElMessageBox.confirm('文件已修改，是否保存？', '提示', {
      confirmButtonText: '保存',
      cancelButtonText: '不保存',
      type: 'warning'
    }).then(() => {
      saveFile(() => {
        editorVisible.value = false
        editingFile.value = null
      })
    }).catch(() => {
      editorVisible.value = false
      editingFile.value = null
    })
  } else {
    editorVisible.value = false
    editingFile.value = null
  }
}

const onContentChange = () => {
  // Content changed, isModified will update automatically
}

const saveFile = async (callback?: () => void) => {
  if (!editingFile.value) return
  saving.value = true
  try {
    await fileApi.updateContent(editingFile.value.id, fileContent.value)
    originalContent.value = fileContent.value
    ElMessage.success('保存成功')
    if (callback) callback()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// Create folder
const showCreateFolderDialog = () => {
  newFolderName.value = ''
  folderDialogVisible.value = true
}

const createFolder = async () => {
  if (!newFolderName.value.trim()) {
    ElMessage.warning('请输入文件夹名称')
    return
  }
  creating.value = true
  try {
    await fileApi.createFolder(newFolderName.value, currentFolderId.value)
    ElMessage.success('创建成功')
    folderDialogVisible.value = false
    loadFiles()
  } catch (error) {
    ElMessage.error('创建失败')
  } finally {
    creating.value = false
  }
}

// Create file
const showCreateFileDialog = () => {
  newFileForm.value.fileName = ''
  fileDialogVisible.value = true
}

const createFile = async () => {
  if (!newFileForm.value.fileName.trim()) {
    ElMessage.warning('请输入文件名')
    return
  }
  let fileName = newFileForm.value.fileName
  if (!fileName.includes('.')) {
    fileName += '.md'
  }
  creatingFile.value = true
  try {
    const res = await fileApi.createTextFile(fileName, '', currentFolderId.value)
    ElMessage.success('创建成功')
    fileDialogVisible.value = false
    loadFiles()
    // Open the new file for editing
    if (res.data) {
      openFile(res.data)
    }
  } catch (error) {
    ElMessage.error('创建失败')
  } finally {
    creatingFile.value = false
  }
}

// Upload
const beforeUpload = (file: File) => {
  const maxSize = 100 * 1024 * 1024 // 100MB
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过 100MB')
    return false
  }
  return true
}

const handleUpload = async (options: any) => {
  try {
    await fileApi.upload(options.file, currentFolderId.value)
    ElMessage.success('上传成功')
    uploadFileList.value = []
    loadFiles()
  } catch (error) {
    ElMessage.error('上传失败')
  }
}

const handleRemove = () => {
  uploadFileList.value = []
}

// Download
const downloadFile = async (row: StudyFile) => {
  try {
    const response = await fileApi.download(row.id)
    const blob = new Blob([response.data])
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = row.fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(link.href)
    ElMessage.success('下载成功')
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

// Delete
const deleteFile = async (row: StudyFile) => {
  try {
    await ElMessageBox.confirm(`确定要删除 "${row.fileName}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await fileApi.delete(row.id)
    ElMessage.success('删除成功')
    loadFiles()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// Move
const showMoveDialog = (row: StudyFile) => {
  movingFile.value = row
  moveTargetId.value = row.parentId || 0
  loadAllFolders()
  moveDialogVisible.value = true
}

const confirmMove = async () => {
  if (!movingFile.value) return
  moving.value = true
  try {
    await fileApi.moveFile(movingFile.value.id, moveTargetId.value)
    ElMessage.success('移动成功')
    moveDialogVisible.value = false
    loadFiles()
  } catch (error) {
    ElMessage.error('移动失败')
  } finally {
    moving.value = false
  }
}

onMounted(() => {
  loadFiles()
})
</script>

<style scoped>
.file-manager {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  flex: 1;
}

.current-path {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.path-text {
  color: #1890ff;
}

.breadcrumb {
  font-size: 14px;
}

.actions {
  display: flex;
  gap: 10px;
}

.file-name-cell {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.file-icon {
  font-size: 20px;
  margin-right: 8px;
}

.file-icon.folder {
  color: #f4a460;
}

.file-icon.file {
  color: #909399;
}

.file-name {
  transition: color 0.2s;
}

.file-name.editable:hover {
  color: #1890ff;
  text-decoration: underline;
}

:deep(.el-breadcrumb__item) {
  cursor: pointer;
}

:deep(.el-breadcrumb__item:hover) {
  color: #1890ff;
}

/* Editor styles */
.editor-container {
  padding: 20px;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.editor-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.editor-title .filename {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.editor-body {
  display: flex;
  gap: 20px;
  height: calc(100vh - 200px);
}

.editor-pane,
.preview-pane {
  flex: 1;
  display: flex;
  flex-direction: column;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.pane-title {
  padding: 10px 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #dcdfe6;
  font-weight: bold;
  color: #606266;
}

.markdown-input :deep(.el-textarea__inner) {
  border: none;
  border-radius: 0;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
  resize: none;
  height: 100%;
}

.markdown-preview {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  background: #fff;
}

.markdown-preview :deep(h1) { font-size: 2em; margin: 0.67em 0; border-bottom: 1px solid #eaecef; padding-bottom: 0.3em; }
.markdown-preview :deep(h2) { font-size: 1.5em; margin: 0.75em 0; border-bottom: 1px solid #eaecef; padding-bottom: 0.3em; }
.markdown-preview :deep(h3) { font-size: 1.25em; margin: 0.83em 0; }
.markdown-preview :deep(p) { margin: 1em 0; line-height: 1.6; }
.markdown-preview :deep(code) { background: #f6f8fa; padding: 0.2em 0.4em; border-radius: 3px; font-family: monospace; }
.markdown-preview :deep(pre) { background: #f6f8fa; padding: 16px; border-radius: 6px; overflow-x: auto; }
.markdown-preview :deep(pre code) { background: none; padding: 0; }
.markdown-preview :deep(blockquote) { border-left: 4px solid #dfe2e5; padding-left: 16px; margin: 0; color: #6a737d; }
.markdown-preview :deep(ul), .markdown-preview :deep(ol) { padding-left: 2em; }
.markdown-preview :deep(table) { border-collapse: collapse; width: 100%; margin: 1em 0; }
.markdown-preview :deep(th), .markdown-preview :deep(td) { border: 1px solid #dfe2e5; padding: 8px 12px; }
.markdown-preview :deep(th) { background: #f6f8fa; }
.markdown-preview :deep(img) { max-width: 100%; }
.markdown-preview :deep(a) { color: #0366d6; text-decoration: none; }

/* Move dialog folder tree */
.folder-tree {
  max-height: 300px;
  overflow-y: auto;
}

.folder-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  cursor: pointer;
  border-radius: 4px;
  margin-bottom: 4px;
}

.folder-item:hover {
  background: #f5f7fa;
}

.folder-item.selected {
  background: #ecf5ff;
  color: #409eff;
}

.folder-item.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* View dialog styles */
.view-dialog-content {
  min-height: 200px;
  max-height: 500px;
  overflow-y: auto;
}

.view-preview {
  padding: 16px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.view-preview :deep(h1) { font-size: 2em; margin: 0.67em 0; border-bottom: 1px solid #eaecef; padding-bottom: 0.3em; }
.view-preview :deep(h2) { font-size: 1.5em; margin: 0.75em 0; border-bottom: 1px solid #eaecef; padding-bottom: 0.3em; }
.view-preview :deep(h3) { font-size: 1.25em; margin: 0.83em 0; }
.view-preview :deep(p) { margin: 1em 0; line-height: 1.6; }
.view-preview :deep(code) { background: #f6f8fa; padding: 0.2em 0.4em; border-radius: 3px; font-family: monospace; }
.view-preview :deep(pre) { background: #f6f8fa; padding: 16px; border-radius: 6px; overflow-x: auto; }
.view-preview :deep(pre code) { background: none; padding: 0; }
.view-preview :deep(blockquote) { border-left: 4px solid #dfe2e5; padding-left: 16px; margin: 0; color: #6a737d; }
.view-preview :deep(ul), .view-preview :deep(ol) { padding-left: 2em; }
.view-preview :deep(table) { border-collapse: collapse; width: 100%; margin: 1em 0; }
.view-preview :deep(th), .view-preview :deep(td) { border: 1px solid #dfe2e5; padding: 8px 12px; }
.view-preview :deep(th) { background: #f6f8fa; }
.view-preview :deep(img) { max-width: 100%; }
.view-preview :deep(a) { color: #0366d6; text-decoration: none; }

.view-plain {
  padding: 16px;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
  max-height: 500px;
  overflow-y: auto;
}
</style>
