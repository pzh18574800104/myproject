-- 好友与私聊模块数据库迁移
USE myproject;

-- 好友申请表
CREATE TABLE IF NOT EXISTS friend_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    from_user_id BIGINT NOT NULL,
    to_user_id BIGINT NOT NULL,
    status TINYINT DEFAULT 0 COMMENT '0-pending, 1-accepted, 2-rejected',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_from_to (from_user_id, to_user_id)
);

-- 好友关系表（双向存储，方便查询）
CREATE TABLE IF NOT EXISTS friend (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    friend_user_id BIGINT NOT NULL,
    remark_name VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_friend (user_id, friend_user_id)
);

-- 私聊消息表
CREATE TABLE IF NOT EXISTS private_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 插入菜单（好友聊天模块）
INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order) VALUES 
(0, '好友聊天', 'friends', '/friends', NULL, 'UserFilled', 3);

SET @friends_parent_id = LAST_INSERT_ID();

INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order) VALUES 
(@friends_parent_id, '好友列表', 'friends:list', '/friends/list', 'friends/Friends', 'User', 1);

-- 关联管理员角色
INSERT INTO sys_role_menu (role_id, menu_id) 
SELECT 1, id FROM sys_menu WHERE menu_code IN ('friends', 'friends:list');
