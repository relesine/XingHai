package com.xinghai.controller;

import com.xinghai.common.Result;
import com.xinghai.service.UploadService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 文件上传接口：视频、图片（封面/头像/商品图）
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    /** 上传视频（校验扩展名 + 配额） */
    @PostMapping("/video")
    public Result<String> uploadVideo(@RequestParam("file") MultipartFile file) {
        return Result.ok("上传成功", uploadService.uploadVideo(file));
    }

    /** 上传图片（封面/头像/商品图） */
    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file,
                                      @RequestParam(defaultValue = "cover") String dir) {
        return Result.ok("上传成功", uploadService.uploadImage(file, dir));
    }
}
