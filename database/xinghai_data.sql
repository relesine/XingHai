-- =====================================================================
-- XingHai 星海视频社交平台 · 种子数据脚本（可直接执行）
-- 密码说明：种子账号密码的 BCrypt 哈希 = $2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C（明文见项目完整文档第 13 节）
-- =====================================================================
USE `xinghai`;

-- ---------- 角色 ----------
INSERT INTO `sys_role` (`id`, `code`, `name`, `remark`) VALUES
(1, 'USER',    '普通用户',   '默认角色：浏览、点赞、评论、发布视频（10GB配额）'),
(2, 'CREATOR', '认证创作者', '在USER基础上：50GB配额、数据看板、店铺与商品管理'),
(3, 'ADMIN',   '超级管理员', '平台管理后台全部权限');

-- ---------- 权限点 ----------
INSERT INTO `sys_permission` (`id`, `code`, `name`, `type`, `sort`) VALUES
(1,  'video:view',     '浏览视频',         'api',    1),
(2,  'video:upload',   '发布视频',         'button', 2),
(3,  'video:manage',   '管理自己的视频',   'button', 3),
(4,  'video:audit',    '视频审核',         'button', 4),
(5,  'user:view',      '查看用户资料',     'api',    5),
(6,  'user:manage',    '用户管理',         'button', 6),
(7,  'shop:manage',    '店铺管理',         'button', 7),
(8,  'product:manage', '商品管理',         'button', 8),
(9,  'order:manage',   '订单管理',         'button', 9),
(10, 'live:manage',    '直播管理',         'button', 10),
(11, 'advert:manage',  '广告管理',         'button', 11),
(12, 'report:manage',  '举报处理',         'button', 12),
(13, 'notice:manage',  '公告管理',         'button', 13),
(14, 'category:manage','分类标签管理',     'button', 14),
(15, 'dashboard:view', '查看数据大盘',     'button', 15),
(16, 'comment:manage', '评论管理',         'button', 16),
(17, 'creator:view',   '创作者数据看板',  'api',    17),
(18, 'system:manage',  '系统权限管理',     'button', 18);

-- ---------- 角色-权限绑定 ----------
-- 普通用户：浏览/上传/管理自己视频/看资料
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(1,1),(1,2),(1,3),(1,5);
-- 创作者：全部用户权限 + 店铺/商品/订单/数据看板
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(2,1),(2,2),(2,3),(2,5),(2,7),(2,8),(2,9),(2,17);
-- 管理员：全部权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(3,1),(3,2),(3,3),(3,4),(3,5),(3,6),(3,7),(3,8),(3,9),(3,10),(3,11),(3,12),(3,13),(3,14),(3,15),(3,16),(3,17),(3,18);

-- ---------- 用户（16个：1管理员 + 5创作者 + 10普通用户） ----------
-- 配额：普通 10GB=10737418240，创作者 50GB=53687091200
INSERT INTO `sys_user` (`id`,`username`,`password`,`nickname`,`avatar`,`gender`,`phone`,`email`,`signature`,`location`,`quota_total`,`quota_used`,`status`) VALUES
(1000,'admin','$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C','平台管理员','/upload/avatar/admin.svg',0,'13800000000','admin@xinghai.com','维护每一片星海','广州',10737418240,0,1),
(1001,'creator01','$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C','星海食光','/upload/avatar/c1.svg',1,'13800000001','c1@xinghai.com','美食博主｜广州','广州',53687091200,524288000,1),
(1002,'creator02','$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C','代码航海家','/upload/avatar/c2.svg',1,'13800000002','c2@xinghai.com','程序员｜分享编程知识','深圳',53687091200,419430400,1),
(1003,'creator03','$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C','肇庆小霸王','/upload/avatar/c3.svg',1,'13800000003','c3@xinghai.com','搞笑日常｜肇庆','肇庆',53687091200,314572800,1),
(1004,'creator04','$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C','沪上健身君','/upload/avatar/c4.svg',2,'13800000004','c4@xinghai.com','健身｜职场｜上海','上海',53687091200,262144000,1),
(1005,'creator05','$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C','京城漫步者','/upload/avatar/c5.svg',1,'13800000005','c5@xinghai.com','旅行博主｜北京','北京',53687091200,209715200,1),
(1006,'user01','$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C','小鱼干','/upload/avatar/u1.svg',0,'13900000001','u1@xinghai.com','','广州',10737418240,0,1),
(1007,'user02','$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C','摸鱼大师','/upload/avatar/u2.svg',1,'13900000002','u2@xinghai.com','','深圳',10737418240,0,1),
(1008,'user03','$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C','桌面改造家','/upload/avatar/u3.svg',0,'13900000003','u3@xinghai.com','','肇庆',10737418240,0,1),
(1009,'user04','$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C','通勤小鹿','/upload/avatar/u4.svg',2,'13900000004','u4@xinghai.com','','上海',10737418240,0,1),
(1010,'user05','$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C','山系青年','/upload/avatar/u5.svg',1,'13900000005','u5@xinghai.com','','北京',10737418240,0,1),
(1011,'user06','$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C','弹吉他的阿泽','/upload/avatar/u6.svg',1,'13900000006','u6@xinghai.com','','广州',10737418240,0,1),
(1012,'user07','$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C','奶茶爱好者','/upload/avatar/u7.svg',2,'13900000007','u7@xinghai.com','','深圳',10737418240,0,1),
(1013,'user08','$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C','自习室常客','/upload/avatar/u8.svg',0,'13900000008','u8@xinghai.com','','肇庆',10737418240,0,1),
(1014,'user09','$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C','胶片收藏家','/upload/avatar/u9.svg',1,'13900000009','u9@xinghai.com','','上海',10737418240,0,1),
(1015,'user10','$2b$10$pt29x0tKa9Kv.mHyefqvHO58VQWxDaVA7lHpGDpzWWBSacrsg2e4C','夜跑冠军','/upload/avatar/u10.svg',1,'13900000010','u10@xinghai.com','','北京',10737418240,0,1);

-- ---------- 用户-角色绑定 ----------
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1000,3),
(1001,2),(1002,2),(1003,2),(1004,2),(1005,2),
(1006,1),(1007,1),(1008,1),(1009,1),(1010,1),(1011,1),(1012,1),(1013,1),(1014,1),(1015,1);
-- 创作者同时拥有普通用户角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1001,1),(1002,1),(1003,1),(1004,1),(1005,1);

-- ---------- 视频分类（3个一级分类） ----------
INSERT INTO `biz_category` (`id`, `name`, `sort`, `status`) VALUES
(1, '生活记录', 1, 1),
(2, '知识分享', 2, 1),
(3, '娱乐搞笑', 3, 1);

-- ---------- 标签（12个，每个分类4个） ----------
INSERT INTO `biz_tag` (`id`, `name`, `category_id`, `hot`, `sort`) VALUES
(1,'美食',1,860,1),(2,'旅行',1,780,2),(3,'日常',1,950,3),(4,'萌宠',1,820,4),
(5,'学习',2,640,1),(6,'编程',2,720,2),(7,'职场',2,580,3),(8,'健身',2,560,4),
(9,'搞笑',3,990,1),(10,'游戏',3,880,2),(11,'音乐',3,760,3),(12,'美妆',3,540,4);

-- ---------- 示例视频（20条，状态：0待审核 1已发布 2已下架 3审核拒绝） ----------
INSERT INTO `biz_video` (`id`,`user_id`,`title`,`description`,`video_url`,`cover_url`,`duration`,`category_id`,`status`,`reject_reason`,`view_count`,`like_count`,`comment_count`,`share_count`,`collect_count`,`is_public`,`allow_comment`,`geo_location`) VALUES
(1,1001,'深夜食堂：一个人也要好好吃饭','忙碌一天后，给自己煮一碗热汤面，记录每一片星海。','/upload/video/sample1.mp4','/upload/cover/v1.svg',32,1,1,NULL,128600,8600,320,1200,980,1,1,'广州'),
(2,1001,'广州塔夜景航拍，全城尽收眼底','无人机视角下的广州塔，太震撼了！','/upload/video/sample2.mp4','/upload/cover/v2.svg',45,1,1,NULL,96200,5400,180,860,720,1,1,'广州'),
(3,1002,'10分钟搞懂TCP三次握手','面试高频考点，用生活化类比讲清楚。','/upload/video/sample3.mp4','/upload/cover/v3.svg',600,2,1,NULL,215000,12800,640,2600,2100,1,1,'深圳'),
(4,1002,'零基础学Python：第一个程序','从安装到print(\"Hello World\")，保姆级教程。','/upload/video/sample4.mp4','/upload/cover/v4.svg',480,2,1,NULL,158000,9200,450,1900,1600,1,1,'深圳'),
(5,1003,'当代大学生宿舍日常','室友的迷惑行为大赏，笑不活了。','/upload/video/sample5.mp4','/upload/cover/v5.svg',58,3,1,NULL,310000,25600,980,5200,3100,1,1,'肇庆'),
(6,1003,'猫咪大战扫地机器人','我家猫和扫地机器人打起来了！','/upload/video/sample1.mp4','/upload/cover/v6.svg',42,1,1,NULL,198000,15300,660,3100,2400,1,1,'肇庆'),
(7,1004,'面试官最爱问的10个Java问题','大厂面试官视角，帮你避坑。','/upload/video/sample2.mp4','/upload/cover/v7.svg',720,2,1,NULL,132000,7600,380,1500,1300,1,1,'上海'),
(8,1004,'健身房新手避坑指南','第一次去健身房不尴尬，记住这5点。','/upload/video/sample3.mp4','/upload/cover/v8.svg',95,2,1,NULL,88000,5100,220,980,860,1,1,'上海'),
(9,1005,'程序员的一天：从改bug开始','加班到深夜的程序员自嘲，过于真实。','/upload/video/sample4.mp4','/upload/cover/v9.svg',66,3,1,NULL,276000,19800,760,4800,2900,1,1,'北京'),
(10,1005,'周末Citywalk路线推荐','北京老胡同漫步指南，文艺感拉满。','/upload/video/sample5.mp4','/upload/cover/v10.svg',120,1,1,NULL,76000,4200,190,860,720,1,1,'北京'),
(11,1006,'第一次做饭翻车现场','把厨房炸了（夸张），但味道居然还行？','/upload/video/sample1.mp4','/upload/cover/v11.svg',38,1,1,NULL,45600,2800,130,520,410,1,1,'广州'),
(12,1007,'办公室摸鱼小技巧（勿学）','窗边工位摸鱼指南，老板别看我！','/upload/video/sample2.mp4','/upload/cover/v12.svg',28,3,1,NULL,68900,4100,200,680,530,1,1,'深圳'),
(13,1008,'我的桌面改造全过程','从乱糟糟到极简风，工具清单在评论区。','/upload/video/sample3.mp4','/upload/cover/v13.svg',88,1,1,NULL,32600,1900,98,420,380,1,1,'肇庆'),
(14,1009,'通勤路上的随手拍','地铁窗外的城市，每一帧都是生活。','/upload/video/sample4.mp4','/upload/cover/v14.svg',52,1,1,NULL,26800,1600,85,310,260,1,1,'上海'),
(15,1010,'周末去爬山，风景绝了','山顶日出，值回早起！','/upload/video/sample5.mp4','/upload/cover/v15.svg',74,1,1,NULL,41200,2500,120,460,390,1,1,'北京'),
(16,1001,'广州早茶探店：虾饺皇','老字号早茶探店，皮薄馅大，一口爆汁。','/upload/video/sample1.mp4','/upload/cover/v16.svg',65,1,0,NULL,0,0,0,0,0,1,1,'广州'),
(17,1002,'Vue3组合式API精讲','setup语法糖、ref与reactive区别，一次讲透。','/upload/video/sample2.mp4','/upload/cover/v17.svg',540,2,0,NULL,0,0,0,0,0,1,1,'深圳'),
(18,1011,'弹唱《晴天》','自习室楼下的即兴弹唱。','/upload/video/sample3.mp4','/upload/cover/v18.svg',180,3,1,NULL,52400,3200,150,620,480,1,1,'广州'),
(19,1003,'游戏高光时刻集锦','这把操作我给满分！','/upload/video/sample4.mp4','/upload/cover/v19.svg',70,3,1,NULL,112000,8900,340,1600,1100,1,1,'肇庆'),
(20,1004,'美妆好物分享','平价好用的化妆刷合集，学生党冲！','/upload/video/sample5.mp4','/upload/cover/v20.svg',96,3,2,'涉及夸大宣传用语，请修改后重新提交',38600,2100,90,260,210,1,1,'上海');

-- ---------- 视频-标签关联 ----------
INSERT INTO `biz_video_tag` (`video_id`, `tag_id`) VALUES
(1,1),(2,2),(3,6),(4,5),(5,9),(6,4),(7,7),(8,8),(9,10),(10,2),
(11,3),(12,9),(13,3),(14,3),(15,2),(16,1),(17,6),(18,11),(19,10),(20,12);

-- ---------- 关注关系（示例：用户关注创作者） ----------
INSERT INTO `biz_follow` (`user_id`, `follow_user_id`) VALUES
(1006,1001),(1007,1001),(1008,1001),(1009,1001),(1010,1001),
(1006,1002),(1007,1002),(1011,1002),(1012,1002),
(1008,1003),(1013,1003),(1006,1003),
(1009,1004),(1014,1004),(1007,1004),
(1010,1005),(1015,1005),(1006,1005),(1011,1005);

-- ---------- 示例评论 ----------
INSERT INTO `biz_comment` (`id`,`video_id`,`user_id`,`parent_id`,`content`,`like_count`,`status`) VALUES
(1,1,1006,0,'看起来好好吃！求教程！',26,1),
(2,1,1007,1,'同求教程+1',8,1),
(3,1,1008,0,'一个人也要好好吃饭，泪目',15,1),
(4,3,1009,0,'终于有人讲清楚了，收藏了',42,1),
(5,3,1010,0,'类比太形象了，感谢UP主',18,1),
(6,5,1011,0,'哈哈哈哈哈太真实了',55,1),
(7,5,1012,0,'最后那个镜头绝了',12,1),
(8,9,1013,0,'程序员看完沉默了……',37,1);

-- ---------- 示例点赞（video类型） ----------
INSERT INTO `biz_like` (`user_id`,`target_type`,`target_id`) VALUES
(1006,'video',1),(1007,'video',1),(1008,'video',1),(1009,'video',2),(1010,'video',3),
(1011,'video',3),(1012,'video',5),(1013,'video',5),(1014,'video',6),(1015,'video',9),(1006,'video',9);

-- ---------- 示例私信 ----------
INSERT INTO `biz_message` (`from_user_id`,`to_user_id`,`content`,`msg_type`,`is_read`) VALUES
(1006,1001,'UP主你好，想请教一下虾饺那家的位置，谢谢！','text',0),
(1001,1006,'在荔湾区老字号一条街，导航搜「银记」就能到～','text',0),
(1010,1005,'Citywalk路线可以发一份详细版吗？','text',1);

-- ---------- 直播间（基础模拟，creator01直播中） ----------
INSERT INTO `biz_live_room` (`id`,`user_id`,`title`,`cover_url`,`status`,`viewer_count`,`like_count`,`start_time`) VALUES
(1,1001,'深夜食堂开播：今晚做广式糖水',NULL,1,320,1600,'2026-09-11 19:00:00'),
(2,1002,'程序员闲聊局：聊聊跳槽与涨薪',NULL,0,0,0,NULL),
(3,1005,'北京初秋Citywalk直播',NULL,0,0,0,NULL);

-- ---------- 店铺（2家示例店铺） ----------
INSERT INTO `biz_shop` (`id`,`user_id`,`shop_name`,`logo`,`description`,`status`) VALUES
(1,1001,'星海优选','/upload/cover/shop1.svg','星海食光甄选好物，好吃不贵',1),
(2,1002,'码农杂货铺','/upload/cover/shop2.svg','程序员周边与学习资料',1);

-- ---------- 商品（10件） ----------
INSERT INTO `biz_product` (`id`,`shop_id`,`product_name`,`cover_url`,`description`,`price`,`stock`,`sales`,`status`) VALUES
(1,1,'广式虾饺皇（速冻）','/upload/cover/p1.svg','皮薄馅大，早餐首选',39.90,200,156,1),
(2,1,'荔湾马蹄糕','/upload/cover/p2.svg','清甜爽口，广式传统糕点',29.90,150,98,1),
(3,1,'星海定制保温杯','/upload/cover/p3.svg','316不锈钢，便携大容量',59.00,300,210,1),
(4,1,'手作辣椒酱（微辣）','/upload/cover/p4.svg','下饭神器，无添加',19.90,500,320,1),
(5,1,'早茶点心礼盒','/upload/cover/p5.svg','送礼自用两相宜',128.00,80,45,1),
(6,2,'程序员机械键盘','/upload/cover/p6.svg','青轴手感，RGB背光',299.00,100,62,1),
(7,2,'星海限量卫衣','/upload/cover/p7.svg','纯棉宽松版型，记录每一片星海',129.00,150,88,1),
(8,2,'编程入门书籍组合','/upload/cover/p8.svg','Java+Python入门经典',159.00,60,40,1),
(9,2,'护眼显示器挂灯','/upload/cover/p9.svg','熬夜写代码必备',89.00,120,75,1),
(10,2,'程序员咖啡豆（深烘）','/upload/cover/p10.svg','提神醒脑，加班续命',49.00,180,130,1);

-- ---------- 示例订单（含订单状态流转演示） ----------
INSERT INTO `biz_order` (`id`,`order_no`,`user_id`,`shop_id`,`product_id`,`product_name`,`product_cover`,`price`,`quantity`,`total_amount`,`status`,`address`,`pay_time`,`ship_time`) VALUES
(1,'XH2026091100001',1006,1,3,'星海定制保温杯','/upload/cover/p3.svg',59.00,1,59.00,3,'广东省肇庆市端州区XX路1号','2026-09-10 10:20:00','2026-09-10 15:30:00'),
(2,'XH2026091100002',1007,2,6,'程序员机械键盘','/upload/cover/p6.svg',299.00,1,299.00,1,'广东省深圳市南山区XX大厦','2026-09-11 09:00:00',NULL),
(3,'XH2026091100003',1008,1,4,'手作辣椒酱（微辣）','/upload/cover/p4.svg',19.90,2,39.80,0,'广东省肇庆市端州区XX路2号',NULL,NULL);

-- ---------- 广告（2条） ----------
INSERT INTO `biz_advert` (`id`,`title`,`image_url`,`link_url`,`position`,`status`,`sort`) VALUES
(1,'星海优选 早茶点心节','/upload/cover/ad1.svg','https://www.xinghai.com/shop/1','home',1,1),
(2,'程序员键盘限时优惠','/upload/cover/ad2.svg','https://www.xinghai.com/shop/2','home',1,2);

-- ---------- 举报（2条待处理演示） ----------
INSERT INTO `biz_report` (`id`,`user_id`,`target_type`,`target_id`,`reason`,`detail`,`status`) VALUES
(1,1010,'video',20,'虚假宣传','广告用语夸张，涉嫌诱导消费',0),
(2,1012,'video',12,'低俗内容','摸鱼技巧视频可能诱导职场违规',0);

-- ---------- 公告（3条） ----------
INSERT INTO `biz_notice` (`id`,`title`,`content`,`status`,`sort`) VALUES
(1,'欢迎来到星海','欢迎使用星海视频社交平台！「记录每一片星海」，分享你的生活、知识与快乐。',1,1),
(2,'创作者认证开放申请','现在起支持创作者认证，认证后上传配额升级至50GB，并可开通店铺带货。',1,2),
(3,'关于内容审核的说明','所有视频发布后将进入人工审核，审核通过后公开可见，请遵守平台规范。',1,3);

-- ---------- 创作者数据统计（近7天，creator01/02/03） ----------
INSERT INTO `biz_creator_stat` (`user_id`,`stat_date`,`views`,`likes`,`fans`,`comments`,`sales_amount`) VALUES
(1001,'2026-09-05',1200,86,1560,42,320.00),
(1001,'2026-09-06',1500,102,1610,50,480.50),
(1001,'2026-09-07',980,64,1660,35,260.00),
(1001,'2026-09-08',2100,150,1715,68,520.00),
(1001,'2026-09-09',1850,128,1760,55,410.00),
(1001,'2026-09-10',2400,172,1810,72,680.00),
(1001,'2026-09-11',1600,110,1840,48,390.00),
(1002,'2026-09-05',2300,180,920,88,0.00),
(1002,'2026-09-06',2800,210,980,102,0.00),
(1002,'2026-09-07',1900,150,1040,76,0.00),
(1002,'2026-09-08',3500,280,1120,130,0.00),
(1002,'2026-09-09',3100,240,1190,115,0.00),
(1002,'2026-09-10',4200,330,1280,150,0.00),
(1002,'2026-09-11',2700,200,1330,96,0.00),
(1003,'2026-09-05',1600,120,680,60,0.00),
(1003,'2026-09-06',1900,145,720,71,0.00),
(1003,'2026-09-07',1300,95,760,52,0.00),
(1003,'2026-09-08',2600,200,810,95,0.00),
(1003,'2026-09-09',2200,170,850,80,0.00),
(1003,'2026-09-10',3000,230,900,110,0.00),
(1003,'2026-09-11',2000,150,930,75,0.00);

-- 种子数据导入完成
