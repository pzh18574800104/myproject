package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.ChatSession;
import com.example.demo.service.ChatService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/session")
    public Result<List<ChatSession>> listSessions(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<ChatSession> sessions = chatService.listSessions(userId);
        return Result.success(sessions);
    }

    @PostMapping("/session")
    public Result<ChatSession> createSession(
            @RequestBody CreateSessionRequest req,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        ChatSession session = chatService.createSession(
                req.getSessionName(), req.getModel(), req.getSystemPrompt(), userId);
        return Result.success(session);
    }

    @PutMapping("/session/{sessionId}")
    public Result<Void> updateSession(
            @PathVariable Long sessionId,
            @RequestBody CreateSessionRequest req,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean success = chatService.updateSession(
                sessionId, req.getSessionName(), req.getModel(), req.getSystemPrompt(), userId);
        if (success) {
            return Result.success();
        }
        return Result.error("更新失败");
    }

    @DeleteMapping("/session/{sessionId}")
    public Result<Void> deleteSession(
            @PathVariable Long sessionId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean success = chatService.deleteSession(sessionId, userId);
        if (success) {
            return Result.success();
        }
        return Result.error("删除失败");
    }

    @GetMapping("/session/{sessionId}/message")
    public Result<List<ChatMessage>> listMessages(
            @PathVariable Long sessionId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<ChatMessage> messages = chatService.listMessages(sessionId, userId);
        return Result.success(messages);
    }

    @PostMapping("/session/{sessionId}/message")
    public Result<ChatMessage> sendMessage(
            @PathVariable Long sessionId,
            @RequestBody SendMessageRequest req,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        ChatMessage aiMsg = chatService.sendMessage(sessionId, req.getContent(), userId);
        if (aiMsg != null) {
            return Result.success(aiMsg);
        }
        return Result.error("发送失败");
    }

    public static class CreateSessionRequest {
        private String sessionName;
        private String model;
        private String systemPrompt;

        public String getSessionName() {
            return sessionName;
        }

        public void setSessionName(String sessionName) {
            this.sessionName = sessionName;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getSystemPrompt() {
            return systemPrompt;
        }

        public void setSystemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
        }
    }

    public static class SendMessageRequest {
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
