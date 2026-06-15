package com.kp.mall.module.shopping_cart.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author wuming
 * @description: 购物车实体类
 */
@Data
@TableName("shopping_cart")
public class ShoppingCart implements Serializable {

    private static final long serialVersionUID=1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 用户  */
    private Long userId;
    /** 商品  */
    private Long productId;
    /** 商品数量  */
    private Integer productNum;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /** 用户姓名  */
    @TableField(exist = false)
    private String userName;
    /** 商品名称  */
    @TableField(exist = false)
    private String productName;
    /** 商品封面图（关联查询） */
    @TableField(exist = false)
    private String productCoverImg;
    /** 商品价格（关联查询） */
    @TableField(exist = false)
    private BigDecimal productPrice;
    /** 商品兑换积分（关联查询） */
    @TableField(exist = false)
    private Integer productIntegral;
    /** 商品库存（关联查询） */
    @TableField(exist = false)
    private Integer productStock;

}
