package com.xinghai.controller;

import com.xinghai.common.Result;
import com.xinghai.dto.MessageDTO;
import com.xinghai.entity.BizMessage;
import com.xinghai.service.MessageService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 私信接口：会话列表、聊天记录、发送、未读数
 */
@RestController
@RequestMapping("/api/message")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /** 会话列表 */
    @GetMapping("/conversations")
    public Result<List<Map<String, Object>>> conversations() {
        return Result.ok(messageService.conversations());
    }

    /** 与某用户的聊天记录（自动标记已读） */
    @GetMapping("/list/{userId}")
    public Result<List<BizMessage>> conversation(@PathVariable Long userId) {
        return Result.ok(messageService.conversation(userId));
    }

    /** 发送私信 */
    @PostMapping("/send")
    public Result<Long> send(@Validated @RequestBody MessageDTO dto) {
        return Result.ok(messageService.send(dto.getToUserId(), dto.getContent()));
    }

    /** 未读消息数 */
    @GetMapping("/unread")
    public Result<Long> unread() {
        return Result.ok(messageService.unreadCount());
    }
}
