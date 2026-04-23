package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("friend_request")
public class FriendRequest extends BaseEntity {
    private Long fromUserId;
    private Long toUserId;
    private Integer status; // 0-pending, 1-accepted, 2-rejected

    @TableField(exist = false)
    private Integer deleted;
}
