/*
 Navicat Premium Data Transfer

 Source Server         : demo_XingHai
 Source Server Type    : MySQL
 Source Server Version : 80012
 Source Host           : localhost:3306
 Source Schema         : xinghai

 Target Server Type    : MySQL
 Target Server Version : 80012
 File Encoding         : 65001

 Date: 24/10/2026 21:36:08
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for biz_advert
-- ----------------------------
DROP TABLE IF EXISTS `biz_advert`;
CREATE TABLE `biz_advert`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '广告ID',
  `title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '广告标题',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '广告图URL',
  `link_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '跳转链接',
  `position` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'home' COMMENT '投放位置：home首页/follow关注/live直播',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态：0停用 1投放中',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序号',
  `start_time` datetime NULL DEFAULT NULL COMMENT '投放开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '投放结束时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '广告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_advert
-- ----------------------------
INSERT INTO `biz_advert` VALUES (1, '星海优选 早茶点心节', '/upload/cover/ad1.svg', 'https://www.xinghai.com/shop/1', 'home', 1, 1, NULL, NULL, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_advert` VALUES (2, '程序员键盘限时优惠', '/upload/cover/ad2.svg', 'https://www.xinghai.com/shop/2', 'home', 1, 2, NULL, NULL, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);

-- ----------------------------
-- Table structure for biz_category
-- ----------------------------
DROP TABLE IF EXISTS `biz_category`;
CREATE TABLE `biz_category`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序号（小在前）',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '视频分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_category
-- ----------------------------
INSERT INTO `biz_category` VALUES (1, '生活记录', 1, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_category` VALUES (2, '知识分享', 2, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_category` VALUES (3, '娱乐搞笑', 3, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);

-- ----------------------------
-- Table structure for biz_comment
-- ----------------------------
DROP TABLE IF EXISTS `biz_comment`;
CREATE TABLE `biz_comment`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `video_id` bigint(20) NOT NULL COMMENT '视频ID',
  `user_id` bigint(20) NOT NULL COMMENT '评论用户ID',
  `parent_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '父评论ID（0=一级评论，>0=回复）',
  `content` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `like_count` int(11) NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：0隐藏 1正常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_comment_video`(`video_id` ASC) USING BTREE,
  INDEX `idx_comment_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_comment_video_status`(`video_id` ASC, `status` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_comment
-- ----------------------------
INSERT INTO `biz_comment` VALUES (1, 1, 1006, 0, '看起来好好吃！求教程！', 26, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_comment` VALUES (2, 1, 1007, 1, '同求教程+1', 8, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_comment` VALUES (3, 1, 1008, 0, '一个人也要好好吃饭，泪目', 15, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_comment` VALUES (4, 3, 1009, 0, '终于有人讲清楚了，收藏了', 42, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_comment` VALUES (5, 3, 1010, 0, '类比太形象了，感谢UP主', 18, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_comment` VALUES (6, 5, 1011, 0, '哈哈哈哈哈太真实了', 55, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_comment` VALUES (7, 5, 1012, 0, '最后那个镜头绝了', 12, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_comment` VALUES (8, 9, 1013, 0, '程序员看完沉默了……', 37, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);

-- ----------------------------
-- Table structure for biz_creator_stat
-- ----------------------------
DROP TABLE IF EXISTS `biz_creator_stat`;
CREATE TABLE `biz_creator_stat`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `user_id` bigint(20) NOT NULL COMMENT '创作者用户ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `views` bigint(20) NOT NULL DEFAULT 0 COMMENT '当日新增浏览量',
  `likes` bigint(20) NOT NULL DEFAULT 0 COMMENT '当日新增点赞数',
  `fans` bigint(20) NOT NULL DEFAULT 0 COMMENT '当日粉丝数（存量快照）',
  `comments` bigint(20) NOT NULL DEFAULT 0 COMMENT '当日新增评论数',
  `sales_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '当日成交金额',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_stat`(`user_id` ASC, `stat_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '创作者数据统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_creator_stat
-- ----------------------------
INSERT INTO `biz_creator_stat` VALUES (1, 1001, '2026-09-05', 1200, 86, 1560, 42, 320.00, '2026-09-11 22:30:23');
INSERT INTO `biz_creator_stat` VALUES (2, 1001, '2026-09-06', 1500, 102, 1610, 50, 480.50, '2026-09-11 22:30:23');
INSERT INTO `biz_creator_stat` VALUES (3, 1001, '2026-09-07', 980, 64, 1660, 35, 260.00, '2026-09-11 22:30:23');
INSERT INTO `biz_creator_stat` VALUES (4, 1001, '2026-09-08', 2100, 150, 1715, 68, 520.00, '2026-09-11 22:30:23');
INSERT INTO `biz_creator_stat` VALUES (5, 1001, '2026-09-09', 1850, 128, 1760, 55, 410.00, '2026-09-11 22:30:23');
INSERT INTO `biz_creator_stat` VALUES (6, 1001, '2026-09-10', 2400, 172, 1810, 72, 680.00, '2026-09-11 22:30:23');
INSERT INTO `biz_creator_stat` VALUES (7, 1001, '2026-09-11', 1600, 110, 1840, 48, 390.00, '2026-09-11 22:30:23');
INSERT INTO `biz_creator_stat` VALUES (8, 1002, '2026-09-05', 2300, 180, 920, 88, 0.00, '2026-09-11 22:30:23');
INSERT INTO `biz_creator_stat` VALUES (9, 1002, '2026-09-06', 2800, 210, 980, 102, 0.00, '2026-09-11 22:30:23');
INSERT INTO `biz_creator_stat` VALUES (10, 1002, '2026-09-07', 1900, 150, 1040, 76, 0.00, '2026-09-11 22:30:23');
INSERT INTO `biz_creator_stat` VALUES (11, 1002, '2026-09-08', 3500, 280, 1120, 130, 0.00, '2026-09-11 22:30:23');
INSERT INTO `biz_creator_stat` VALUES (12, 1002, '2026-09-09', 3100, 240, 1190, 115, 0.00, '2026-09-11 22:30:23');
INSERT INTO `biz_creator_stat` VALUES (13, 1002, '2026-09-10', 4200, 330, 1280, 150, 0.00, '2026-09-11 22:30:23');
INSERT INTO `biz_creator_stat` VALUES (14, 1002, '2026-09-11', 2700, 200, 1330, 96, 0.00, '2026-09-11 22:30:23');
INSERT INTO `biz_creator_stat` VALUES (15, 1003, '2026-09-05', 1600, 120, 680, 60, 0.00, '2026-09-11 22:30:23');
INSERT INTO `biz_creator_stat` VALUES (16, 1003, '2026-09-06', 1900, 145, 720, 71, 0.00, '2026-09-11 22:30:23');
INSERT INTO `biz_creator_stat` VALUES (17, 1003, '2026-09-07', 1300, 95, 760, 52, 0.00, '2026-09-11 22:30:23');
INSERT INTO `biz_creator_stat` VALUES (18, 1003, '2026-09-08', 2600, 200, 810, 95, 0.00, '2026-09-11 22:30:23');
INSERT INTO `biz_creator_stat` VALUES (19, 1003, '2026-09-09', 2200, 170, 850, 80, 0.00, '2026-09-11 22:30:23');
INSERT INTO `biz_creator_stat` VALUES (20, 1003, '2026-09-10', 3000, 230, 900, 110, 0.00, '2026-09-11 22:30:23');
INSERT INTO `biz_creator_stat` VALUES (21, 1003, '2026-09-11', 2000, 150, 930, 75, 0.00, '2026-09-11 22:30:23');

-- ----------------------------
-- Table structure for biz_follow
-- ----------------------------
DROP TABLE IF EXISTS `biz_follow`;
CREATE TABLE `biz_follow`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关注ID',
  `user_id` bigint(20) NOT NULL COMMENT '粉丝用户ID（谁关注）',
  `follow_user_id` bigint(20) NOT NULL COMMENT '被关注用户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0关注中 1已取关',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_follow`(`user_id` ASC, `follow_user_id` ASC) USING BTREE,
  INDEX `idx_follow_target`(`follow_user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '关注表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_follow
-- ----------------------------
INSERT INTO `biz_follow` VALUES (1, 1006, 1001, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_follow` VALUES (2, 1007, 1001, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_follow` VALUES (3, 1008, 1001, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_follow` VALUES (4, 1009, 1001, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_follow` VALUES (5, 1010, 1001, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_follow` VALUES (6, 1006, 1002, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_follow` VALUES (7, 1007, 1002, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_follow` VALUES (8, 1011, 1002, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_follow` VALUES (9, 1012, 1002, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_follow` VALUES (10, 1008, 1003, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_follow` VALUES (11, 1013, 1003, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_follow` VALUES (12, 1006, 1003, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_follow` VALUES (13, 1009, 1004, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_follow` VALUES (14, 1014, 1004, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_follow` VALUES (15, 1007, 1004, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_follow` VALUES (16, 1010, 1005, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_follow` VALUES (17, 1015, 1005, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_follow` VALUES (18, 1006, 1005, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_follow` VALUES (19, 1011, 1005, '2026-09-11 22:30:23', 0);

-- ----------------------------
-- Table structure for biz_like
-- ----------------------------
DROP TABLE IF EXISTS `biz_like`;
CREATE TABLE `biz_like`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `user_id` bigint(20) NOT NULL COMMENT '点赞用户ID',
  `target_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目标类型：video视频/comment评论',
  `target_id` bigint(20) NOT NULL COMMENT '目标ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0已赞 1已取消（软删除实现取消点赞）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_target`(`user_id` ASC, `target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_like_target`(`target_type` ASC, `target_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_like
-- ----------------------------
INSERT INTO `biz_like` VALUES (1, 1006, 'video', 1, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_like` VALUES (2, 1007, 'video', 1, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_like` VALUES (3, 1008, 'video', 1, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_like` VALUES (4, 1009, 'video', 2, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_like` VALUES (5, 1010, 'video', 3, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_like` VALUES (6, 1011, 'video', 3, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_like` VALUES (7, 1012, 'video', 5, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_like` VALUES (8, 1013, 'video', 5, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_like` VALUES (9, 1014, 'video', 6, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_like` VALUES (10, 1015, 'video', 9, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_like` VALUES (11, 1006, 'video', 9, '2026-09-11 22:30:23', 0);

-- ----------------------------
-- Table structure for biz_live_room
-- ----------------------------
DROP TABLE IF EXISTS `biz_live_room`;
CREATE TABLE `biz_live_room`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '直播间ID',
  `user_id` bigint(20) NOT NULL COMMENT '主播用户ID',
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '直播标题',
  `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面URL',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态：0未开播 1直播中 2已结束',
  `viewer_count` int(11) NOT NULL DEFAULT 0 COMMENT '观看人数（模拟）',
  `like_count` int(11) NOT NULL DEFAULT 0 COMMENT '点赞数（模拟）',
  `push_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '推流地址（高阶预留）',
  `play_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '播放地址（高阶预留）',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开播时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_live_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_live_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '直播表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_live_room
-- ----------------------------
INSERT INTO `biz_live_room` VALUES (1, 1001, '深夜食堂开播：今晚做广式糖水', NULL, 1, 320, 1600, NULL, NULL, '2026-09-11 19:00:00', NULL, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_live_room` VALUES (2, 1002, '程序员闲聊局：聊聊跳槽与涨薪', NULL, 0, 0, 0, NULL, NULL, NULL, NULL, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_live_room` VALUES (3, 1005, '北京初秋Citywalk直播', NULL, 0, 0, 0, NULL, NULL, NULL, NULL, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);

-- ----------------------------
-- Table structure for biz_message
-- ----------------------------
DROP TABLE IF EXISTS `biz_message`;
CREATE TABLE `biz_message`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '私信ID',
  `from_user_id` bigint(20) NOT NULL COMMENT '发送者ID',
  `to_user_id` bigint(20) NOT NULL COMMENT '接收者ID',
  `content` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '私信内容',
  `msg_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'text' COMMENT '消息类型：text文本/image图片/system系统',
  `is_read` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否已读：0未读 1已读',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_msg_to`(`to_user_id` ASC, `is_read` ASC) USING BTREE,
  INDEX `idx_msg_from`(`from_user_id` ASC) USING BTREE,
  INDEX `idx_msg_from_to`(`from_user_id` ASC, `to_user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '私信表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_message
-- ----------------------------
INSERT INTO `biz_message` VALUES (1, 1006, 1001, 'UP主你好，想请教一下虾饺那家的位置，谢谢！', 'text', 0, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_message` VALUES (2, 1001, 1006, '在荔湾区老字号一条街，导航搜「银记」就能到～', 'text', 1, '2026-09-11 22:30:23', 0);
INSERT INTO `biz_message` VALUES (3, 1010, 1005, 'Citywalk路线可以发一份详细版吗？', 'text', 1, '2026-09-11 22:30:23', 0);

-- ----------------------------
-- Table structure for biz_notice
-- ----------------------------
DROP TABLE IF EXISTS `biz_notice`;
CREATE TABLE `biz_notice`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告内容',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：0下线 1发布中',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_notice
-- ----------------------------
INSERT INTO `biz_notice` VALUES (1, '欢迎来到星海', '欢迎使用星海视频社交平台！「记录每一片星海」，分享你的生活、知识与快乐。', 1, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_notice` VALUES (2, '创作者认证开放申请', '现在起支持创作者认证，认证后上传配额升级至50GB，并可开通店铺带货。', 1, 2, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_notice` VALUES (3, '关于内容审核的说明', '所有视频发布后将进入人工审核，审核通过后公开可见，请遵守平台规范。', 1, 3, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);

-- ----------------------------
-- Table structure for biz_order
-- ----------------------------
DROP TABLE IF EXISTS `biz_order`;
CREATE TABLE `biz_order`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号（唯一，雪花/时间戳生成）',
  `user_id` bigint(20) NOT NULL COMMENT '下单用户ID（买家）',
  `shop_id` bigint(20) NOT NULL COMMENT '店铺ID',
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `product_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称快照',
  `product_cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品主图快照',
  `price` decimal(10, 2) NOT NULL COMMENT '成交单价快照',
  `quantity` int(11) NOT NULL DEFAULT 1 COMMENT '购买数量',
  `total_amount` decimal(12, 2) NOT NULL COMMENT '订单总金额',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态：0待付款 1已付款 2已发货 3已完成 4已取消',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货地址',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '付款时间',
  `ship_time` datetime NULL DEFAULT NULL COMMENT '发货时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_order_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_order_shop`(`shop_id` ASC) USING BTREE,
  INDEX `idx_order_user_status`(`user_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_order_shop_status`(`shop_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_order_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_order
-- ----------------------------
INSERT INTO `biz_order` VALUES (1, 'XH2026091100001', 1006, 1, 3, '星海定制保温杯', '/upload/cover/p3.svg', 59.00, 1, 59.00, 3, '广东省肇庆市端州区XX路1号', '2026-09-10 10:20:00', '2026-09-10 15:30:00', '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_order` VALUES (2, 'XH2026091100002', 1007, 2, 6, '程序员机械键盘', '/upload/cover/p6.svg', 299.00, 1, 299.00, 1, '广东省深圳市南山区XX大厦', '2026-09-11 09:00:00', NULL, '2026-09-11 22:30:23', '2026-10-14 19:11:50', 0);
INSERT INTO `biz_order` VALUES (3, 'XH2026091100003', 1008, 1, 4, '手作辣椒酱（微辣）', '/upload/cover/p4.svg', 19.90, 2, 39.80, 0, '广东省肇庆市端州区XX路2号', NULL, NULL, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);

-- ----------------------------
-- Table structure for biz_product
-- ----------------------------
DROP TABLE IF EXISTS `biz_product`;
CREATE TABLE `biz_product`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `shop_id` bigint(20) NOT NULL COMMENT '所属店铺ID',
  `product_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品主图URL',
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品描述',
  `price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '售价（元）',
  `stock` int(11) NOT NULL DEFAULT 0 COMMENT '库存',
  `sales` int(11) NOT NULL DEFAULT 0 COMMENT '销量',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态：0下架 1上架',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_product_shop`(`shop_id` ASC) USING BTREE,
  INDEX `idx_product_shop_status`(`shop_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_product_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_product
-- ----------------------------
INSERT INTO `biz_product` VALUES (1, 1, '广式虾饺皇（速冻）', '/upload/cover/p1.svg', '皮薄馅大，早餐首选', 39.90, 200, 156, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_product` VALUES (2, 1, '荔湾马蹄糕', '/upload/cover/p2.svg', '清甜爽口，广式传统糕点', 29.90, 150, 98, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_product` VALUES (3, 1, '星海定制保温杯', '/upload/cover/p3.svg', '316不锈钢，便携大容量', 59.00, 300, 210, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_product` VALUES (4, 1, '手作辣椒酱（微辣）', '/upload/cover/p4.svg', '下饭神器，无添加', 19.90, 500, 320, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_product` VALUES (5, 1, '早茶点心礼盒', '/upload/cover/p5.svg', '送礼自用两相宜', 128.00, 80, 45, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_product` VALUES (6, 2, '程序员机械键盘', '/upload/cover/p6.svg', '青轴手感，RGB背光', 299.00, 100, 62, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_product` VALUES (7, 2, '星海限量卫衣', '/upload/cover/p7.svg', '纯棉宽松版型，记录每一片星海', 129.00, 150, 88, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_product` VALUES (8, 2, '编程入门书籍组合', '/upload/cover/p8.svg', 'Java+Python入门经典', 159.00, 60, 40, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_product` VALUES (9, 2, '护眼显示器挂灯', '/upload/cover/p9.svg', '熬夜写代码必备', 89.00, 120, 75, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_product` VALUES (10, 2, '程序员咖啡豆（深烘）', '/upload/cover/p10.svg', '提神醒脑，加班续命', 49.00, 180, 130, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);

-- ----------------------------
-- Table structure for biz_report
-- ----------------------------
DROP TABLE IF EXISTS `biz_report`;
CREATE TABLE `biz_report`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `user_id` bigint(20) NOT NULL COMMENT '举报人用户ID',
  `target_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '举报对象类型：video/comment/user',
  `target_id` bigint(20) NOT NULL COMMENT '举报对象ID',
  `reason` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '举报原因分类',
  `detail` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '补充说明',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '处理状态：0待处理 1已处理 2已驳回',
  `handle_user_id` bigint(20) NULL DEFAULT NULL COMMENT '处理人（管理员）ID',
  `handle_time` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_report_status`(`status` ASC) USING BTREE,
  INDEX `idx_report_status_time`(`status` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '举报表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_report
-- ----------------------------
INSERT INTO `biz_report` VALUES (1, 1010, 'video', 20, '虚假宣传', '广告用语夸张，涉嫌诱导消费', 0, NULL, NULL, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_report` VALUES (2, 1012, 'video', 12, '低俗内容', '摸鱼技巧视频可能诱导职场违规', 0, NULL, NULL, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);

-- ----------------------------
-- Table structure for biz_shop
-- ----------------------------
DROP TABLE IF EXISTS `biz_shop`;
CREATE TABLE `biz_shop`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '店铺ID',
  `user_id` bigint(20) NOT NULL COMMENT '店主用户ID（创作者）',
  `shop_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '店铺名称',
  `logo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '店铺Logo URL',
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '店铺简介',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：0已关闭 1营业中',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_shop_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '店铺表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_shop
-- ----------------------------
INSERT INTO `biz_shop` VALUES (1, 1001, '星海优选', '/upload/cover/shop1.svg', '星海食光甄选好物，好吃不贵', 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_shop` VALUES (2, 1002, '码农杂货铺', '/upload/cover/shop2.svg', '程序员周边与学习资料', 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);

-- ----------------------------
-- Table structure for biz_tag
-- ----------------------------
DROP TABLE IF EXISTS `biz_tag`;
CREATE TABLE `biz_tag`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签名称',
  `category_id` bigint(20) NOT NULL COMMENT '所属分类ID',
  `hot` int(11) NOT NULL DEFAULT 0 COMMENT '热度值（推荐算法参考）',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tag_cate`(`category_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_tag
-- ----------------------------
INSERT INTO `biz_tag` VALUES (1, '美食', 1, 860, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_tag` VALUES (2, '旅行', 1, 780, 2, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_tag` VALUES (3, '日常', 1, 950, 3, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_tag` VALUES (4, '萌宠', 1, 820, 4, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_tag` VALUES (5, '学习', 2, 640, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_tag` VALUES (6, '编程', 2, 720, 2, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_tag` VALUES (7, '职场', 2, 580, 3, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_tag` VALUES (8, '健身', 2, 560, 4, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_tag` VALUES (9, '搞笑', 3, 990, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_tag` VALUES (10, '游戏', 3, 880, 2, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_tag` VALUES (11, '音乐', 3, 760, 3, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_tag` VALUES (12, '美妆', 3, 540, 4, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);

-- ----------------------------
-- Table structure for biz_video
-- ----------------------------
DROP TABLE IF EXISTS `biz_video`;
CREATE TABLE `biz_video`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '视频ID',
  `user_id` bigint(20) NOT NULL COMMENT '发布者用户ID',
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '视频标题',
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '视频简介',
  `video_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '视频文件URL',
  `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图URL',
  `duration` int(11) NOT NULL DEFAULT 0 COMMENT '视频时长（秒）',
  `category_id` bigint(20) NOT NULL COMMENT '所属分类ID',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态：0待审核 1已发布 2已下架 3审核拒绝（审核流转核心字段）',
  `reject_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核拒绝原因',
  `view_count` bigint(20) NOT NULL DEFAULT 0 COMMENT '浏览量（Redis去重后回写）',
  `like_count` bigint(20) NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` bigint(20) NOT NULL DEFAULT 0 COMMENT '评论数',
  `share_count` bigint(20) NOT NULL DEFAULT 0 COMMENT '转发数',
  `collect_count` bigint(20) NOT NULL DEFAULT 0 COMMENT '收藏数',
  `is_public` tinyint(4) NOT NULL DEFAULT 1 COMMENT '是否公开：1公开 0私密（创作者作品权限）',
  `allow_comment` tinyint(4) NOT NULL DEFAULT 1 COMMENT '是否允许评论：1允许 0禁止',
  `geo_location` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发布位置（同城频道用）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_video_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_video_cate`(`category_id` ASC) USING BTREE,
  INDEX `idx_video_status`(`status` ASC) USING BTREE,
  INDEX `idx_video_geo`(`geo_location` ASC) USING BTREE,
  INDEX `idx_video_public`(`status` ASC, `is_public` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_video_cate_status`(`category_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_video_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '视频表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_video
-- ----------------------------
INSERT INTO `biz_video` VALUES (1, 1001, '深夜食堂：一个人也要好好吃饭', '忙碌一天后，给自己煮一碗热汤面，记录每一片星海。', '/upload/video/sample1.mp4', '/upload/cover/v1.svg', 32, 1, 1, NULL, 128605, 8600, 320, 1200, 980, 1, 1, '广州', '2026-09-11 22:30:23', '2026-10-24 13:01:46', 0);
INSERT INTO `biz_video` VALUES (2, 1001, '广州塔夜景航拍，全城尽收眼底', '无人机视角下的广州塔，太震撼了！', '/upload/video/sample2.mp4', '/upload/cover/v2.svg', 45, 1, 1, NULL, 96200, 5400, 180, 860, 720, 1, 1, '广州', '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_video` VALUES (3, 1002, '10分钟搞懂TCP三次握手', '面试高频考点，用生活化类比讲清楚。', '/upload/video/sample3.mp4', '/upload/cover/v3.svg', 600, 2, 1, NULL, 215000, 12800, 640, 2600, 2100, 1, 1, '深圳', '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_video` VALUES (4, 1002, '零基础学Python：第一个程序', '从安装到print(\"Hello World\")，保姆级教程。', '/upload/video/sample4.mp4', '/upload/cover/v4.svg', 480, 2, 1, NULL, 158001, 9200, 450, 1900, 1600, 1, 1, '深圳', '2026-09-11 22:30:23', '2026-09-11 22:49:57', 0);
INSERT INTO `biz_video` VALUES (5, 1003, '当代大学生宿舍日常', '室友的迷惑行为大赏，笑不活了。', '/upload/video/sample5.mp4', '/upload/cover/v5.svg', 58, 3, 1, NULL, 310003, 25600, 980, 5200, 3100, 1, 1, '肇庆', '2026-09-11 22:30:23', '2026-10-24 14:37:02', 0);
INSERT INTO `biz_video` VALUES (6, 1003, '猫咪大战扫地机器人', '我家猫和扫地机器人打起来了！', '/upload/video/sample1.mp4', '/upload/cover/v6.svg', 42, 1, 1, NULL, 198000, 15300, 660, 3100, 2400, 1, 1, '肇庆', '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_video` VALUES (7, 1004, '面试官最爱问的10个Java问题', '大厂面试官视角，帮你避坑。', '/upload/video/sample2.mp4', '/upload/cover/v7.svg', 720, 2, 1, NULL, 132000, 7600, 380, 1500, 1300, 1, 1, '上海', '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_video` VALUES (8, 1004, '健身房新手避坑指南', '第一次去健身房不尴尬，记住这5点。', '/upload/video/sample3.mp4', '/upload/cover/v8.svg', 95, 2, 1, NULL, 88000, 5100, 220, 980, 860, 1, 1, '上海', '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_video` VALUES (9, 1005, '程序员的一天：从改bug开始', '加班到深夜的程序员自嘲，过于真实。', '/upload/video/sample4.mp4', '/upload/cover/v9.svg', 66, 3, 1, NULL, 276002, 19800, 760, 4800, 2900, 1, 1, '北京', '2026-09-11 22:30:23', '2026-10-14 20:25:12', 0);
INSERT INTO `biz_video` VALUES (10, 1005, '周末Citywalk路线推荐', '北京老胡同漫步指南，文艺感拉满。', '/upload/video/sample5.mp4', '/upload/cover/v10.svg', 120, 1, 1, NULL, 76000, 4200, 190, 860, 720, 1, 1, '北京', '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_video` VALUES (11, 1006, '第一次做饭翻车现场', '把厨房炸了（夸张），但味道居然还行？', '/upload/video/sample1.mp4', '/upload/cover/v11.svg', 38, 1, 1, NULL, 45601, 2800, 130, 520, 410, 1, 1, '广州', '2026-09-11 22:30:23', '2026-09-11 22:48:52', 0);
INSERT INTO `biz_video` VALUES (12, 1007, '办公室摸鱼小技巧（勿学）', '窗边工位摸鱼指南，老板别看我！', '/upload/video/sample2.mp4', '/upload/cover/v12.svg', 28, 3, 1, NULL, 68900, 4100, 200, 680, 530, 1, 1, '深圳', '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_video` VALUES (13, 1008, '我的桌面改造全过程', '从乱糟糟到极简风，工具清单在评论区。', '/upload/video/sample3.mp4', '/upload/cover/v13.svg', 88, 1, 1, NULL, 32600, 1900, 98, 420, 380, 1, 1, '肇庆', '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_video` VALUES (14, 1009, '通勤路上的随手拍', '地铁窗外的城市，每一帧都是生活。', '/upload/video/sample4.mp4', '/upload/cover/v14.svg', 52, 1, 1, NULL, 26800, 1600, 85, 310, 260, 1, 1, '上海', '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_video` VALUES (15, 1010, '周末去爬山，风景绝了', '山顶日出，值回早起！', '/upload/video/sample5.mp4', '/upload/cover/v15.svg', 74, 1, 1, NULL, 41200, 2500, 120, 460, 390, 1, 1, '北京', '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_video` VALUES (16, 1001, '广州早茶探店：虾饺皇', '老字号早茶探店，皮薄馅大，一口爆汁。', '/upload/video/sample1.mp4', '/upload/cover/v16.svg', 65, 1, 0, NULL, 0, 0, 0, 0, 0, 1, 1, '广州', '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_video` VALUES (17, 1002, 'Vue3组合式API精讲', 'setup语法糖、ref与reactive区别，一次讲透。', '/upload/video/sample2.mp4', '/upload/cover/v17.svg', 540, 2, 0, NULL, 0, 0, 0, 0, 0, 1, 1, '深圳', '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_video` VALUES (18, 1011, '弹唱《晴天》', '自习室楼下的即兴弹唱。', '/upload/video/sample3.mp4', '/upload/cover/v18.svg', 180, 3, 1, NULL, 52400, 3200, 150, 620, 480, 1, 1, '广州', '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `biz_video` VALUES (19, 1003, '游戏高光时刻集锦', '这把操作我给满分！', '/upload/video/sample4.mp4', '/upload/cover/v19.svg', 70, 3, 1, NULL, 112001, 8900, 340, 1600, 1100, 1, 1, '肇庆', '2026-09-11 22:30:23', '2026-09-11 22:49:46', 0);
INSERT INTO `biz_video` VALUES (20, 1004, '美妆好物分享', '平价好用的化妆刷合集，学生党冲！', '/upload/video/sample5.mp4', '/upload/cover/v20.svg', 96, 3, 2, '涉及夸大宣传用语，请修改后重新提交', 38600, 2100, 90, 260, 210, 1, 1, '上海', '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);

-- ----------------------------
-- Table structure for biz_video_tag
-- ----------------------------
DROP TABLE IF EXISTS `biz_video_tag`;
CREATE TABLE `biz_video_tag`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `video_id` bigint(20) NOT NULL COMMENT '视频ID',
  `tag_id` bigint(20) NOT NULL COMMENT '标签ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_video_tag`(`video_id` ASC, `tag_id` ASC) USING BTREE,
  INDEX `idx_tag`(`tag_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '视频标签关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_video_tag
-- ----------------------------
INSERT INTO `biz_video_tag` VALUES (1, 1, 1);
INSERT INTO `biz_video_tag` VALUES (2, 2, 2);
INSERT INTO `biz_video_tag` VALUES (3, 3, 6);
INSERT INTO `biz_video_tag` VALUES (4, 4, 5);
INSERT INTO `biz_video_tag` VALUES (5, 5, 9);
INSERT INTO `biz_video_tag` VALUES (6, 6, 4);
INSERT INTO `biz_video_tag` VALUES (7, 7, 7);
INSERT INTO `biz_video_tag` VALUES (8, 8, 8);
INSERT INTO `biz_video_tag` VALUES (9, 9, 10);
INSERT INTO `biz_video_tag` VALUES (10, 10, 2);
INSERT INTO `biz_video_tag` VALUES (11, 11, 3);
INSERT INTO `biz_video_tag` VALUES (12, 12, 9);
INSERT INTO `biz_video_tag` VALUES (13, 13, 3);
INSERT INTO `biz_video_tag` VALUES (14, 14, 3);
INSERT INTO `biz_video_tag` VALUES (15, 15, 2);
INSERT INTO `biz_video_tag` VALUES (16, 16, 1);
INSERT INTO `biz_video_tag` VALUES (17, 17, 6);
INSERT INTO `biz_video_tag` VALUES (18, 18, 11);
INSERT INTO `biz_video_tag` VALUES (19, 19, 10);
INSERT INTO `biz_video_tag` VALUES (20, 20, 12);

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限编码，如 video:upload / user:manage',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限名称',
  `type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'button' COMMENT '类型：menu菜单/button按钮/api接口',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_perm_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_permission
-- ----------------------------
INSERT INTO `sys_permission` VALUES (1, 'video:view', '浏览视频', 'api', 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_permission` VALUES (2, 'video:upload', '发布视频', 'button', 2, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_permission` VALUES (3, 'video:manage', '管理自己的视频', 'button', 3, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_permission` VALUES (4, 'video:audit', '视频审核', 'button', 4, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_permission` VALUES (5, 'user:view', '查看用户资料', 'api', 5, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_permission` VALUES (6, 'user:manage', '用户管理', 'button', 6, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_permission` VALUES (7, 'shop:manage', '店铺管理', 'button', 7, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_permission` VALUES (8, 'product:manage', '商品管理', 'button', 8, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_permission` VALUES (9, 'order:manage', '订单管理', 'button', 9, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_permission` VALUES (10, 'live:manage', '直播管理', 'button', 10, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_permission` VALUES (11, 'advert:manage', '广告管理', 'button', 11, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_permission` VALUES (12, 'report:manage', '举报处理', 'button', 12, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_permission` VALUES (13, 'notice:manage', '公告管理', 'button', 13, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_permission` VALUES (14, 'category:manage', '分类标签管理', 'button', 14, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_permission` VALUES (15, 'dashboard:view', '查看数据大盘', 'button', 15, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_permission` VALUES (16, 'comment:manage', '评论管理', 'button', 16, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_permission` VALUES (17, 'creator:view', '创作者数据看板', 'api', 17, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_permission` VALUES (18, 'system:manage', '系统权限管理', 'button', 18, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码：USER/CREATOR/ADMIN',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称：普通用户/认证创作者/超级管理员',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, 'USER', '普通用户', '默认角色：浏览、点赞、评论、发布视频（10GB配额）', '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_role` VALUES (2, 'CREATOR', '认证创作者', '在USER基础上：50GB配额、数据看板、店铺与商品管理', '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_role` VALUES (3, 'ADMIN', '超级管理员', '平台管理后台全部权限', '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `permission_id` bigint(20) NOT NULL COMMENT '权限ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_perm`(`role_id` ASC, `permission_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 30 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色权限关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_permission
-- ----------------------------
INSERT INTO `sys_role_permission` VALUES (1, 1, 1, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (2, 1, 2, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (3, 1, 3, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (4, 1, 5, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (5, 2, 1, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (6, 2, 2, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (7, 2, 3, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (8, 2, 5, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (9, 2, 7, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (10, 2, 8, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (11, 2, 9, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (12, 2, 17, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (13, 3, 1, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (14, 3, 2, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (15, 3, 3, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (16, 3, 4, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (17, 3, 5, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (18, 3, 6, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (19, 3, 7, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (20, 3, 8, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (21, 3, 9, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (22, 3, 10, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (23, 3, 11, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (24, 3, 12, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (25, 3, 13, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (26, 3, 14, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (27, 3, 15, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (28, 3, 16, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (29, 3, 17, '2026-09-11 22:30:23');
INSERT INTO `sys_role_permission` VALUES (30, 3, 18, '2026-09-11 22:30:23');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录用户名（唯一）',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（BCrypt加密存储）',
  `nickname` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `gender` tinyint(4) NOT NULL DEFAULT 0 COMMENT '性别：0未知 1男 2女',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `signature` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个性签名',
  `location` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所在城市（用于同城频道）',
  `quota_total` bigint(20) NOT NULL DEFAULT 10737418240 COMMENT '上传配额字节：普通用户10GB=10737418240',
  `quota_used` bigint(20) NOT NULL DEFAULT 0 COMMENT '已使用配额字节',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '账号状态：0禁用 1正常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1015 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1000, 'admin', '$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C', '平台管理员', '/upload/avatar/admin.svg', 0, '13800000000', 'admin@xinghai.com', '维护每一片星海', '广州', 10737418240, 0, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_user` VALUES (1001, 'creator01', '$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C', '星海食光', '/upload/avatar/c1.svg', 1, '13800000001', 'c1@xinghai.com', '美食博主｜广州', '广州', 53687091200, 524288000, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_user` VALUES (1002, 'creator02', '$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C', '代码航海家', '/upload/avatar/c2.svg', 1, '13800000002', 'c2@xinghai.com', '程序员｜分享编程知识', '深圳', 53687091200, 419430400, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_user` VALUES (1003, 'creator03', '$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C', '肇庆小霸王', '/upload/avatar/c3.svg', 1, '13800000003', 'c3@xinghai.com', '搞笑日常｜肇庆', '肇庆', 53687091200, 314572800, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_user` VALUES (1004, 'creator04', '$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C', '沪上健身君', '/upload/avatar/c4.svg', 2, '13800000004', 'c4@xinghai.com', '健身｜职场｜上海', '上海', 53687091200, 262144000, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_user` VALUES (1005, 'creator05', '$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C', '京城漫步者', '/upload/avatar/c5.svg', 1, '13800000005', 'c5@xinghai.com', '旅行博主｜北京', '北京', 53687091200, 209715200, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_user` VALUES (1006, 'user01', '$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C', '小鱼干', '/upload/avatar/u1.svg', 0, '13900000001', 'u1@xinghai.com', '', '广州', 10737418240, 0, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_user` VALUES (1007, 'user02', '$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C', '摸鱼大师', '/upload/avatar/u2.svg', 1, '13900000002', 'u2@xinghai.com', '', '深圳', 10737418240, 0, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_user` VALUES (1008, 'user03', '$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C', '桌面改造家', '/upload/avatar/u3.svg', 0, '13900000003', 'u3@xinghai.com', '', '肇庆', 10737418240, 0, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_user` VALUES (1009, 'user04', '$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C', '通勤小鹿', '/upload/avatar/u4.svg', 2, '13900000004', 'u4@xinghai.com', '', '上海', 10737418240, 0, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_user` VALUES (1010, 'user05', '$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C', '山系青年', '/upload/avatar/u5.svg', 1, '13900000005', 'u5@xinghai.com', '', '北京', 10737418240, 0, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_user` VALUES (1011, 'user06', '$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C', '弹吉他的阿泽', '/upload/avatar/u6.svg', 1, '13900000006', 'u6@xinghai.com', '', '广州', 10737418240, 0, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_user` VALUES (1012, 'user07', '$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C', '奶茶爱好者', '/upload/avatar/u7.svg', 2, '13900000007', 'u7@xinghai.com', '', '深圳', 10737418240, 0, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_user` VALUES (1013, 'user08', '$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C', '自习室常客', '/upload/avatar/u8.svg', 0, '13900000008', 'u8@xinghai.com', '', '肇庆', 10737418240, 0, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_user` VALUES (1014, 'user09', '$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C', '胶片收藏家', '/upload/avatar/u9.svg', 1, '13900000009', 'u9@xinghai.com', '', '上海', 10737418240, 0, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);
INSERT INTO `sys_user` VALUES (1015, 'user10', '$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C', '夜跑冠军', '/upload/avatar/u10.svg', 1, '13900000010', 'u10@xinghai.com', '', '北京', 10737418240, 0, 1, '2026-09-11 22:30:23', '2026-09-11 22:30:23', 0);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id` ASC, `role_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1000, 3, '2026-09-11 22:30:23');
INSERT INTO `sys_user_role` VALUES (2, 1001, 2, '2026-09-11 22:30:23');
INSERT INTO `sys_user_role` VALUES (3, 1002, 2, '2026-09-11 22:30:23');
INSERT INTO `sys_user_role` VALUES (4, 1003, 2, '2026-09-11 22:30:23');
INSERT INTO `sys_user_role` VALUES (5, 1004, 2, '2026-09-11 22:30:23');
INSERT INTO `sys_user_role` VALUES (6, 1005, 2, '2026-09-11 22:30:23');
INSERT INTO `sys_user_role` VALUES (7, 1006, 1, '2026-09-11 22:30:23');
INSERT INTO `sys_user_role` VALUES (8, 1007, 1, '2026-09-11 22:30:23');
INSERT INTO `sys_user_role` VALUES (9, 1008, 1, '2026-09-11 22:30:23');
INSERT INTO `sys_user_role` VALUES (10, 1009, 1, '2026-09-11 22:30:23');
INSERT INTO `sys_user_role` VALUES (11, 1010, 1, '2026-09-11 22:30:23');
INSERT INTO `sys_user_role` VALUES (12, 1011, 1, '2026-09-11 22:30:23');
INSERT INTO `sys_user_role` VALUES (13, 1012, 1, '2026-09-11 22:30:23');
INSERT INTO `sys_user_role` VALUES (14, 1013, 1, '2026-09-11 22:30:23');
INSERT INTO `sys_user_role` VALUES (15, 1014, 1, '2026-09-11 22:30:23');
INSERT INTO `sys_user_role` VALUES (16, 1015, 1, '2026-09-11 22:30:23');
INSERT INTO `sys_user_role` VALUES (17, 1001, 1, '2026-09-11 22:30:23');
INSERT INTO `sys_user_role` VALUES (18, 1002, 1, '2026-09-11 22:30:23');
INSERT INTO `sys_user_role` VALUES (19, 1003, 1, '2026-09-11 22:30:23');
INSERT INTO `sys_user_role` VALUES (20, 1004, 1, '2026-09-11 22:30:23');
INSERT INTO `sys_user_role` VALUES (21, 1005, 1, '2026-09-11 22:30:23');

SET FOREIGN_KEY_CHECKS = 1;
