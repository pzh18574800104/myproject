<template>
  <div class="menu-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>菜单管理</span>
          <el-button type="primary" :icon="Plus">新增菜单</el-button>
        </div>
      </template>
      
      <el-table :data="menuList" style="width: 100%" row-key="id" :tree-props="{ children: 'children' }">
        <el-table-column prop="menuName" label="菜单名称" />
        <el-table-column prop="menuCode" label="菜单编码" />
        <el-table-column prop="path" label="路径" />
        <el-table-column prop="component" label="组件" />
        <el-table-column prop="icon" label="图标" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="200">
          <template #default>
            <el-button link type="primary">编辑</el-button>
            <el-button link type="danger">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'

const menuList = ref([
  {
    id: 1,
    menuName: '学习模块',
    menuCode: 'study',
    path: '/study',
    component: null,
    icon: 'Document',
    sortOrder: 1,
    children: [
      { id: 4, menuName: '文件管理', menuCode: 'study:file', path: '/study/file', component: 'study/FileManager', icon: 'Folder', sortOrder: 1 },
      { id: 5, menuName: 'Markdown编辑', menuCode: 'study:md', path: '/study/md', component: 'study/MarkdownEditor', icon: 'Edit', sortOrder: 2 }
    ]
  },
  {
    id: 2,
    menuName: '摸鱼模块',
    menuCode: 'chat',
    path: '/chat',
    component: null,
    icon: 'ChatDotRound',
    sortOrder: 2,
    children: [
      { id: 6, menuName: 'AI对话', menuCode: 'chat:ai', path: '/chat/ai', component: 'chat/AIChat', icon: 'ChatLineRound', sortOrder: 1 }
    ]
  },
  {
    id: 3,
    menuName: '系统管理',
    menuCode: 'system',
    path: '/system',
    component: null,
    icon: 'Setting',
    sortOrder: 99,
    children: [
      { id: 7, menuName: '用户管理', menuCode: 'system:user', path: '/system/user', component: 'system/UserManage', icon: 'User', sortOrder: 1 },
      { id: 8, menuName: '角色管理', menuCode: 'system:role', path: '/system/role', component: 'system/RoleManage', icon: 'UserFilled', sortOrder: 2 },
      { id: 9, menuName: '菜单管理', menuCode: 'system:menu', path: '/system/menu', component: 'system/MenuManage', icon: 'Menu', sortOrder: 3 }
    ]
  }
])
</script>

<style scoped>
.menu-manage {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
