package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("friend")
public class Friend extends BaseEntity {
    private Long userId;
    private Long friendUserId;
    private String remarkName;

    @TableField(exist = false)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private Integer deleted;
}
