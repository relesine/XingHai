package com.xinghai.controller;

import com.xinghai.common.Result;
import com.xinghai.entity.BizAdvert;
import com.xinghai.security.RequireRole;
import com.xinghai.service.ContentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 广告接口（用户端只读，管理端增删改）
 */
@RestController
@RequestMapping("/api/advert")
public class AdvertController {

    private final ContentService contentService;

    public AdvertController(ContentService contentService) {
        this.contentService = contentService;
    }

    /** 投放中的广告列表（未登录可看） */
    @GetMapping("/list")
    public Result<List<BizAdvert>> list(@RequestParam(required = false) String position) {
        return Result.ok(contentService.listAdverts(position));
    }

    /** 管理端：新增广告 */
    @PostMapping
    @RequireRole("ADMIN")
    public Result<Void> save(@RequestBody BizAdvert advert) {
        contentService.saveAdvert(advert);
        return Result.ok();
    }

    /** 管理端：修改广告 */
    @PutMapping
    @RequireRole("ADMIN")
    public Result<Void> update(@RequestBody BizAdvert advert) {
        contentService.updateAdvert(advert);
        return Result.ok();
    }

    /** 管理端：删除广告 */
    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    public Result<Void> delete(@PathVariable Long id) {
        contentService.deleteAdvert(id);
        return Result.ok();
    }
}
