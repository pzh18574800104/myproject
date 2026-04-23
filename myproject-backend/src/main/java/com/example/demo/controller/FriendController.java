package com.example.demo.controller;

import com.example.demo.entity.Friend;
import com.example.demo.entity.FriendRequest;
import com.example.demo.entity.PrivateMessage;
import com.example.demo.entity.User;
import com.example.demo.service.FriendService;
import com.example.demo.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/friend")
public class FriendController {

    @Autowired
    private FriendService friendService;

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    @GetMapping("/search")
    public Result<List<User>> searchUsers(@RequestParam String keyword, HttpServletRequest request) {
        Long userId = getUserId(request);
        return Result.success(friendService.searchUsers(keyword, userId));
    }

    @PostMapping("/request")
    public Result<Void> sendFriendRequest(@RequestParam Long toUserId, HttpServletRequest request) {
        Long userId = getUserId(request);
        friendService.sendFriendRequest(userId, toUserId);
        return Result.success();
    }

    @GetMapping("/request")
    public Result<List<FriendRequest>> listFriendRequests(HttpServletRequest request) {
        Long userId = getUserId(request);
        return Result.success(friendService.listFriendRequests(userId));
    }

    @PostMapping("/request/{requestId}/accept")
    public Result<Void> acceptFriendRequest(@PathVariable Long requestId, HttpServletRequest request) {
        Long userId = getUserId(request);
        friendService.acceptFriendRequest(requestId, userId);
        return Result.success();
    }

    @PostMapping("/request/{requestId}/reject")
    public Result<Void> rejectFriendRequest(@PathVariable Long requestId, HttpServletRequest request) {
        Long userId = getUserId(request);
        friendService.rejectFriendRequest(requestId, userId);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<FriendVO>> listFriends(HttpServletRequest request) {
        Long userId = getUserId(request);
        List<Friend> friends = friendService.listFriends(userId);
        List<FriendVO> result = new ArrayList<>();
        for (Friend f : friends) {
            User u = friendService.searchUsers(String.valueOf(f.getFriendUserId()), userId).stream()
                    .findFirst().orElse(null);
            FriendVO vo = new FriendVO();
            vo.setId(f.getId());
            vo.setFriendUserId(f.getFriendUserId());
            vo.setRemarkName(f.getRemarkName());
            vo.setCreateTime(f.getCreateTime());
            if (u != null) {
                vo.setUsername(u.getUsername());
                vo.setNickname(u.getNickname());
                vo.setAvatar(u.getAvatar());
            }
            result.add(vo);
        }
        return Result.success(result);
    }

    public static class FriendVO {
        private Long id;
        private Long friendUserId;
        private String remarkName;
        private String username;
        private String nickname;
        private String avatar;
        private java.time.LocalDateTime createTime;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getFriendUserId() { return friendUserId; }
        public void setFriendUserId(Long friendUserId) { this.friendUserId = friendUserId; }
        public String getRemarkName() { return remarkName; }
        public void setRemarkName(String remarkName) { this.remarkName = remarkName; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
        public java.time.LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(java.time.LocalDateTime createTime) { this.createTime = createTime; }
    }

    @GetMapping("/message/{friendId}")
    public Result<List<PrivateMessage>> listMessages(@PathVariable Long friendId, HttpServletRequest request) {
        Long userId = getUserId(request);
        return Result.success(friendService.listMessages(userId, friendId));
    }

    @PostMapping("/message")
    public Result<PrivateMessage> sendMessage(@RequestBody SendMessageRequest req, HttpServletRequest request) {
        Long userId = getUserId(request);
        PrivateMessage message = friendService.sendMessage(userId, req.getReceiverId(), req.getContent());
        return Result.success(message);
    }

    @PostMapping("/ai-suggest/{friendId}")
    public Result<String> aiSuggestReply(@PathVariable Long friendId, HttpServletRequest request) {
        Long userId = getUserId(request);
        return Result.success(friendService.aiSuggestReply(userId, friendId));
    }

    public static class SendMessageRequest {
        private Long receiverId;
        private String content;

        public Long getReceiverId() { return receiverId; }
        public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
