package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.entity.Friend;
import com.example.demo.entity.FriendRequest;
import com.example.demo.entity.PrivateMessage;
import com.example.demo.entity.User;
import com.example.demo.mapper.FriendMapper;
import com.example.demo.mapper.FriendRequestMapper;
import com.example.demo.mapper.PrivateMessageMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.FriendService;
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
public class FriendServiceImpl implements FriendService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FriendRequestMapper friendRequestMapper;

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private PrivateMessageMapper privateMessageMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${ai.dashscope.api-key:}")
    private String dashscopeApiKey;

    @Value("${ai.dashscope.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String dashscopeBaseUrl;

    @Override
    public List<User> searchUsers(String keyword, Long currentUserId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.eq(User::getId, keyword)
                    .or()
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getNickname, keyword));
        }
        wrapper.ne(User::getId, currentUserId);
        return userMapper.selectList(wrapper);
    }

    @Override
    public void sendFriendRequest(Long fromUserId, Long toUserId) {
        // Check if already friends
        LambdaQueryWrapper<Friend> friendWrapper = new LambdaQueryWrapper<>();
        friendWrapper.eq(Friend::getUserId, fromUserId)
                .eq(Friend::getFriendUserId, toUserId);
        if (friendMapper.selectCount(friendWrapper) > 0) {
            throw new RuntimeException("Already friends");
        }

        // Check if request already exists
        LambdaQueryWrapper<FriendRequest> reqWrapper = new LambdaQueryWrapper<>();
        reqWrapper.eq(FriendRequest::getFromUserId, fromUserId)
                .eq(FriendRequest::getToUserId, toUserId)
                .eq(FriendRequest::getStatus, 0);
        if (friendRequestMapper.selectCount(reqWrapper) > 0) {
            throw new RuntimeException("Friend request already sent");
        }

        FriendRequest request = new FriendRequest();
        request.setFromUserId(fromUserId);
        request.setToUserId(toUserId);
        request.setStatus(0);
        friendRequestMapper.insert(request);
    }

    @Override
    public List<FriendRequest> listFriendRequests(Long userId) {
        LambdaQueryWrapper<FriendRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendRequest::getToUserId, userId)
                .eq(FriendRequest::getStatus, 0)
                .orderByDesc(FriendRequest::getCreateTime);
        return friendRequestMapper.selectList(wrapper);
    }

    @Override
    public void acceptFriendRequest(Long requestId, Long userId) {
        FriendRequest request = friendRequestMapper.selectById(requestId);
        if (request == null || !request.getToUserId().equals(userId)) {
            throw new RuntimeException("Request not found");
        }
        request.setStatus(1);
        friendRequestMapper.updateById(request);

        // Create bidirectional friend relationship
        Friend f1 = new Friend();
        f1.setUserId(request.getFromUserId());
        f1.setFriendUserId(request.getToUserId());
        friendMapper.insert(f1);

        Friend f2 = new Friend();
        f2.setUserId(request.getToUserId());
        f2.setFriendUserId(request.getFromUserId());
        friendMapper.insert(f2);
    }

    @Override
    public void rejectFriendRequest(Long requestId, Long userId) {
        FriendRequest request = friendRequestMapper.selectById(requestId);
        if (request == null || !request.getToUserId().equals(userId)) {
            throw new RuntimeException("Request not found");
        }
        request.setStatus(2);
        friendRequestMapper.updateById(request);
    }

    @Override
    public List<Friend> listFriends(Long userId) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, userId)
                .orderByDesc(Friend::getCreateTime);
        return friendMapper.selectList(wrapper);
    }

    @Override
    public PrivateMessage sendMessage(Long senderId, Long receiverId, String content) {
        PrivateMessage message = new PrivateMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        privateMessageMapper.insert(message);
        return message;
    }

    @Override
    public List<PrivateMessage> listMessages(Long userId, Long friendId) {
        LambdaQueryWrapper<PrivateMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(PrivateMessage::getSenderId, userId).eq(PrivateMessage::getReceiverId, friendId)
                .or()
                .eq(PrivateMessage::getSenderId, friendId).eq(PrivateMessage::getReceiverId, userId))
                .orderByAsc(PrivateMessage::getCreateTime);
        return privateMessageMapper.selectList(wrapper);
    }

    @Override
    public String aiSuggestReply(Long userId, Long friendId) {
        if (dashscopeApiKey == null || dashscopeApiKey.isBlank()) {
            return "AI API Key is not configured.";
        }

        List<PrivateMessage> messages = listMessages(userId, friendId);
        if (messages.isEmpty()) {
            return "No conversation history yet. Start chatting first!";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(dashscopeApiKey);

            // Build conversation context
            StringBuilder context = new StringBuilder();
            context.append("You are a helpful assistant that drafts natural, friendly replies based on conversation history.\n\n");
            context.append("Here is the conversation history between two people (Me and My Friend):\n\n");

            int start = Math.max(0, messages.size() - 20);
            for (int i = start; i < messages.size(); i++) {
                PrivateMessage msg = messages.get(i);
                String role = msg.getSenderId().equals(userId) ? "Me" : "Friend";
                context.append(role).append(": ").append(msg.getContent()).append("\n");
            }

            context.append("\nPlease draft a natural, contextually appropriate reply for Me. Only output the reply text, no explanations.");

            List<Map<String, String>> apiMessages = new ArrayList<>();
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", "You are a conversation assistant. Generate a natural, friendly reply based on the conversation context. Output only the reply text, no explanations or prefixes.");
            apiMessages.add(systemMsg);

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", context.toString());
            apiMessages.add(userMsg);

            Map<String, Object> body = new HashMap<>();
            body.put("model", "qwen-turbo");
            body.put("messages", apiMessages);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            String url = dashscopeBaseUrl + "/chat/completions";

            String response = restTemplate.postForObject(url, request, String.class);

            JsonNode root = objectMapper.readTree(response);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).path("message");
                return message.path("content").asText("No response content").trim();
            }
            return "Empty response from AI";
        } catch (Exception e) {
            return "AI service error: " + e.getMessage();
        }
    }
}
