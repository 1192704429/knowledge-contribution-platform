package com.kp.mall.module.product.controller;

import com.kp.mall.common.Result;
import com.kp.mall.module.product.entity.Product;
import com.kp.mall.module.product.service.ProductService;
import com.kp.mall.module.product_category.entity.ProductCategory;
import com.kp.mall.module.product_category.service.ProductCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author wuming
 * @description: 用户端商品Controller
 */
@RestController
@RequestMapping("/user/product")
public class UserProductController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private ProductCategoryService productCategoryService;

    /**
     * 分页查询商品列表（用户端，只返回状态正常的上架商品）
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) Map<String, Object> params) {
        Product entity = new Product();
        Integer current = 1;
        Integer size = 8; // 默认一页8个
        
        if (params != null) {
            // 提取分页参数
            if (params.get("current") != null) {
                current = Integer.valueOf(params.get("current").toString());
            }
            if (params.get("size") != null) {
                size = Integer.valueOf(params.get("size").toString());
            }
            // 提取查询条件
            if (params.get("categoryId") != null) {
                entity.setCategoryId(Long.valueOf(params.get("categoryId").toString()));
            }
            if (params.get("productName") != null) {
                entity.setProductName(params.get("productName").toString());
            }
        }
        
        // 用户端只显示状态正常的上架商品
        entity.setStatus(1);
        return Result.success(productService.pageQuery(current, size, entity));
    }

    /**
     * 根据ID查询商品详情（用户端）
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        // 检查商品状态
        if (product.getStatus() == null || product.getStatus() != 1) {
            return Result.error("商品不存在或已下架");
        }
        return Result.success(product);
    }

    /**
     * 获取商品分类列表（用户端，只返回状态正常的）
     */
    @GetMapping("/category/list")
    public Result listCategory() {
        ProductCategory category = new ProductCategory();
        category.setStatus(1); // 只查询状态正常的分类
        List<ProductCategory> list = productCategoryService.listQuery(category);
        return Result.success(list);
    }
}
