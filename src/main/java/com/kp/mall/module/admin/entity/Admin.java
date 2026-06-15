package com.kp.mall.module.admin.entity;

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
 * @description: 管理员实体类
 */
@Data
@TableName("admin")
public class Admin implements Serializable {

    private static final long serialVersionUID=1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 管理员账号  */
    private String adminAccount;
    /** 头像  */
    private String avatar;
    /** 性别  */
    private Integer gender;
    /** 管理员密码 */
    private String adminPassword;
    /** 管理员姓名  */
    private String adminName;
    /** 管理员角色 */
    private String roleType;
    /** 管理员手机号 */
    private String adminPhone;
    /** 管理员邮箱 */
    private String adminEmail;
    /** 状态  */
    private Integer status;
    /** 最后登录时间  */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


}
