package com.xinghai.controller;

import com.xinghai.common.Result;
import com.xinghai.entity.BizTag;
import com.xinghai.security.RequireRole;
import com.xinghai.service.ContentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 标签接口（用户端只读，管理端增删改）
 */
@RestController
@RequestMapping("/api/tag")
public class TagController {

    private final ContentService contentService;

    public TagController(ContentService contentService) {
        this.contentService = contentService;
    }

    /** 标签列表（可按分类筛选） */
    @GetMapping("/list")
    public Result<List<BizTag>> list(@RequestParam(required = false) Long categoryId) {
        return Result.ok(contentService.listTags(categoryId));
    }

    /** 管理端：新增标签 */
    @PostMapping
    @RequireRole("ADMIN")
    public Result<Void> save(@RequestBody Map<String, Object> body) {
        contentService.saveTag((String) body.get("name"),
                Long.valueOf(body.get("categoryId").toString()),
                body.get("hot") == null ? null : Integer.valueOf(body.get("hot").toString()));
        return Result.ok();
    }

    /** 管理端：修改标签 */
    @PutMapping("/{id}")
    @RequireRole("ADMIN")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        contentService.updateTag(id, (String) body.get("name"),
                body.get("categoryId") == null ? null : Long.valueOf(body.get("categoryId").toString()),
                body.get("hot") == null ? null : Integer.valueOf(body.get("hot").toString()));
        return Result.ok();
    }

    /** 管理端：删除标签 */
    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    public Result<Void> delete(@PathVariable Long id) {
        contentService.deleteTag(id);
        return Result.ok();
    }
}
