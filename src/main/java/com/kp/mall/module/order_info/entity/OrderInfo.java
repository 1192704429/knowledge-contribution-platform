package com.kp.mall.module.order_info.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * @author wuming
 * @description: 订单实体类
 */
@Data
@TableName("order_info")
public class OrderInfo implements Serializable {

    private static final long serialVersionUID=1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 订单编号  */
    private String orderCode;
    /** 用户  */
    private Long userId;
    /** 订单金额  */
    private BigDecimal orderAmount;
    /** 实付金额  */
    private BigDecimal payAmount;
    /** 抵扣积分  */
    private Integer integralDeduct;
    /** 积分抵扣金额  */
    private BigDecimal integralAmount;
    /** 支付方式  */
    private Integer payType;
    /** 支付状态  */
    private Integer payStatus;
    /** 支付时间  */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;
    /** 支付订单号 */
    private String payOrderNo;
    /** 订单状态  */
    private Integer orderStatus;
    /** 收货人姓名 */
    private String receiverName;
    /** 收货人手机号 */
    private String receiverPhone;
    /** 收货地址 */
    private String receiverAddress;
    /** 物流单号 */
    private String deliveryNo;
    /** 发货时间  */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliveryTime;
    /** 收货时间  */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime receiveTime;
    /** 取消时间  */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;
    /** 取消原因  */
    private String cancelReason;
    /** 订单备注  */
    private String remark;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /** 用户姓名  */
    @TableField(exist = false)
    private String userName;


}
