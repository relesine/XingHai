package com.xinghai.controller;

import com.xinghai.common.PageResult;
import com.xinghai.common.Result;
import com.xinghai.service.LikeService;
import com.xinghai.service.VideoService;
import com.xinghai.vo.VideoVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 点赞接口：视频/评论点赞、取消点赞、我赞过的视频
 */
@RestController
@RequestMapping("/api/like")
public class LikeController {

    private final LikeService likeService;
    private final VideoService videoService;

    public LikeController(LikeService likeService, VideoService videoService) {
        this.likeService = likeService;
        this.videoService = videoService;
    }

    /** 点赞（video/comment） */
    @PostMapping("/{targetType}/{targetId}")
    public Result<Void> like(@PathVariable String targetType, @PathVariable Long targetId) {
        likeService.like(targetType, targetId);
        return Result.ok();
    }

    /** 取消点赞 */
    @DeleteMapping("/{targetType}/{targetId}")
    public Result<Void> unlike(@PathVariable String targetType, @PathVariable Long targetId) {
        likeService.unlike(targetType, targetId);
        return Result.ok();
    }

    /** 我点赞过的视频 */
    @GetMapping("/videos")
    public Result<List<VideoVO>> myLikedVideos() {
        List<VideoVO> list = likeService.myLikedVideoIds().stream()
                .map(id -> videoService.detailQuiet(id))
                .filter(java.util.Objects::nonNull)
                .toList();
        return Result.ok(list);
    }

    /** 收藏视频（复用 biz_like 表，targetType=collect） */
    @PostMapping("/collect/{videoId}")
    public Result<Void> collect(@PathVariable Long videoId) {
        likeService.like("collect", videoId);
        return Result.ok();
    }

    /** 取消收藏 */
    @DeleteMapping("/collect/{videoId}")
    public Result<Void> uncollect(@PathVariable Long videoId) {
        likeService.unlike("collect", videoId);
        return Result.ok();
    }

    /** 我收藏过的视频 */
    @GetMapping("/collections")
    public Result<List<VideoVO>> myCollectedVideos() {
        List<VideoVO> list = likeService.myCollectedVideoIds().stream()
                .map(id -> videoService.detailQuiet(id))
                .filter(java.util.Objects::nonNull)
                .toList();
        return Result.ok(list);
    }
}
