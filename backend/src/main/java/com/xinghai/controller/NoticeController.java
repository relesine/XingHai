package com.xinghai.controller;

import com.xinghai.common.Result;
import com.xinghai.entity.BizNotice;
import com.xinghai.security.RequireRole;
import com.xinghai.service.ContentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 公告接口（用户端只读，管理端增删改）
 */
@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    private final ContentService contentService;

    public NoticeController(ContentService contentService) {
        this.contentService = contentService;
    }

    /** 发布中的公告列表（未登录可看） */
    @GetMapping("/list")
    public Result<List<BizNotice>> list() {
        return Result.ok(contentService.listNotices());
    }

    /** 管理端：新增公告 */
    @PostMapping
    @RequireRole("ADMIN")
    public Result<Void> save(@RequestBody Map<String, String> body) {
        contentService.saveNotice(body.get("title"), body.get("content"));
        return Result.ok();
    }

    /** 管理端：修改公告 */
    @PutMapping("/{id}")
    @RequireRole("ADMIN")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        contentService.updateNotice(id, (String) body.get("title"), (String) body.get("content"),
                body.get("status") == null ? null : Integer.valueOf(body.get("status").toString()));
        return Result.ok();
    }

    /** 管理端：删除公告 */
    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    public Result<Void> delete(@PathVariable Long id) {
        contentService.deleteNotice(id);
        return Result.ok();
    }
}
