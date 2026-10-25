package com.xinghai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xinghai.common.ResultCode;
import com.xinghai.entity.BizMessage;
import com.xinghai.entity.SysUser;
import com.xinghai.exception.BusinessException;
import com.xinghai.mapper.BizMessageMapper;
import com.xinghai.mapper.SysUserMapper;
import com.xinghai.security.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 私信服务：发送、会话列表、聊天记录、已读
 */
@Service
public class MessageService {

    private final BizMessageMapper messageMapper;
    private final SysUserMapper userMapper;

    public MessageService(BizMessageMapper messageMapper, SysUserMapper userMapper) {
        this.messageMapper = messageMapper;
        this.userMapper = userMapper;
    }

    /** 发送私信 */
    @Transactional
    public Long send(Long toUserId, String content) {
        SysUser toUser = userMapper.selectById(toUserId);
        if (toUser == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "接收者不存在");
        }
        BizMessage msg = new BizMessage();
        msg.setFromUserId(UserContext.getUserId());
        msg.setToUserId(toUserId);
        msg.setContent(content);
        msg.setMsgType("text");
        msg.setIsRead(0);
        messageMapper.insert(msg);
        return msg.getId();
    }

    /** 会话列表：返回与我聊过天的用户（去重），附带最近一条消息与未读数 */
    public List<Map<String, Object>> conversations() {
        Long userId = UserContext.getUserId();
        List<BizMessage> all = messageMapper.selectList(new LambdaQueryWrapper<BizMessage>()
                .eq(BizMessage::getFromUserId, userId)
                .or().eq(BizMessage::getToUserId, userId)
                .orderByDesc(BizMessage::getCreateTime));
        Map<Long, List<BizMessage>> grouped = new HashMap<>();
        for (BizMessage m : all) {
            Long peerId = m.getFromUserId().equals(userId) ? m.getToUserId() : m.getFromUserId();
            grouped.computeIfAbsent(peerId, k -> new ArrayList<>()).add(m);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, List<BizMessage>> e : grouped.entrySet()) {
            SysUser peer = userMapper.selectById(e.getKey());
            if (peer == null) {
                continue;
            }
            List<BizMessage> msgs = e.getValue();
            BizMessage last = msgs.get(0);
            long unread = msgs.stream()
                    .filter(m -> m.getToUserId().equals(userId) && m.getIsRead() == 0)
                    .count();
            Map<String, Object> item = new HashMap<>();
            item.put("userId", peer.getId());
            item.put("nickname", peer.getNickname());
            item.put("avatar", peer.getAvatar());
            item.put("lastContent", last.getContent());
            item.put("lastTime", last.getCreateTime());
            item.put("unread", unread);
            result.add(item);
        }
        return result;
    }

    /** 与某用户的聊天记录（按时间正序），并将对方发来的消息标记已读 */
    @Transactional
    public List<BizMessage> conversation(Long peerId) {
        Long userId = UserContext.getUserId();
        List<BizMessage> msgs = messageMapper.selectList(new LambdaQueryWrapper<BizMessage>()
                .eq(BizMessage::getFromUserId, userId).eq(BizMessage::getToUserId, peerId)
                .or(w -> w.eq(BizMessage::getFromUserId, peerId).eq(BizMessage::getToUserId, userId))
                .orderByAsc(BizMessage::getCreateTime));
        messageMapper.update(null, new LambdaUpdateWrapper<BizMessage>()
                .eq(BizMessage::getFromUserId, peerId)
                .eq(BizMessage::getToUserId, userId)
                .eq(BizMessage::getIsRead, 0)
                .set(BizMessage::getIsRead, 1));
        return msgs;
    }

    /** 未读消息总数（消息中心角标） */
    public long unreadCount() {
        return messageMapper.selectCount(new LambdaQueryWrapper<BizMessage>()
                .eq(BizMessage::getToUserId, UserContext.getUserId())
                .eq(BizMessage::getIsRead, 0));
    }
}
