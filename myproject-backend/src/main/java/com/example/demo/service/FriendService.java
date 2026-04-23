package com.example.demo.service;

import com.example.demo.entity.Friend;
import com.example.demo.entity.FriendRequest;
import com.example.demo.entity.PrivateMessage;
import com.example.demo.entity.User;

import java.util.List;

public interface FriendService {
    List<User> searchUsers(String keyword, Long currentUserId);

    void sendFriendRequest(Long fromUserId, Long toUserId);

    List<FriendRequest> listFriendRequests(Long userId);

    void acceptFriendRequest(Long requestId, Long userId);

    void rejectFriendRequest(Long requestId, Long userId);

    List<Friend> listFriends(Long userId);

    PrivateMessage sendMessage(Long senderId, Long receiverId, String content);

    List<PrivateMessage> listMessages(Long userId, Long friendId);

    String aiSuggestReply(Long userId, Long friendId);
}
