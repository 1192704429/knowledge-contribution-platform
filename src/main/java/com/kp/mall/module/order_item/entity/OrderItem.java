package com.kp.mall.module.order_item.entity;

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
 * @description: 订单明细实体类
 */
@Data
@TableName("order_item")
public class OrderItem implements Serializable {

    private static final long serialVersionUID=1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 关联订单  */
    private Long orderId;
    /** 商品 */
    private Long productId;
    /** 商品名称 */
    private String productName;
    /** 商品图片 */
    private String productImg;
    /** 商品单价  */
    private BigDecimal productPrice;
    /** 购买数量  */
    private Integer productNum;
    /** 商品所需积分  */
    private Integer productIntegral;
    /** 小计金额  */
    private BigDecimal itemAmount;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /** 订单编号  */
    @TableField(exist = false)
    private String orderCode;
    /** 商品名称  */
    @TableField(exist = false)
    private String productName2;


}
