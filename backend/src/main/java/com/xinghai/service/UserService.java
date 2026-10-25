package com.xinghai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinghai.common.ResultCode;
import com.xinghai.entity.BizFollow;
import com.xinghai.entity.BizVideo;
import com.xinghai.entity.SysRole;
import com.xinghai.entity.SysUser;
import com.xinghai.exception.BusinessException;
import com.xinghai.mapper.BizFollowMapper;
import com.xinghai.mapper.BizVideoMapper;
import com.xinghai.mapper.SysUserMapper;
import com.xinghai.security.UserContext;
import com.xinghai.vo.UserVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务：个人资料、关注/粉丝列表、公开信息
 */
@Service
public class UserService {

    private final SysUserMapper userMapper;
    private final BizFollowMapper followMapper;
    private final BizVideoMapper videoMapper;
    private final SysRoleService roleService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(SysUserMapper userMapper, BizFollowMapper followMapper,
                       BizVideoMapper videoMapper, SysRoleService roleService) {
        this.userMapper = userMapper;
        this.followMapper = followMapper;
        this.videoMapper = videoMapper;
        this.roleService = roleService;
    }

    /** 当前登录用户完整资料 */
    public UserVO getCurrentUser() {
        return toVO(getUserOrThrow(UserContext.getUserId()));
    }

    /** 公开用户信息（任何人可看，不含手机号等敏感信息） */
    public UserVO getPublicInfo(Long userId) {
        SysUser user = getUserOrThrow(userId);
        UserVO vo = toVO(user);
        // 公开信息隐藏手机号/邮箱
        vo.setPhone(null);
        vo.setEmail(null);
        // 回填当前登录用户是否已关注该用户（未登录或查看自己时为 false）
        Long currentId = UserContext.getUserId();
        if (currentId != null && !currentId.equals(userId)) {
            Long cnt = followMapper.selectCount(new LambdaQueryWrapper<BizFollow>()
                    .eq(BizFollow::getUserId, currentId)
                    .eq(BizFollow::getFollowUserId, userId));
            vo.setFollowed(cnt != null && cnt > 0);
        } else {
            vo.setFollowed(false);
        }
        return vo;
    }

    /** 修改个人资料（昵称、签名、性别、城市） */
    public void updateProfile(String nickname, String signature, Integer gender, String location) {
        SysUser user = getUserOrThrow(UserContext.getUserId());
        if (nickname != null && !nickname.isBlank()) {
            user.setNickname(nickname);
        }
        user.setSignature(signature);
        if (gender != null) {
            user.setGender(gender);
        }
        if (location != null) {
            user.setLocation(location);
        }
        userMapper.updateById(user);
    }

    /** 修改头像 */
    public void updateAvatar(String avatarUrl) {
        SysUser user = getUserOrThrow(UserContext.getUserId());
        user.setAvatar(avatarUrl);
        userMapper.updateById(user);
    }

    /** 修改密码（需验证原密码） */
    public void updatePassword(String oldPassword, String newPassword) {
        SysUser user = getUserOrThrow(UserContext.getUserId());
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    /** 我的关注列表（我关注了谁） */
    public List<UserVO> listFollowing(Long userId) {
        List<BizFollow> follows = followMapper.selectList(new LambdaQueryWrapper<BizFollow>()
                .eq(BizFollow::getUserId, userId).orderByDesc(BizFollow::getCreateTime));
        return follows.stream()
                .map(f -> getPublicInfo(f.getFollowUserId()))
                .collect(Collectors.toList());
    }

    /** 我的粉丝列表（谁关注了我） */
    public List<UserVO> listFans(Long userId) {
        List<BizFollow> follows = followMapper.selectList(new LambdaQueryWrapper<BizFollow>()
                .eq(BizFollow::getFollowUserId, userId).orderByDesc(BizFollow::getCreateTime));
        return follows.stream()
                .map(f -> getPublicInfo(f.getUserId()))
                .collect(Collectors.toList());
    }

    /** 用户转 VO（补全关注数、粉丝数、作品数、角色） */
    public UserVO toVO(SysUser user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setGender(user.getGender());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setSignature(user.getSignature());
        vo.setLocation(user.getLocation());
        vo.setQuotaTotal(user.getQuotaTotal());
        vo.setQuotaUsed(user.getQuotaUsed());

        List<SysRole> roles = roleService.listByUserId(user.getId());
        vo.setRoles(roles.stream().map(SysRole::getCode).collect(Collectors.toList()));
        vo.setCreator(roles.stream().anyMatch(r -> "CREATOR".equals(r.getCode())));

        vo.setFollowCount(followMapper.selectCount(new LambdaQueryWrapper<BizFollow>()
                .eq(BizFollow::getUserId, user.getId())));
        vo.setFansCount(followMapper.selectCount(new LambdaQueryWrapper<BizFollow>()
                .eq(BizFollow::getFollowUserId, user.getId())));
        vo.setVideoCount(videoMapper.selectCount(new LambdaQueryWrapper<BizVideo>()
                .eq(BizVideo::getUserId, user.getId())));
        return vo;
    }

    /** 查询用户，不存在抛异常 */
    public SysUser getUserOrThrow(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }
        return user;
    }

    /** 校验并返回用户（供其他服务使用） */
    public SysUser getById(Long userId) {
        return userMapper.selectById(userId);
    }

    /** 更新用户（供管理端） */
    public void updateById(SysUser user) {
        userMapper.updateById(user);
    }

    /** 累加已用上传配额（上传视频成功后调用） */
    public void addQuotaUsed(Long userId, long bytes) {
        SysUser user = getUserOrThrow(userId);
        user.setQuotaUsed(user.getQuotaUsed() + bytes);
        userMapper.updateById(user);
    }

    /** 管理端：禁用/启用账号 */
    @Transactional
    public void changeStatus(Long userId, Integer status) {
        SysUser user = getUserOrThrow(userId);
        if (status != 0 && status != 1) {
            throw new BusinessException("状态值不合法");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }
}
