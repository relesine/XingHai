package com.xinghai.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一业务状态码定义
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "系统繁忙，请稍后重试"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有权限访问"),
    NOT_FOUND(404, "资源不存在"),

    LOGIN_FAIL(1001, "用户名或密码错误"),
    USER_DISABLED(1002, "账号已被禁用"),
    USER_EXISTS(1003, "用户名已存在"),
    PASSWORD_ERROR(1004, "原密码错误"),

    VIDEO_NOT_FOUND(2001, "视频不存在或已删除"),
    VIDEO_UNAVAILABLE(2002, "视频不可见或未通过审核"),
    VIDEO_STATUS_ERROR(2003, "当前状态不允许该操作"),

    FILE_TYPE_ERROR(3001, "文件类型不允许"),
    FILE_SIZE_ERROR(3002, "文件超出大小限制"),
    QUOTA_EXCEED(3003, "上传配额不足"),

    LIKE_REPEAT(4001, "请勿重复操作"),
    COLLECT_REPEAT(4002, "已收藏该视频"),

    FOLLOW_EXISTS(4101, "已关注该用户"),
    NOT_FOLLOW(4102, "尚未关注该用户"),

    SHOP_EXISTS(5001, "已拥有店铺"),
    STOCK_NOT_ENOUGH(5002, "商品库存不足"),
    ORDER_STATUS_ERROR(5003, "订单状态不允许该操作"),
    ORDER_REPEAT(5004, "请勿重复提交订单"),

    ROOM_EXISTS(6001, "已有正在进行的直播"),
    TARGET_NOT_FOUND(7001, "目标内容不存在");

    private final Integer code;
    private final String message;
}
