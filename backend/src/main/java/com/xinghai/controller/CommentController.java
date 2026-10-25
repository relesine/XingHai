package com.xinghai.controller;

import com.xinghai.common.PageResult;
import com.xinghai.common.Result;
import com.xinghai.dto.CommentDTO;
import com.xinghai.service.CommentService;
import com.xinghai.vo.CommentVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 评论接口：发表、列表、删除
 */
@RestController
@RequestMapping("/api/comment")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /** 发表评论 */
    @PostMapping("/{videoId}")
    public Result<Long> add(@PathVariable Long videoId, @Validated @RequestBody CommentDTO dto) {
        return Result.ok(commentService.add(videoId, dto));
    }

    /** 评论列表（未登录可看） */
    @GetMapping("/list/{videoId}")
    public Result<PageResult<CommentVO>> list(@PathVariable Long videoId,
                                              @RequestParam(defaultValue = "1") long pageNum,
                                              @RequestParam(defaultValue = "20") long pageSize) {
        return Result.ok(commentService.list(videoId, pageNum, pageSize));
    }

    /** 删除评论（本人或管理员） */
    @DeleteMapping("/{commentId}")
    public Result<Void> delete(@PathVariable Long commentId) {
        commentService.delete(commentId);
        return Result.ok();
    }
}
