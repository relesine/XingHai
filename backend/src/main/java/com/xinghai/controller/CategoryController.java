package com.xinghai.controller;

import com.xinghai.common.Result;
import com.xinghai.entity.BizCategory;
import com.xinghai.security.RequireRole;
import com.xinghai.service.ContentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 分类接口（用户端只读，管理端增删改）
 */
@RestController
@RequestMapping("/api/category")
public class CategoryController {

    private final ContentService contentService;

    public CategoryController(ContentService contentService) {
        this.contentService = contentService;
    }

    /** 分类列表（首页/发布页用） */
    @GetMapping("/list")
    public Result<List<BizCategory>> list() {
        return Result.ok(contentService.listCategories());
    }

    /** 管理端：新增分类 */
    @PostMapping
    @RequireRole("ADMIN")
    public Result<Void> save(@RequestBody Map<String, Object> body) {
        contentService.saveCategory((String) body.get("name"),
                body.get("sort") == null ? null : Integer.valueOf(body.get("sort").toString()));
        return Result.ok();
    }

    /** 管理端：修改分类 */
    @PutMapping("/{id}")
    @RequireRole("ADMIN")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        contentService.updateCategory(id, (String) body.get("name"),
                body.get("sort") == null ? null : Integer.valueOf(body.get("sort").toString()),
                body.get("status") == null ? null : Integer.valueOf(body.get("status").toString()));
        return Result.ok();
    }

    /** 管理端：删除分类 */
    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    public Result<Void> delete(@PathVariable Long id) {
        contentService.deleteCategory(id);
        return Result.ok();
    }
}
