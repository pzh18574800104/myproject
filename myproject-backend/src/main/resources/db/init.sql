-- 创建数据库
CREATE DATABASE IF NOT EXISTS myproject CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE myproject;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    status TINYINT DEFAULT 1,
    deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 菜单表
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0,
    menu_name VARCHAR(50) NOT NULL,
    menu_code VARCHAR(50),
    path VARCHAR(100),
    component VARCHAR(100),
    icon VARCHAR(50),
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
);

-- 学习模块-文件表
CREATE TABLE IF NOT EXISTS study_file (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500),
    file_type VARCHAR(20),
    file_size BIGINT,
    content TEXT,
    parent_id BIGINT DEFAULT 0,
    is_folder TINYINT DEFAULT 0,
    deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- AI对话会话表
CREATE TABLE IF NOT EXISTS chat_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    session_name VARCHAR(100),
    model VARCHAR(50) DEFAULT 'qwen-turbo',
    system_prompt TEXT,
    deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- AI对话消息表
CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 初始化数据
-- 插入默认管理员用户 (密码: admin123)
INSERT INTO sys_user (username, password, nickname, status) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '管理员', 1);

-- 插入默认角色
INSERT INTO sys_role (role_name, role_code, description) VALUES 
('超级管理员', 'ROLE_ADMIN', '拥有所有权限'),
('普通用户', 'ROLE_USER', '普通用户权限');

-- 插入菜单
INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order) VALUES 
(0, '学习模块', 'study', '/study', NULL, 'Document', 1),
(0, '摸鱼模块', 'chat', '/chat', NULL, 'ChatDotRound', 2),
(0, '系统管理', 'system', '/system', NULL, 'Setting', 99),
(1, '文件管理', 'study:file', '/study/file', 'study/FileManager', 'Folder', 1),
(1, 'Markdown编辑', 'study:md', '/study/md', 'study/MarkdownEditor', 'Edit', 2),
(2, 'AI对话', 'chat:ai', '/chat/ai', 'chat/AIChat', 'ChatLineRound', 1),
(3, '用户管理', 'system:user', '/system/user', 'system/UserManage', 'User', 1),
(3, '角色管理', 'system:role', '/system/role', 'system/RoleManage', 'UserFilled', 2),
(3, '菜单管理', 'system:menu', '/system/menu', 'system/MenuManage', 'Menu', 3);

-- 关联管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 关联所有菜单给管理员角色
INSERT INTO sys_role_menu (role_id, menu_id) 
SELECT 1, id FROM sys_menu;
