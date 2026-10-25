package com.xinghai.service;

import com.xinghai.common.ResultCode;
import com.xinghai.exception.BusinessException;
import com.xinghai.security.UserContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传服务：本地存储（必做），视频/封面/头像通用
 * <p>安全要点：
 * 1. 扩展名白名单校验（防上传 exe/jsp 等危险文件）；
 * 2. 文件头魔数校验（防伪装成图片/视频的可执行文件）；
 * 3. 存储目录白名单校验（防目录穿越，只能写入 avatar/cover/video 三个目录）；
 * 4. UUID 重命名（防文件名乱码与路径穿越）；
 * 5. 视频上传时校验用户配额（10GB/50GB）。</p>
 */
@Service
public class UploadService {

    /** 允许写入的业务目录白名单（杜绝通过 dir 参数进行目录穿越） */
    private static final Set<String> ALLOWED_DIRS = Set.of("avatar", "cover", "video");

    @Value("${xinghai.upload.base-dir}")
    private String baseDir;

    @Value("${xinghai.upload.video-ext}")
    private String videoExt;

    @Value("${xinghai.upload.image-ext}")
    private String imageExt;

    private final UserService userService;

    public UploadService(UserService userService) {
        this.userService = userService;
    }

    /**
     * 上传图片（封面/头像）：校验目录与扩展名，保存到 uploads/{dir}/{yyyyMMdd}/
     *
     * @param dir  业务目录：avatar（头像）/ cover（封面、商品图）
     * @return 可访问的相对 URL，如 /upload/cover/20261014/xxxx.jpg
     */
    public String uploadImage(MultipartFile file, String dir) {
        String safeDir = checkDir(dir);
        String ext = checkExt(file, imageExt, "图片");
        verifyMagic(file, ext, "图片");
        String relativePath = save(file, safeDir, ext);
        return "/upload/" + relativePath;
    }

    /**
     * 上传视频：扩展名校验 + 配额校验（配额不足报 QUOTA_EXCEED）
     *
     * @return 可访问的相对 URL，如 /upload/video/20261014/xxxx.mp4
     */
    public String uploadVideo(MultipartFile file) {
        String ext = checkExt(file, videoExt, "视频");
        verifyMagic(file, ext, "视频");
        // 配额校验：已用 + 本次大小 <= 总配额
        Long userId = UserContext.getUserId();
        long quotaUsed = userService.getUserOrThrow(userId).getQuotaUsed();
        long quotaTotal = userService.getUserOrThrow(userId).getQuotaTotal();
        long fileSize = file.getSize();
        if (quotaUsed + fileSize > quotaTotal) {
            throw new BusinessException(ResultCode.QUOTA_EXCEED);
        }
        String relativePath = save(file, "video", ext);
        userService.addQuotaUsed(userId, fileSize);
        return "/upload/" + relativePath;
    }

    /**
     * 目录白名单校验：只允许 avatar/cover/video，防止 {@code ../} 目录穿越
     */
    private String checkDir(String dir) {
        if (dir == null || dir.isBlank() || !ALLOWED_DIRS.contains(dir.trim())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "非法的上传目录");
        }
        return dir.trim();
    }

    /** 扩展名白名单校验 */
    private String checkExt(MultipartFile file, String allowed, String typeName) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }
        String original = file.getOriginalFilename();
        // 白名单判断（防绕过：只认扩展名，不认 Content-Type）
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase();
        }
        final String extFinal = ext;
        boolean ok = Arrays.stream(allowed.split(","))
                .map(String::trim)
                .anyMatch(a -> a.equalsIgnoreCase(extFinal));
        if (!ok) {
            throw new BusinessException(ResultCode.FILE_TYPE_ERROR.getCode(),
                    typeName + "文件类型不允许: ." + ext + "（允许: " + allowed + "）");
        }
        return ext;
    }

    /**
     * 文件头魔数校验：扩展名可以伪造，读取前 12 字节确认真实文件类型
     * <p>覆盖图片（jpg/png/gif/webp）与视频（mp4/mov/mkv/webm/avi）常见容器格式。</p>
     */
    private void verifyMagic(MultipartFile file, String ext, String typeName) {
        byte[] head = new byte[12];
        int read;
        try (InputStream in = file.getInputStream()) {
            read = in.read(head);
        } catch (IOException e) {
            throw new BusinessException("文件读取失败，请重新上传");
        }
        if (read < 4) {
            throw new BusinessException(ResultCode.FILE_TYPE_ERROR.getCode(),
                    typeName + "文件内容不完整");
        }
        boolean ok = switch (ext) {
            case "jpg", "jpeg" -> startsWith(head, read, 0xFF, 0xD8, 0xFF);
            case "png" -> startsWith(head, read, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
            case "gif" -> startsWith(head, read, 0x47, 0x49, 0x46, 0x38);
            case "webp" -> startsWith(head, read, 0x52, 0x49, 0x46, 0x46)
                    && matchesAt(head, read, 8, 0x57, 0x45, 0x42, 0x50);
            case "mp4", "mov" -> matchesAt(head, read, 4, 0x66, 0x74, 0x79, 0x70);
            case "mkv", "webm" -> startsWith(head, read, 0x1A, 0x45, 0xDF, 0xA3);
            case "avi" -> startsWith(head, read, 0x52, 0x49, 0x46, 0x46)
                    && matchesAt(head, read, 8, 0x41, 0x56, 0x49, 0x20);
            default -> false;
        };
        if (!ok) {
            throw new BusinessException(ResultCode.FILE_TYPE_ERROR.getCode(),
                    typeName + "文件内容与扩展名不符，请上传真实的 " + ext + " 文件");
        }
    }

    private boolean startsWith(byte[] data, int read, int... signature) {
        return matchesAt(data, read, 0, signature);
    }

    private boolean matchesAt(byte[] data, int read, int offset, int... signature) {
        if (read < offset + signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if ((data[offset + i] & 0xFF) != signature[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 保存文件：按日期分目录 + UUID 重命名
     *
     * @return 相对路径（含日期目录），如 cover/20261014/xxxx.jpg，用于拼访问 URL
     */
    private String save(MultipartFile file, String dir, String ext) {
        String datePath = LocalDate.now().toString().replace("-", "");
        File dirFile = new File(baseDir, dir + File.separator + datePath);
        if (!dirFile.exists() && !dirFile.mkdirs()) {
            throw new BusinessException("创建上传目录失败");
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        File target = new File(dirFile, fileName);
        try {
            file.transferTo(target.getAbsoluteFile());
        } catch (IOException e) {
            throw new BusinessException("文件保存失败: " + e.getMessage());
        }
        // 关键：返回的相对路径必须包含日期目录，否则静态资源映射 /upload/** 会 404
        return dir + "/" + datePath + "/" + fileName;
    }
}
