package com.kp.mall.module.answer.entity;

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
 * @description: 问答回答实体类
 */
@Data
@TableName("answer")
public class Answer implements Serializable {

    private static final long serialVersionUID=1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 关联问答  */
    private Long questionId;
    /** 回答内容  */
    private String content;
    /** 回答用户 */
    private Long userId;
    /** 回答评级  */
    private Integer answerGrade;
    /** 审核状态  */
    private Integer auditStatus;
    /** 审核备注  */
    private String auditRemark;
    /** 审核时间  */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;
    /** 审核管理员 */
    private Long auditAdminId;
    /** 状态  */
    private Integer status;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /** 问答标题  */
    @TableField(exist = false)
    private String title;
    /** 用户姓名  */
    @TableField(exist = false)
    private String userName;
    /** 管理员姓名  */
    @TableField(exist = false)
    private String adminName;
    /** 用户头像  */
    @TableField(exist = false)
    private String avatar;


}
