package com.kp.mall.module.integral_record.entity;

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
 * @description: 积分记录实体类
 */
@Data
@TableName("integral_record")
public class IntegralRecord implements Serializable {

    private static final long serialVersionUID=1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 用户  */
    private Long userId;
    /** 积分类型  */
    private Integer integralType;
    /** 积分来源  */
    private Integer integralSource;
    /** 积分数量  */
    private Integer integralNum;
    /** 关联业务ID */
    private Long relatedId;
    /** 关联业务描述 */
    private String relatedDesc;
    /** 积分余额  */
    private Integer balance;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /** 用户姓名  */
    @TableField(exist = false)
    private String userName;


}
