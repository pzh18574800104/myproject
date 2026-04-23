package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class Menu extends BaseEntity {
    private Long parentId;
    private String menuName;
    private String menuCode;
    private String path;
    private String component;
    private String icon;
    private Integer sortOrder;
    private Integer status;
}
