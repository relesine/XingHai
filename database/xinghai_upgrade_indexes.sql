-- =====================================================================
-- XingHai 星海视频社交平台 · 性能优化 · 索引升级脚本（幂等，可重复执行）
-- 适用：MySQL 8.0
-- 执行方式：Navicat 中打开本脚本运行；或命令行：
--   mysql -uroot -p < xinghai_upgrade_indexes.sql
-- 说明：脚本通过 information_schema 判断索引是否已存在，缺失才创建，
--       已存在则跳过，因此可安全重复执行，不会报错。
-- 目标：为信息流/订单/评论/点赞/商品等高频查询补充复合索引，消除全表扫描。
-- =====================================================================

USE `xinghai`;

-- 存放待执行的 DDL 与结果
DROP PROCEDURE IF EXISTS `add_index_if_missing`;
DELIMITER //
CREATE PROCEDURE `add_index_if_missing`(
    IN p_table VARCHAR(64),
    IN p_index VARCHAR(64),
    IN p_ddl   VARCHAR(500)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = p_table
          AND index_name = p_index
    ) THEN
        SET @sql = p_ddl;
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        SELECT CONCAT('已创建索引: ', p_table, '.', p_index) AS result;
    ELSE
        SELECT CONCAT('索引已存在，跳过: ', p_table, '.', p_index) AS result;
    END IF;
END //
DELIMITER ;

-- ---------------------------------------------------------------------
-- 1. 视频表：信息流/审核列表高频组合查询
-- ---------------------------------------------------------------------
-- 公开信息流：WHERE status=1 AND is_public=1 AND deleted=0
CALL add_index_if_missing('biz_video','idx_video_public',
  'ALTER TABLE `biz_video` ADD INDEX `idx_video_public` (`status`,`is_public`,`deleted`)');
-- 分类页：WHERE category_id=? AND status=1
CALL add_index_if_missing('biz_video','idx_video_cate_status',
  'ALTER TABLE `biz_video` ADD INDEX `idx_video_cate_status` (`category_id`,`status`)');
-- 创作者作品/管理端按时间排序
CALL add_index_if_missing('biz_video','idx_video_create_time',
  'ALTER TABLE `biz_video` ADD INDEX `idx_video_create_time` (`create_time`)');

-- ---------------------------------------------------------------------
-- 2. 评论表：视频详情页评论列表 WHERE video_id=? AND status=1 ORDER BY create_time
-- ---------------------------------------------------------------------
CALL add_index_if_missing('biz_comment','idx_comment_video_status',
  'ALTER TABLE `biz_comment` ADD INDEX `idx_comment_video_status` (`video_id`,`status`,`create_time`)');

-- ---------------------------------------------------------------------
-- 3. 点赞/收藏表：按目标统计点赞数、批量判断状态
-- ---------------------------------------------------------------------
CALL add_index_if_missing('biz_like','idx_like_target',
  'ALTER TABLE `biz_like` ADD INDEX `idx_like_target` (`target_type`,`target_id`)');

-- ---------------------------------------------------------------------
-- 4. 订单表：买家订单/店铺订单按状态筛选
-- ---------------------------------------------------------------------
CALL add_index_if_missing('biz_order','idx_order_user_status',
  'ALTER TABLE `biz_order` ADD INDEX `idx_order_user_status` (`user_id`,`status`)');
CALL add_index_if_missing('biz_order','idx_order_shop_status',
  'ALTER TABLE `biz_order` ADD INDEX `idx_order_shop_status` (`shop_id`,`status`)');
CALL add_index_if_missing('biz_order','idx_order_create_time',
  'ALTER TABLE `biz_order` ADD INDEX `idx_order_create_time` (`create_time`)');

-- ---------------------------------------------------------------------
-- 5. 商品表：商城/店铺按状态查询
-- ---------------------------------------------------------------------
CALL add_index_if_missing('biz_product','idx_product_shop_status',
  'ALTER TABLE `biz_product` ADD INDEX `idx_product_shop_status` (`shop_id`,`status`)');
CALL add_index_if_missing('biz_product','idx_product_status',
  'ALTER TABLE `biz_product` ADD INDEX `idx_product_status` (`status`)');

-- ---------------------------------------------------------------------
-- 6. 私信表：会话双方消息查询
-- ---------------------------------------------------------------------
CALL add_index_if_missing('biz_message','idx_msg_from_to',
  'ALTER TABLE `biz_message` ADD INDEX `idx_msg_from_to` (`from_user_id`,`to_user_id`)');

-- ---------------------------------------------------------------------
-- 7. 举报表：按状态+时间筛选
-- ---------------------------------------------------------------------
CALL add_index_if_missing('biz_report','idx_report_status_time',
  'ALTER TABLE `biz_report` ADD INDEX `idx_report_status_time` (`status`,`create_time`)');

-- 清理存储过程
DROP PROCEDURE IF EXISTS `add_index_if_missing`;

-- 完成
SELECT '索引升级完成' AS result;
