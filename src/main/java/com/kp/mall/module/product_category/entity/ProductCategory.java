package com.kp.mall.module.product_category.entity;

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
 * @description: 商品分类实体类
 */
@Data
@TableName("product_category")
public class ProductCategory implements Serializable {

    private static final long serialVersionUID=1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 分类名称  */
    private String categoryName;
    /** 分类描述  */
    private String categoryDesc;
    /** 排序值  */
    private Integer sort;
    /** 状态  */
    private Integer status;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


}
