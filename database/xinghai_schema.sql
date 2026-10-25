-- =====================================================================
-- XingHai 星海视频社交平台 · 建库建表脚本
-- 数据库：MySQL 8.0（phpStudy Pro 集成环境）
-- 说明：本脚本可直接在 Navicat / mysql 命令行执行，含完整中文注释
-- =====================================================================

-- ① 建库（utf8mb4 支持 emoji 与中文，排序规则 utf8mb4_general_ci）
CREATE DATABASE IF NOT EXISTS `xinghai`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE `xinghai`;

-- =====================================================================
-- 通用字段规范（所有业务表统一遵守）：
--   id           BIGINT 自增主键
--   create_time  DATETIME 创建时间
--   update_time  DATETIME 更新时间
--   deleted      TINYINT  逻辑删除标记（0=正常 1=已删除），配合 MyBatis Plus @TableLogic
-- =====================================================================

-- ---------------------------------------------------------------
-- 1. 角色表 sys_role
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `code`        VARCHAR(32)  NOT NULL COMMENT '角色编码：USER/CREATOR/ADMIN',
  `name`        VARCHAR(32)  NOT NULL COMMENT '角色名称：普通用户/认证创作者/超级管理员',
  `remark`      VARCHAR(255) DEFAULT NULL COMMENT '角色备注',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ---------------------------------------------------------------
-- 2. 权限表 sys_permission（RBAC 权限点，按钮级权限控制）
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `code`        VARCHAR(64)  NOT NULL COMMENT '权限编码，如 video:upload / user:manage',
  `name`        VARCHAR(64)  NOT NULL COMMENT '权限名称',
  `type`        VARCHAR(16)  NOT NULL DEFAULT 'button' COMMENT '类型：menu菜单/button按钮/api接口',
  `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_perm_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- ---------------------------------------------------------------
-- 3. 用户表 sys_user
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`     VARCHAR(32)  NOT NULL COMMENT '登录用户名（唯一）',
  `password`     VARCHAR(128) NOT NULL COMMENT '密码（BCrypt加密存储）',
  `nickname`     VARCHAR(32)  NOT NULL COMMENT '昵称',
  `avatar`       VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `gender`       TINYINT      NOT NULL DEFAULT 0 COMMENT '性别：0未知 1男 2女',
  `phone`        VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
  `email`        VARCHAR(64)  DEFAULT NULL COMMENT '邮箱',
  `signature`    VARCHAR(255) DEFAULT NULL COMMENT '个性签名',
  `location`     VARCHAR(64)  DEFAULT NULL COMMENT '所在城市（用于同城频道）',
  `quota_total`  BIGINT       NOT NULL DEFAULT 10737418240 COMMENT '上传配额字节：普通用户10GB=10737418240',
  `quota_used`   BIGINT       NOT NULL DEFAULT 0 COMMENT '已使用配额字节',
  `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '账号状态：0禁用 1正常',
  `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ---------------------------------------------------------------
-- 4. 用户-角色关联表 sys_user_role（多对多）
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `user_id`     BIGINT   NOT NULL COMMENT '用户ID',
  `role_id`     BIGINT   NOT NULL COMMENT '角色ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ---------------------------------------------------------------
-- 5. 角色-权限关联表 sys_role_permission
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
  `id`            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `role_id`       BIGINT   NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT   NOT NULL COMMENT '权限ID',
  `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_perm` (`role_id`, `permission_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ---------------------------------------------------------------
-- 6. 视频分类表 biz_category（一级分类）
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `biz_category`;
CREATE TABLE `biz_category` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name`        VARCHAR(32)  NOT NULL COMMENT '分类名称',
  `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序号（小在前）',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='视频分类表';

-- ---------------------------------------------------------------
-- 7. 标签表 biz_tag
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `biz_tag`;
CREATE TABLE `biz_tag` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name`        VARCHAR(32) NOT NULL COMMENT '标签名称',
  `category_id` BIGINT      NOT NULL COMMENT '所属分类ID',
  `hot`         INT         NOT NULL DEFAULT 0 COMMENT '热度值（推荐算法参考）',
  `sort`        INT         NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_tag_cate` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- ---------------------------------------------------------------
-- 8. 视频表 biz_video
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `biz_video`;
CREATE TABLE `biz_video` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '视频ID',
  `user_id`       BIGINT        NOT NULL COMMENT '发布者用户ID',
  `title`         VARCHAR(128)  NOT NULL COMMENT '视频标题',
  `description`   VARCHAR(512)  DEFAULT NULL COMMENT '视频简介',
  `video_url`     VARCHAR(255)  NOT NULL COMMENT '视频文件URL',
  `cover_url`     VARCHAR(255)  DEFAULT NULL COMMENT '封面图URL',
  `duration`      INT           NOT NULL DEFAULT 0 COMMENT '视频时长（秒）',
  `category_id`   BIGINT        NOT NULL COMMENT '所属分类ID',
  `status`        TINYINT       NOT NULL DEFAULT 0 COMMENT '状态：0待审核 1已发布 2已下架 3审核拒绝（审核流转核心字段）',
  `reject_reason` VARCHAR(255)  DEFAULT NULL COMMENT '审核拒绝原因',
  `view_count`    BIGINT        NOT NULL DEFAULT 0 COMMENT '浏览量（Redis去重后回写）',
  `like_count`    BIGINT        NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` BIGINT        NOT NULL DEFAULT 0 COMMENT '评论数',
  `share_count`   BIGINT        NOT NULL DEFAULT 0 COMMENT '转发数',
  `collect_count` BIGINT        NOT NULL DEFAULT 0 COMMENT '收藏数',
  `is_public`     TINYINT       NOT NULL DEFAULT 1 COMMENT '是否公开：1公开 0私密（创作者作品权限）',
  `allow_comment` TINYINT       NOT NULL DEFAULT 1 COMMENT '是否允许评论：1允许 0禁止',
  `geo_location`  VARCHAR(64)   DEFAULT NULL COMMENT '发布位置（同城频道用）',
  `create_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_video_user` (`user_id`),
  KEY `idx_video_cate` (`category_id`),
  KEY `idx_video_status` (`status`),
  KEY `idx_video_geo` (`geo_location`),
  KEY `idx_video_public` (`status`,`is_public`,`deleted`),
  KEY `idx_video_cate_status` (`category_id`,`status`),
  KEY `idx_video_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='视频表';

-- ---------------------------------------------------------------
-- 9. 视频-标签关联表 biz_video_tag
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `biz_video_tag`;
CREATE TABLE `biz_video_tag` (
  `id`      BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `video_id` BIGINT NOT NULL COMMENT '视频ID',
  `tag_id`  BIGINT NOT NULL COMMENT '标签ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_video_tag` (`video_id`, `tag_id`),
  KEY `idx_tag` (`tag_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='视频标签关联表';

-- ---------------------------------------------------------------
-- 10. 点赞表 biz_like（点赞/收藏通用，防重复靠唯一索引）
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `biz_like`;
CREATE TABLE `biz_like` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `user_id`     BIGINT      NOT NULL COMMENT '点赞用户ID',
  `target_type` VARCHAR(16) NOT NULL COMMENT '目标类型：video视频/comment评论',
  `target_id`   BIGINT      NOT NULL COMMENT '目标ID',
  `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  `deleted`     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0已赞 1已取消（软删除实现取消点赞）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
  KEY `idx_like_target` (`target_type`,`target_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';

-- ---------------------------------------------------------------
-- 11. 评论表 biz_comment
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `biz_comment`;
CREATE TABLE `biz_comment` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `video_id`    BIGINT       NOT NULL COMMENT '视频ID',
  `user_id`     BIGINT       NOT NULL COMMENT '评论用户ID',
  `parent_id`   BIGINT       NOT NULL DEFAULT 0 COMMENT '父评论ID（0=一级评论，>0=回复）',
  `content`     VARCHAR(512) NOT NULL COMMENT '评论内容',
  `like_count`  INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0隐藏 1正常',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_comment_video` (`video_id`),
  KEY `idx_comment_user` (`user_id`),
  KEY `idx_comment_video_status` (`video_id`,`status`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- ---------------------------------------------------------------
-- 12. 关注表 biz_follow
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `biz_follow`;
CREATE TABLE `biz_follow` (
  `id`            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '关注ID',
  `user_id`       BIGINT   NOT NULL COMMENT '粉丝用户ID（谁关注）',
  `follow_user_id` BIGINT  NOT NULL COMMENT '被关注用户ID',
  `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  `deleted`       TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0关注中 1已取关',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follow` (`user_id`, `follow_user_id`),
  KEY `idx_follow_target` (`follow_user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='关注表';

-- ---------------------------------------------------------------
-- 13. 私信表 biz_message
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `biz_message`;
CREATE TABLE `biz_message` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '私信ID',
  `from_user_id`  BIGINT       NOT NULL COMMENT '发送者ID',
  `to_user_id`    BIGINT       NOT NULL COMMENT '接收者ID',
  `content`       VARCHAR(1024) NOT NULL COMMENT '私信内容',
  `msg_type`      VARCHAR(16)  NOT NULL DEFAULT 'text' COMMENT '消息类型：text文本/image图片/system系统',
  `is_read`       TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读：0未读 1已读',
  `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `deleted`       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_msg_to` (`to_user_id`, `is_read`),
  KEY `idx_msg_from` (`from_user_id`),
  KEY `idx_msg_from_to` (`from_user_id`,`to_user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='私信表';

-- ---------------------------------------------------------------
-- 14. 直播表 biz_live_room（先做基础模拟，高阶对接真实推流）
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `biz_live_room`;
CREATE TABLE `biz_live_room` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '直播间ID',
  `user_id`      BIGINT       NOT NULL COMMENT '主播用户ID',
  `title`        VARCHAR(128) NOT NULL COMMENT '直播标题',
  `cover_url`    VARCHAR(255) DEFAULT NULL COMMENT '封面URL',
  `status`       TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0未开播 1直播中 2已结束',
  `viewer_count` INT          NOT NULL DEFAULT 0 COMMENT '观看人数（模拟）',
  `like_count`   INT          NOT NULL DEFAULT 0 COMMENT '点赞数（模拟）',
  `push_url`     VARCHAR(255) DEFAULT NULL COMMENT '推流地址（高阶预留）',
  `play_url`     VARCHAR(255) DEFAULT NULL COMMENT '播放地址（高阶预留）',
  `start_time`   DATETIME     DEFAULT NULL COMMENT '开播时间',
  `end_time`     DATETIME     DEFAULT NULL COMMENT '结束时间',
  `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_live_user` (`user_id`),
  KEY `idx_live_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='直播表';

-- ---------------------------------------------------------------
-- 15. 店铺表 biz_shop
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `biz_shop`;
CREATE TABLE `biz_shop` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '店铺ID',
  `user_id`     BIGINT       NOT NULL COMMENT '店主用户ID（创作者）',
  `shop_name`   VARCHAR(64)  NOT NULL COMMENT '店铺名称',
  `logo`        VARCHAR(255) DEFAULT NULL COMMENT '店铺Logo URL',
  `description` VARCHAR(512) DEFAULT NULL COMMENT '店铺简介',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0已关闭 1营业中',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shop_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='店铺表';

-- ---------------------------------------------------------------
-- 16. 商品表 biz_product
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `biz_product`;
CREATE TABLE `biz_product` (
  `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `shop_id`     BIGINT         NOT NULL COMMENT '所属店铺ID',
  `product_name` VARCHAR(128)  NOT NULL COMMENT '商品名称',
  `cover_url`   VARCHAR(255)   DEFAULT NULL COMMENT '商品主图URL',
  `description` VARCHAR(512)   DEFAULT NULL COMMENT '商品描述',
  `price`       DECIMAL(10,2)  NOT NULL DEFAULT 0.00 COMMENT '售价（元）',
  `stock`       INT            NOT NULL DEFAULT 0 COMMENT '库存',
  `sales`       INT            NOT NULL DEFAULT 0 COMMENT '销量',
  `status`      TINYINT        NOT NULL DEFAULT 0 COMMENT '状态：0下架 1上架',
  `create_time` DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_product_shop` (`shop_id`),
  KEY `idx_product_shop_status` (`shop_id`,`status`),
  KEY `idx_product_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ---------------------------------------------------------------
-- 17. 订单表 biz_order（订单库存扣减核心业务）
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `biz_order`;
CREATE TABLE `biz_order` (
  `id`             BIGINT         NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no`       VARCHAR(32)    NOT NULL COMMENT '订单编号（唯一，雪花/时间戳生成）',
  `user_id`        BIGINT         NOT NULL COMMENT '下单用户ID（买家）',
  `shop_id`        BIGINT         NOT NULL COMMENT '店铺ID',
  `product_id`     BIGINT         NOT NULL COMMENT '商品ID',
  `product_name`   VARCHAR(128)   NOT NULL COMMENT '商品名称快照',
  `product_cover`  VARCHAR(255)   DEFAULT NULL COMMENT '商品主图快照',
  `price`          DECIMAL(10,2)  NOT NULL COMMENT '成交单价快照',
  `quantity`       INT            NOT NULL DEFAULT 1 COMMENT '购买数量',
  `total_amount`   DECIMAL(12,2)  NOT NULL COMMENT '订单总金额',
  `status`         TINYINT        NOT NULL DEFAULT 0 COMMENT '状态：0待付款 1已付款 2已发货 3已完成 4已取消',
  `address`        VARCHAR(255)   DEFAULT NULL COMMENT '收货地址',
  `pay_time`       DATETIME       DEFAULT NULL COMMENT '付款时间',
  `ship_time`      DATETIME       DEFAULT NULL COMMENT '发货时间',
  `create_time`    DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `update_time`    DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_order_user` (`user_id`),
  KEY `idx_order_shop` (`shop_id`),
  KEY `idx_order_user_status` (`user_id`,`status`),
  KEY `idx_order_shop_status` (`shop_id`,`status`),
  KEY `idx_order_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ---------------------------------------------------------------
-- 18. 广告表 biz_advert
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `biz_advert`;
CREATE TABLE `biz_advert` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '广告ID',
  `title`       VARCHAR(64)  NOT NULL COMMENT '广告标题',
  `image_url`   VARCHAR(255) DEFAULT NULL COMMENT '广告图URL',
  `link_url`    VARCHAR(255) DEFAULT NULL COMMENT '跳转链接',
  `position`    VARCHAR(16)  NOT NULL DEFAULT 'home' COMMENT '投放位置：home首页/follow关注/live直播',
  `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0停用 1投放中',
  `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序号',
  `start_time`  DATETIME     DEFAULT NULL COMMENT '投放开始时间',
  `end_time`    DATETIME     DEFAULT NULL COMMENT '投放结束时间',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='广告表';

-- ---------------------------------------------------------------
-- 19. 举报表 biz_report
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `biz_report`;
CREATE TABLE `biz_report` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `user_id`       BIGINT       NOT NULL COMMENT '举报人用户ID',
  `target_type`   VARCHAR(16)  NOT NULL COMMENT '举报对象类型：video/comment/user',
  `target_id`     BIGINT       NOT NULL COMMENT '举报对象ID',
  `reason`        VARCHAR(64)  NOT NULL COMMENT '举报原因分类',
  `detail`        VARCHAR(512) DEFAULT NULL COMMENT '补充说明',
  `status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '处理状态：0待处理 1已处理 2已驳回',
  `handle_user_id` BIGINT      DEFAULT NULL COMMENT '处理人（管理员）ID',
  `handle_time`   DATETIME     DEFAULT NULL COMMENT '处理时间',
  `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
  `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_report_status` (`status`),
  KEY `idx_report_status_time` (`status`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='举报表';

-- ---------------------------------------------------------------
-- 20. 公告表 biz_notice
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `biz_notice`;
CREATE TABLE `biz_notice` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title`       VARCHAR(128) NOT NULL COMMENT '公告标题',
  `content`     TEXT         NOT NULL COMMENT '公告内容',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0下线 1发布中',
  `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- ---------------------------------------------------------------
-- 21. 创作者数据统计表 biz_creator_stat（按天汇总，支撑数据看板图表）
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `biz_creator_stat`;
CREATE TABLE `biz_creator_stat` (
  `id`           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `user_id`      BIGINT   NOT NULL COMMENT '创作者用户ID',
  `stat_date`    DATE     NOT NULL COMMENT '统计日期',
  `views`        BIGINT   NOT NULL DEFAULT 0 COMMENT '当日新增浏览量',
  `likes`        BIGINT   NOT NULL DEFAULT 0 COMMENT '当日新增点赞数',
  `fans`         BIGINT   NOT NULL DEFAULT 0 COMMENT '当日粉丝数（存量快照）',
  `comments`     BIGINT   NOT NULL DEFAULT 0 COMMENT '当日新增评论数',
  `sales_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '当日成交金额',
  `create_time`  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stat` (`user_id`, `stat_date`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='创作者数据统计表';

-- 建表完成，共 21 张表（含角色权限相关5张、业务表16张）
