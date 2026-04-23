-- Migration script for existing chat tables
-- Run this if you already have the chat_session and chat_message tables created

ALTER TABLE chat_session ADD COLUMN model VARCHAR(50) DEFAULT 'qwen-turbo' AFTER session_name;
ALTER TABLE chat_session ADD COLUMN system_prompt TEXT AFTER model;
