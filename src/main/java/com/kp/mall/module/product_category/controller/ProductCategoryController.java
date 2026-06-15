package com.kp.mall.module.product_category.controller;

import com.kp.mall.common.Result;
import com.kp.mall.module.product_category.entity.ProductCategory;
import com.kp.mall.module.product_category.service.ProductCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author wuming
 * @description: 商品分类Controller
 */
@RestController
@RequestMapping("/product_category")
public class ProductCategoryController {

    @Autowired
    private ProductCategoryService productCategoryService;

    /**
     * 分页查询商品分类列表
     */
    @PostMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestBody(required = false) ProductCategory entity) {
        if (entity == null) {
            entity = new ProductCategory();
        }
        return Result.success(productCategoryService.pageQuery(current, size, entity));
    }

    /**
     * 查询所有商品分类列表
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) ProductCategory entity) {
        if (entity == null) {
            entity = new ProductCategory();
        }
        List<ProductCategory> list = productCategoryService.listQuery(entity);
        return Result.success(list);
    }

    /**
     * 根据ID查询商品分类
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        ProductCategory productCategory = productCategoryService.getById(id);
        if (productCategory == null) {
            return Result.error("商品分类不存在");
        }
        return Result.success(productCategory);
    }

    /**
     * 新增商品分类
     */
    @PostMapping
    public Result save(@RequestBody ProductCategory productCategory) {
        productCategory.setCreateTime(LocalDateTime.now());
        boolean success = productCategoryService.save(productCategory);
        if (success) {
            return Result.success("新增成功", productCategory);
        }
        return Result.error("新增失败");
    }

    /**
     * 更新商品分类
     */
    @PutMapping
    public Result update(@RequestBody ProductCategory productCategory) {
        boolean success = productCategoryService.updateById(productCategory);
        if (success) {
            return Result.success("更新成功", productCategory);
        }
        return Result.error("更新失败");
    }

    /**
     * 根据ID删除商品分类
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean success = productCategoryService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除商品分类
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean success = productCategoryService.removeByIds(ids);
        if (success) {
            return Result.success("批量删除成功");
        }
        return Result.error("批量删除失败");
    }

}
