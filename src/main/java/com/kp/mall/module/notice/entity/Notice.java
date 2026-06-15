package com.kp.mall.module.notice.entity;

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
 * @description: 公告实体类
 */
@Data
@TableName("notice")
public class Notice implements Serializable {

    private static final long serialVersionUID=1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 公告标题  */
    private String title;
    /** 公告内容  */
    private String content;
    /** 发布人 */
    private Long publisherId;
    /** 发布时间   */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;
    /** 状态  */
    private Integer status;
    /** 阅读量  */
    private Integer readCount;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /** 管理员姓名  */
    @TableField(exist = false)
    private String adminName;

    // 范围查询字段
    /** 发布时间   开始 */
    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTimeStart;
    /** 发布时间   结束 */
    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTimeEnd;

}
