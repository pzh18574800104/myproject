package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.ChatSession;

import java.util.List;

public interface ChatService extends IService<ChatSession> {

    List<ChatSession> listSessions(Long userId);

    ChatSession createSession(String sessionName, String model, String systemPrompt, Long userId);

    boolean updateSession(Long sessionId, String sessionName, String model, String systemPrompt, Long userId);

    boolean deleteSession(Long sessionId, Long userId);

    List<ChatMessage> listMessages(Long sessionId, Long userId);

    ChatMessage sendMessage(Long sessionId, String content, Long userId);
}
