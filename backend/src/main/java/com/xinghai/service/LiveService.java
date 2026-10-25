package com.xinghai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinghai.common.ResultCode;
import com.xinghai.entity.BizLiveRoom;
import com.xinghai.entity.SysUser;
import com.xinghai.exception.BusinessException;
import com.xinghai.mapper.BizLiveRoomMapper;
import com.xinghai.mapper.SysUserMapper;
import com.xinghai.security.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 直播服务：基础模拟（开播/关播/列表/模拟热度）
 */
@Service
public class LiveService {

    private final BizLiveRoomMapper roomMapper;
    private final SysUserMapper userMapper;

    public LiveService(BizLiveRoomMapper roomMapper, SysUserMapper userMapper) {
        this.roomMapper = roomMapper;
        this.userMapper = userMapper;
    }

    /** 开播：存在未开播房间则复用，否则新建 */
    @Transactional
    public Long start(String title) {
        Long userId = UserContext.getUserId();
        BizLiveRoom active = roomMapper.selectOne(new LambdaQueryWrapper<BizLiveRoom>()
                .eq(BizLiveRoom::getUserId, userId)
                .eq(BizLiveRoom::getStatus, 1));
        if (active != null) {
            throw new BusinessException(ResultCode.ROOM_EXISTS);
        }
        BizLiveRoom room = roomMapper.selectOne(new LambdaQueryWrapper<BizLiveRoom>()
                .eq(BizLiveRoom::getUserId, userId)
                .eq(BizLiveRoom::getStatus, 0)
                .last("LIMIT 1"));
        if (room == null) {
            room = new BizLiveRoom();
            room.setUserId(userId);
            room.setTitle(title);
            room.setStatus(0);
            room.setViewerCount(0);
            room.setLikeCount(0);
            roomMapper.insert(room);
        }
        room.setTitle(title);
        room.setStatus(1);
        room.setStartTime(LocalDateTime.now());
        roomMapper.updateById(room);
        return room.getId();
    }

    /** 关播 */
    @Transactional
    public void stop(Long roomId) {
        BizLiveRoom room = getRoom(roomId);
        if (!room.getUserId().equals(UserContext.getUserId()) && !UserContext.hasRole("ADMIN")) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        room.setStatus(2);
        room.setEndTime(LocalDateTime.now());
        roomMapper.updateById(room);
    }

    /** 直播间列表：直播中优先 */
    public List<Map<String, Object>> list() {
        return roomMapper.selectList(new LambdaQueryWrapper<BizLiveRoom>()
                        .orderByDesc(BizLiveRoom::getStatus)
                        .orderByDesc(BizLiveRoom::getCreateTime))
                .stream().map(this::toMap).collect(Collectors.toList());
    }

    /** 模拟热度：每次调用随机波动观看数与点赞数 */
    @Transactional
    public void simulate() {
        List<BizLiveRoom> live = roomMapper.selectList(new LambdaQueryWrapper<BizLiveRoom>()
                .eq(BizLiveRoom::getStatus, 1));
        for (BizLiveRoom room : live) {
            room.setViewerCount(room.getViewerCount() + (int) (Math.random() * 30));
            room.setLikeCount(room.getLikeCount() + (int) (Math.random() * 100));
            roomMapper.updateById(room);
        }
    }

    private BizLiveRoom getRoom(Long roomId) {
        BizLiveRoom room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "直播间不存在");
        }
        return room;
    }

    private Map<String, Object> toMap(BizLiveRoom room) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", room.getId());
        m.put("userId", room.getUserId());
        SysUser user = userMapper.selectById(room.getUserId());
        m.put("nickname", user == null ? "" : user.getNickname());
        m.put("avatar", user == null ? null : user.getAvatar());
        m.put("title", room.getTitle());
        m.put("coverUrl", room.getCoverUrl());
        m.put("status", room.getStatus());
        m.put("viewerCount", room.getViewerCount());
        m.put("likeCount", room.getLikeCount());
        m.put("startTime", room.getStartTime());
        return m;
    }
}
