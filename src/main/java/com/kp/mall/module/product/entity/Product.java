package com.kp.mall.module.product.entity;

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
 * @description: 商品实体类
 */
@Data
@TableName("product")
public class Product implements Serializable {

    private static final long serialVersionUID=1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 商品编号  */
    private String productCode;
    /** 商品名称  */
    private String productName;
    /** 所属分类  */
    private Long categoryId;
    /** 商品类型  */
    private Integer productType;
    /** 商品封面  */
    private String coverImg;
    /** 商品图片 */
    private String productImgs;
    /** 商品价格  */
    private BigDecimal price;
    /** 兑换所需积分  */
    private Integer integral;
    /** 商品库存  */
    private Integer stock;
    /** 销量  */
    private Integer salesCount;
    /** 商品描述  */
    private String productDesc;
    /** 状态  */
    private Integer status;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /** 分类名称  */
    @TableField(exist = false)
    private String categoryName;


}
