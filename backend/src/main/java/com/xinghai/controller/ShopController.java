package com.xinghai.controller;

import com.xinghai.common.Result;
import com.xinghai.security.RequireRole;
import com.xinghai.service.ShopService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 店铺接口（创作者开店、公开店铺浏览）
 */
@RestController
@RequestMapping("/api/shop")
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    /** 营业中的店铺列表（公开，商城首页用） */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        return Result.ok(shopService.publicShops());
    }

    /** 公开店铺详情（含在售商品） */
    @GetMapping("/detail/{shopId}")
    public Result<Map<String, Object>> detail(@PathVariable Long shopId) {
        return Result.ok(shopService.shopDetail(shopId));
    }

    /** 我的店铺（创作者） */
    @GetMapping("/mine")
    public Result<Map<String, Object>> mine() {
        return Result.ok(shopService.myShop());
    }

    /** 开店 */
    @PostMapping("/create")
    @RequireRole("CREATOR")
    public Result<Long> create(@RequestBody Map<String, String> body) {
        return Result.ok(shopService.createShop(body.get("shopName"), body.get("logo"), body.get("description")));
    }

    /** 更新店铺 */
    @PutMapping("/{shopId}")
    @RequireRole("CREATOR")
    public Result<Void> update(@PathVariable Long shopId, @RequestBody Map<String, String> body) {
        shopService.updateShop(shopId, body.get("shopName"), body.get("logo"), body.get("description"));
        return Result.ok();
    }
}
