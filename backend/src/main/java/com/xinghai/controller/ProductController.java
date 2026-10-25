package com.xinghai.controller;

import com.xinghai.common.PageResult;
import com.xinghai.common.Result;
import com.xinghai.dto.ProductDTO;
import com.xinghai.entity.BizProduct;
import com.xinghai.security.RequireRole;
import com.xinghai.service.ShopService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商品接口（用户端浏览，创作者端管理）
 */
@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ShopService shopService;

    public ProductController(ShopService shopService) {
        this.shopService = shopService;
    }

    /** 商城首页：全部在售商品 */
    @GetMapping("/list")
    public Result<PageResult<BizProduct>> list(@RequestParam(defaultValue = "1") long pageNum,
                                               @RequestParam(defaultValue = "12") long pageSize) {
        return Result.ok(shopService.publicProducts(pageNum, pageSize));
    }

    /** 某店铺在售商品 */
    @GetMapping("/shop/{shopId}")
    public Result<PageResult<BizProduct>> byShop(@PathVariable Long shopId,
                                                 @RequestParam(defaultValue = "1") long pageNum,
                                                 @RequestParam(defaultValue = "12") long pageSize) {
        return Result.ok(shopService.listProducts(shopId, pageNum, pageSize));
    }

    /** 商品详情（含店铺信息） */
    @GetMapping("/detail/{productId}")
    public Result<Map<String, Object>> detail(@PathVariable Long productId) {
        return Result.ok(shopService.productDetail(productId));
    }

    /** 上架商品 */
    @PostMapping
    @RequireRole("CREATOR")
    public Result<Long> add(@Validated @RequestBody ProductDTO dto) {
        return Result.ok(shopService.addProduct(
                dto.getProductName(),
                dto.getCoverUrl(),
                dto.getDescription(),
                dto.getPrice(),
                dto.getStock()));
    }

    /** 修改商品 */
    @PutMapping("/{productId}")
    @RequireRole("CREATOR")
    public Result<Void> update(@PathVariable Long productId, @Validated @RequestBody ProductDTO dto) {
        shopService.updateProduct(productId,
                dto.getProductName(),
                dto.getCoverUrl(),
                dto.getDescription(),
                dto.getPrice(),
                dto.getStock());
        return Result.ok();
    }

    /** 上下架 */
    @PutMapping("/status/{productId}")
    @RequireRole("CREATOR")
    public Result<Void> changeStatus(@PathVariable Long productId, @RequestBody Map<String, Integer> body) {
        shopService.changeProductStatus(productId, body.get("status"));
        return Result.ok();
    }

    /** 删除商品 */
    @DeleteMapping("/{productId}")
    @RequireRole("CREATOR")
    public Result<Void> delete(@PathVariable Long productId) {
        shopService.deleteProduct(productId);
        return Result.ok();
    }
}
