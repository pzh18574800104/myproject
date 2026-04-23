<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="welcome-card">
          <h2>欢迎使用 MyProject</h2>
          <p>这是一个个人项目管理系统，包含学习模块和摸鱼模块。</p>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="mt-20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <el-icon><Document /></el-icon>
              <span>学习模块</span>
            </div>
          </template>
          <p>在线编辑 Markdown 文件</p>
          <p>文件上传和下载</p>
          <p>在线浏览 MD 和 PDF 文件</p>
          <el-button type="primary" @click="$router.push('/study/file')">进入学习模块</el-button>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <el-icon><ChatDotRound /></el-icon>
              <span>摸鱼模块</span>
            </div>
          </template>
          <p>接入通义千问 AI 对话</p>
          <p>存储对话内容</p>
          <p>AI 有记忆功能</p>
          <el-button type="primary" @click="$router.push('/chat/ai')">进入摸鱼模块</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.welcome-card {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: #fff;
}

.welcome-card h2 {
  margin: 0 0 10px 0;
}

.welcome-card p {
  margin: 0;
  opacity: 0.9;
}

.mt-20 {
  margin-top: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}
</style>
