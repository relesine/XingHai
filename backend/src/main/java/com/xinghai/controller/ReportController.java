package com.xinghai.controller;

import com.xinghai.common.PageResult;
import com.xinghai.common.Result;
import com.xinghai.dto.ReportDTO;
import com.xinghai.entity.BizReport;
import com.xinghai.security.RequireRole;
import com.xinghai.service.ContentService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 举报接口（用户举报，管理端处理）
 */
@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ContentService contentService;

    public ReportController(ContentService contentService) {
        this.contentService = contentService;
    }

    /** 用户提交举报 */
    @PostMapping
    public Result<Long> create(@Validated @RequestBody ReportDTO dto) {
        return Result.ok(contentService.createReport(dto.getTargetType(), dto.getTargetId(),
                dto.getReason(), dto.getDetail()));
    }

    /** 管理端：举报列表 */
    @GetMapping("/list")
    @RequireRole("ADMIN")
    public Result<PageResult<BizReport>> list(@RequestParam(required = false) Integer status,
                                              @RequestParam(defaultValue = "1") long pageNum,
                                              @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(contentService.reportList(status, pageNum, pageSize));
    }

    /** 管理端：处理举报（agree/reject） */
    @PostMapping("/handle/{reportId}")
    @RequireRole("ADMIN")
    public Result<Void> handle(@PathVariable Long reportId, @RequestBody Map<String, String> body) {
        contentService.handleReport(reportId, body.get("action"));
        return Result.ok();
    }
}
