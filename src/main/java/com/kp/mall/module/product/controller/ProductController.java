package com.kp.mall.module.product.controller;

import com.kp.mall.common.Result;
import com.kp.mall.module.product.entity.Product;
import com.kp.mall.module.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import com.kp.mall.vo.SelectVO;

/**
 * @author wuming
 * @description: 商品Controller
 */
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 分页查询商品列表
     */
    @PostMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestBody(required = false) Product entity) {
        if (entity == null) {
            entity = new Product();
        }
        return Result.success(productService.pageQuery(current, size, entity));
    }

    /**
     * 查询所有商品列表
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) Product entity) {
        if (entity == null) {
            entity = new Product();
        }
        List<Product> list = productService.listQuery(entity);
        return Result.success(list);
    }

    /**
     * 根据ID查询商品
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        return Result.success(product);
    }

    /**
     * 新增商品
     */
    @PostMapping
    public Result save(@RequestBody Product product) {
        product.setCreateTime(LocalDateTime.now());
        // 如果商品编号为空，自动生成
        if (product.getProductCode() == null || product.getProductCode().trim().isEmpty()) {
            String productCode = "PR" + System.currentTimeMillis();
            product.setProductCode(productCode);
        }
        boolean success = productService.save(product);
        if (success) {
            return Result.success("新增成功", product);
        }
        return Result.error("新增失败");
    }

    /**
     * 更新商品
     */
    @PutMapping
    public Result update(@RequestBody Product product) {
        boolean success = productService.updateById(product);
        if (success) {
            return Result.success("更新成功", product);
        }
        return Result.error("更新失败");
    }

    /**
     * 根据ID删除商品
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean success = productService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除商品
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean success = productService.removeByIds(ids);
        if (success) {
            return Result.success("批量删除成功");
        }
        return Result.error("批量删除失败");
    }

    /**
     * 下拉数据接口：product_category
     */
    @GetMapping("/select/listProductCategory")
    public Result listProductCategory() {
        List<SelectVO> list = productService.listProductCategory();
        return Result.success(list);
    }

}
