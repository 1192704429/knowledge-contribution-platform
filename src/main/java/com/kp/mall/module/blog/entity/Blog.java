package com.kp.mall.module.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author wuming
 * @description: 博客实体类
 */
@Data
@TableName("blog")
public class Blog implements Serializable {

    private static final long serialVersionUID=1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 博客编号  */
    private String blogCode;
    /** 博客标题  */
    private String title;
    /** 博客内容  */
    private String content;
    /** 封面图片  */
    private String coverImg;
    /** 所属分类  */
    private Long categoryId;
    /** 发布用户 */
    private Long userId;
    /** 阅读量  */
    private Integer readCount;
    /** 审核状态  */
    private Integer auditStatus;
    /** 审核备注  */
    private String auditRemark;
    /** 审核时间  */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;
    /** 审核管理员 */
    private Long auditAdminId;
    /** 发布时间  */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;
    /** 状态  */
    private Integer status;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /** 分类名称  */
    @TableField(exist = false)
    private String categoryName;
    /** 用户姓名  */
    @TableField(exist = false)
    private String userName;
    /** 管理员姓名  */
    @TableField(exist = false)
    private String adminName;


}
