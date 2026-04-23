package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("private_message")
public class PrivateMessage extends BaseEntity {
    private Long senderId;
    private Long receiverId;
    private String content;

    @TableField(exist = false)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private Integer deleted;
}
