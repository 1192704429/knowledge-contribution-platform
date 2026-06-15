package com.kp.mall.module.user.entity;

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
 * @description: 用户实体类
 */
@Data
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID=1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 用户账号  */
    private String userAccount;
    /** 用户密码 */
    private String userPassword;
    /** 用户姓名  */
    private String userName;
    /** 用户手机号 */
    private String userPhone;
    /** 用户邮箱  */
    private String userEmail;
    /** 用户头像  */
    private String avatar;
    /** 性别  */
    private Integer gender;
    /** 状态  */
    private Integer status;
    /** 总积分  */
    private Integer totalIntegral;
    /** 最后登录时间  */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


}
