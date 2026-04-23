package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.ChatSession;
import com.example.demo.mapper.ChatMessageMapper;
import com.example.demo.mapper.ChatSessionMapper;
import com.example.demo.service.ChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements ChatService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${ai.dashscope.api-key:}")
    private String dashscopeApiKey;

    @Value("${ai.dashscope.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String dashscopeBaseUrl;

    @Override
    public List<ChatSession> listSessions(Long userId) {
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSession::getUserId, userId)
                .orderByDesc(ChatSession::getCreateTime);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public ChatSession createSession(String sessionName, String model, String systemPrompt, Long userId) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setSessionName(sessionName);
        session.setModel(model != null ? model : "qwen-turbo");
        session.setSystemPrompt(systemPrompt);
        baseMapper.insert(session);
        return session;
    }

    @Override
    public boolean updateSession(Long sessionId, String sessionName, String model, String systemPrompt, Long userId) {
        ChatSession session = baseMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            return false;
        }
        if (sessionName != null) session.setSessionName(sessionName);
        if (model != null) session.setModel(model);
        if (systemPrompt != null) session.setSystemPrompt(systemPrompt);
        return baseMapper.updateById(session) > 0;
    }

    @Override
    public boolean deleteSession(Long sessionId, Long userId) {
        ChatSession session = baseMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            return false;
        }
        LambdaQueryWrapper<ChatMessage> msgWrapper = new LambdaQueryWrapper<>();
        msgWrapper.eq(ChatMessage::getSessionId, sessionId);
        chatMessageMapper.delete(msgWrapper);
        return baseMapper.deleteById(sessionId) > 0;
    }

    @Override
    public List<ChatMessage> listMessages(Long sessionId, Long userId) {
        ChatSession session = baseMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            return List.of();
        }
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
                .orderByAsc(ChatMessage::getCreateTime);
        return chatMessageMapper.selectList(wrapper);
    }

    @Override
    public ChatMessage sendMessage(Long sessionId, String content, Long userId) {
        ChatSession session = baseMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            return null;
        }

        // Save user message
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(content);
        chatMessageMapper.insert(userMsg);

        // Call AI API
        String aiReply = callDashScope(session, content);

        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSessionId(sessionId);
        aiMsg.setRole("assistant");
        aiMsg.setContent(aiReply);
        chatMessageMapper.insert(aiMsg);

        return aiMsg;
    }

    private String callDashScope(ChatSession session, String newMessage) {
        if (dashscopeApiKey == null || dashscopeApiKey.isBlank()) {
            return "AI API Key is not configured. Please set `ai.dashscope.api-key` in application.yml.";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(dashscopeApiKey);

            List<Map<String, String>> messages = new ArrayList<>();

            // Add system prompt if configured
            if (session.getSystemPrompt() != null && !session.getSystemPrompt().isBlank()) {
                Map<String, String> sysMsg = new HashMap<>();
                sysMsg.put("role", "system");
                sysMsg.put("content", session.getSystemPrompt());
                messages.add(sysMsg);
            }

            // Add history messages (last 20 to keep token count reasonable)
            LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ChatMessage::getSessionId, session.getId())
                    .orderByAsc(ChatMessage::getCreateTime);
            List<ChatMessage> history = chatMessageMapper.selectList(wrapper);
            // Exclude the just-inserted user message (last one)
            int limit = Math.max(0, history.size() - 1);
            List<ChatMessage> context = history.subList(0, limit);
            // Keep last 20 messages for context
            if (context.size() > 20) {
                context = context.subList(context.size() - 20, context.size());
            }
            for (ChatMessage msg : context) {
                Map<String, String> m = new HashMap<>();
                m.put("role", msg.getRole());
                m.put("content", msg.getContent());
                messages.add(m);
            }

            // Add current user message
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", newMessage);
            messages.add(userMsg);

            Map<String, Object> body = new HashMap<>();
            body.put("model", session.getModel());
            body.put("messages", messages);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            String url = dashscopeBaseUrl + "/chat/completions";

            String response = restTemplate.postForObject(url, request, String.class);

            JsonNode root = objectMapper.readTree(response);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).path("message");
                return message.path("content").asText("No response content");
            }
            return "Empty response from AI";
        } catch (Exception e) {
            return "AI service error: " + e.getMessage();
        }
    }
}
