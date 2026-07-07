-- ============================================
-- Smart Restaurant Order Food System - Initial Data
-- ============================================

USE `order_food`;

-- --------------------------------------------
-- Admin User (password: admin123, BCrypt hashed)
-- --------------------------------------------
INSERT INTO `admin_user` (`username`, `password`, `name`) VALUES
    ('admin', '$2b$10$aAfHO8nDxutJL8/m6o7lq.9KBiy0CozrwIFH1IRGf0jOm2J3x9DIG', '系统管理员');

-- --------------------------------------------
-- Categories (5 categories)
-- --------------------------------------------
INSERT INTO `category` (`name`, `sort`, `status`) VALUES
    ('热菜', 1, 1),
    ('凉菜', 2, 1),
    ('主食', 3, 1),
    ('汤/羹', 4, 1),
    ('饮品', 5, 1);

-- --------------------------------------------
-- Dining Tables (5 tables)
-- --------------------------------------------
INSERT INTO `dining_table` (`code`, `name`, `capacity`, `status`) VALUES
    ('A1', 'A区1号桌', 4, 0),
    ('A2', 'A区2号桌', 4, 0),
    ('B1', 'B区1号桌', 6, 0),
    ('B2', 'B区2号桌', 6, 0),
    ('C1', 'C区1号包间', 8, 0);

-- --------------------------------------------
-- Dishes (34 dishes across all categories)
-- --------------------------------------------

-- 热菜 (category_id=1) — 辣度(必选)+香菜(可选)+葱花(可选)
INSERT INTO `dish` (`category_id`, `name`, `price`, `image`, `description`, `taste_config`, `stock`, `status`) VALUES
    (1, '红烧肉', 38.00, 'https://picsum.photos/seed/hongshaorou/300/300', '精选五花肉，肥而不腻，入口即化，浓油赤酱经典本帮味', '[{"name":"辣度","type":"single","required":true,"options":[{"label":"不辣","value":"不辣"},{"label":"微辣","value":"微辣"},{"label":"中辣","value":"中辣"},{"label":"特辣","value":"特辣"}]},{"name":"香菜","type":"single","required":false,"options":[{"label":"加香菜","value":"加香菜"},{"label":"不要香菜","value":"不要香菜"}]},{"name":"葱花","type":"single","required":false,"options":[{"label":"加葱花","value":"加葱花"},{"label":"不要葱花","value":"不要葱花"}]}]', 999, 1),
    (1, '宫保鸡丁', 32.00, 'https://picsum.photos/seed/gongbao/300/300', '鸡胸肉切丁，花生米酥脆，微辣微甜，下饭神器', '[{"name":"辣度","type":"single","required":true,"options":[{"label":"不辣","value":"不辣"},{"label":"微辣","value":"微辣"},{"label":"中辣","value":"中辣"},{"label":"特辣","value":"特辣"}]},{"name":"香菜","type":"single","required":false,"options":[{"label":"加香菜","value":"加香菜"},{"label":"不要香菜","value":"不要香菜"}]},{"name":"葱花","type":"single","required":false,"options":[{"label":"加葱花","value":"加葱花"},{"label":"不要葱花","value":"不要葱花"}]}]', 999, 1),
    (1, '水煮牛肉', 58.00, 'https://picsum.photos/seed/shuizhu/300/300', '鲜嫩牛肉片，麻辣鲜香，红油滚滚，嗜辣星人必点', '[{"name":"辣度","type":"single","required":true,"options":[{"label":"不辣","value":"不辣"},{"label":"微辣","value":"微辣"},{"label":"中辣","value":"中辣"},{"label":"特辣","value":"特辣"}]},{"name":"香菜","type":"single","required":false,"options":[{"label":"加香菜","value":"加香菜"},{"label":"不要香菜","value":"不要香菜"}]},{"name":"葱花","type":"single","required":false,"options":[{"label":"加葱花","value":"加葱花"},{"label":"不要葱花","value":"不要葱花"}]}]', 999, 1),
    (1, '糖醋排骨', 42.00, 'https://picsum.photos/seed/tangcupaigu/300/300', '小排炸透后裹糖醋汁，外酥里嫩，酸甜适口', '[{"name":"辣度","type":"single","required":true,"options":[{"label":"不辣","value":"不辣"},{"label":"微辣","value":"微辣"},{"label":"中辣","value":"中辣"},{"label":"特辣","value":"特辣"}]},{"name":"香菜","type":"single","required":false,"options":[{"label":"加香菜","value":"加香菜"},{"label":"不要香菜","value":"不要香菜"}]},{"name":"葱花","type":"single","required":false,"options":[{"label":"加葱花","value":"加葱花"},{"label":"不要葱花","value":"不要葱花"}]}]', 999, 1),
    (1, '麻婆豆腐', 26.00, 'https://picsum.photos/seed/mapodoufu/300/300', '嫩豆腐配牛肉末，麻辣鲜香烫，地道川味', '[{"name":"辣度","type":"single","required":true,"options":[{"label":"不辣","value":"不辣"},{"label":"微辣","value":"微辣"},{"label":"中辣","value":"中辣"},{"label":"特辣","value":"特辣"}]},{"name":"香菜","type":"single","required":false,"options":[{"label":"加香菜","value":"加香菜"},{"label":"不要香菜","value":"不要香菜"}]},{"name":"葱花","type":"single","required":false,"options":[{"label":"加葱花","value":"加葱花"},{"label":"不要葱花","value":"不要葱花"}]}]', 999, 1),
    (1, '鱼香肉丝', 28.00, 'https://picsum.photos/seed/yuxiang/300/300', '木耳笋丝肉丝，鱼香汁浓郁，虽无鱼却有鱼味', '[{"name":"辣度","type":"single","required":true,"options":[{"label":"不辣","value":"不辣"},{"label":"微辣","value":"微辣"},{"label":"中辣","value":"中辣"},{"label":"特辣","value":"特辣"}]},{"name":"香菜","type":"single","required":false,"options":[{"label":"加香菜","value":"加香菜"},{"label":"不要香菜","value":"不要香菜"}]},{"name":"葱花","type":"single","required":false,"options":[{"label":"加葱花","value":"加葱花"},{"label":"不要葱花","value":"不要葱花"}]}]', 999, 1),
    (1, '回锅肉', 36.00, 'https://picsum.photos/seed/huiguo/300/300', '二刀肉配蒜苗青椒，先煮后炒，肥而不腻', '[{"name":"辣度","type":"single","required":true,"options":[{"label":"不辣","value":"不辣"},{"label":"微辣","value":"微辣"},{"label":"中辣","value":"中辣"},{"label":"特辣","value":"特辣"}]},{"name":"香菜","type":"single","required":false,"options":[{"label":"加香菜","value":"加香菜"},{"label":"不要香菜","value":"不要香菜"}]},{"name":"葱花","type":"single","required":false,"options":[{"label":"加葱花","value":"加葱花"},{"label":"不要葱花","value":"不要葱花"}]}]', 999, 1),
    (1, '铁板牛柳', 68.00, 'https://picsum.photos/seed/tieban/300/300', '铁板上滋滋作响的黑椒牛柳，肉质嫩滑多汁', '[{"name":"辣度","type":"single","required":true,"options":[{"label":"不辣","value":"不辣"},{"label":"微辣","value":"微辣"},{"label":"中辣","value":"中辣"},{"label":"特辣","value":"特辣"}]},{"name":"香菜","type":"single","required":false,"options":[{"label":"加香菜","value":"加香菜"},{"label":"不要香菜","value":"不要香菜"}]},{"name":"葱花","type":"single","required":false,"options":[{"label":"加葱花","value":"加葱花"},{"label":"不要葱花","value":"不要葱花"}]}]', 999, 1);

-- 凉菜 (category_id=2) — 辣度(必选)+香菜(可选)
INSERT INTO `dish` (`category_id`, `name`, `price`, `image`, `description`, `taste_config`, `stock`, `status`) VALUES
    (2, '口水鸡', 32.00, 'https://picsum.photos/seed/koushuiji/300/300', '嫩滑鸡肉浇红油花椒汁，麻辣鲜香，让人口水直流', '[{"name":"辣度","type":"single","required":true,"options":[{"label":"不辣","value":"不辣"},{"label":"微辣","value":"微辣"},{"label":"中辣","value":"中辣"}]},{"name":"香菜","type":"single","required":false,"options":[{"label":"加香菜","value":"加香菜"},{"label":"不要香菜","value":"不要香菜"}]}]', 999, 1),
    (2, '凉拌黄瓜', 12.00, 'https://picsum.photos/seed/huanggua/300/300', '拍黄瓜拌蒜泥香醋，清脆爽口，开胃首选', '[{"name":"辣度","type":"single","required":true,"options":[{"label":"不辣","value":"不辣"},{"label":"微辣","value":"微辣"},{"label":"中辣","value":"中辣"}]},{"name":"香菜","type":"single","required":false,"options":[{"label":"加香菜","value":"加香菜"},{"label":"不要香菜","value":"不要香菜"}]}]', 999, 1),
    (2, '夫妻肺片', 38.00, 'https://picsum.photos/seed/feipian/300/300', '牛肉牛杂切片，红油花生碎，麻辣鲜香', '[{"name":"辣度","type":"single","required":true,"options":[{"label":"不辣","value":"不辣"},{"label":"微辣","value":"微辣"},{"label":"中辣","value":"中辣"}]},{"name":"香菜","type":"single","required":false,"options":[{"label":"加香菜","value":"加香菜"},{"label":"不要香菜","value":"不要香菜"}]}]', 999, 1),
    (2, '白斩鸡', 42.00, 'https://picsum.photos/seed/baizhan/300/300', '三黄鸡水煮后冰镇，皮黄肉白，蘸姜葱酱', '[{"name":"辣度","type":"single","required":true,"options":[{"label":"不辣","value":"不辣"},{"label":"微辣","value":"微辣"},{"label":"中辣","value":"中辣"}]},{"name":"香菜","type":"single","required":false,"options":[{"label":"加香菜","value":"加香菜"},{"label":"不要香菜","value":"不要香菜"}]}]', 999, 1),
    (2, '凉拌木耳', 16.00, 'https://picsum.photos/seed/muer/300/300', '黑木耳拌洋葱青红椒，脆爽酸辣，清爽解腻', '[{"name":"辣度","type":"single","required":true,"options":[{"label":"不辣","value":"不辣"},{"label":"微辣","value":"微辣"},{"label":"中辣","value":"中辣"}]},{"name":"香菜","type":"single","required":false,"options":[{"label":"加香菜","value":"加香菜"},{"label":"不要香菜","value":"不要香菜"}]}]', 999, 1),
    (2, '皮蛋豆腐', 14.00, 'https://picsum.photos/seed/pidan/300/300', '嫩豆腐配皮蛋，淋生抽香油，简单却美味', '[{"name":"辣度","type":"single","required":true,"options":[{"label":"不辣","value":"不辣"},{"label":"微辣","value":"微辣"},{"label":"中辣","value":"中辣"}]},{"name":"香菜","type":"single","required":false,"options":[{"label":"加香菜","value":"加香菜"},{"label":"不要香菜","value":"不要香菜"}]}]', 999, 1),
    (2, '酸辣凤爪', 22.00, 'https://picsum.photos/seed/fengzhua/300/300', '去骨鸡爪泡酸辣汁，Q弹爽脆，越吃越上瘾', '[{"name":"辣度","type":"single","required":true,"options":[{"label":"不辣","value":"不辣"},{"label":"微辣","value":"微辣"},{"label":"中辣","value":"中辣"}]},{"name":"香菜","type":"single","required":false,"options":[{"label":"加香菜","value":"加香菜"},{"label":"不要香菜","value":"不要香菜"}]}]', 999, 1);

-- 主食 (category_id=3) — 葱花(可选)
INSERT INTO `dish` (`category_id`, `name`, `price`, `image`, `description`, `taste_config`, `stock`, `status`) VALUES
    (3, '蛋炒饭', 18.00, 'https://picsum.photos/seed/danchaofan/300/300', '粒粒分明的蛋炒饭，蛋香四溢，简单经典', '[{"name":"葱花","type":"single","required":false,"options":[{"label":"加葱花","value":"加葱花"},{"label":"不要葱花","value":"不要葱花"}]}]', 999, 1),
    (3, '担担面', 22.00, 'https://picsum.photos/seed/dandanmian/300/300', '细面拌肉末芽菜，麻辣鲜香，一口入魂', '[{"name":"葱花","type":"single","required":false,"options":[{"label":"加葱花","value":"加葱花"},{"label":"不要葱花","value":"不要葱花"}]}]', 999, 1),
    (3, '牛肉拉面', 28.00, 'https://picsum.photos/seed/niuroulamian/300/300', '手工拉面配红烧牛腩，汤浓面劲，大口满足', '[{"name":"葱花","type":"single","required":false,"options":[{"label":"加葱花","value":"加葱花"},{"label":"不要葱花","value":"不要葱花"}]}]', 999, 1),
    (3, '蒸饺', 20.00, 'https://picsum.photos/seed/zhengjiao/300/300', '手工蒸饺，薄皮大馅，一笼十二只', '[{"name":"葱花","type":"single","required":false,"options":[{"label":"加葱花","value":"加葱花"},{"label":"不要葱花","value":"不要葱花"}]}]', 999, 1),
    (3, '生煎包', 18.00, 'https://picsum.photos/seed/shengjian/300/300', '底部金黄酥脆，咬一口汤汁四溢，上海经典', '[{"name":"葱花","type":"single","required":false,"options":[{"label":"加葱花","value":"加葱花"},{"label":"不要葱花","value":"不要葱花"}]}]', 999, 1),
    (3, '扬州炒饭', 26.00, 'https://picsum.photos/seed/yangzhou/300/300', '虾仁火腿青豆蛋，色彩丰富，鲜香满口', '[{"name":"葱花","type":"single","required":false,"options":[{"label":"加葱花","value":"加葱花"},{"label":"不要葱花","value":"不要葱花"}]}]', 999, 1),
    (3, '葱油拌面', 16.00, 'https://picsum.photos/seed/congyou/300/300', '细面拌葱油酱油，简简单单就是好吃', '[{"name":"葱花","type":"single","required":false,"options":[{"label":"加葱花","value":"加葱花"},{"label":"不要葱花","value":"不要葱花"}]}]', 999, 1);

-- 汤/羹 (category_id=4) — 葱花(可选)
INSERT INTO `dish` (`category_id`, `name`, `price`, `image`, `description`, `taste_config`, `stock`, `status`) VALUES
    (4, '番茄蛋汤', 12.00, 'https://picsum.photos/seed/fanqiedantang/300/300', '酸甜番茄配蛋花，开胃暖胃，老少皆宜', '[{"name":"葱花","type":"single","required":false,"options":[{"label":"加葱花","value":"加葱花"},{"label":"不要葱花","value":"不要葱花"}]}]', 999, 1),
    (4, '紫菜蛋花汤', 10.00, 'https://picsum.photos/seed/zicai/300/300', '紫菜蛋花，清淡鲜美，来一碗暖暖的', '[{"name":"葱花","type":"single","required":false,"options":[{"label":"加葱花","value":"加葱花"},{"label":"不要葱花","value":"不要葱花"}]}]', 999, 1),
    (4, '酸辣汤', 16.00, 'https://picsum.photos/seed/suanlatang/300/300', '木耳豆腐丝肉丝，酸辣开胃，层次丰富', '[{"name":"葱花","type":"single","required":false,"options":[{"label":"加葱花","value":"加葱花"},{"label":"不要葱花","value":"不要葱花"}]}]', 999, 1),
    (4, '排骨莲藕汤', 32.00, 'https://picsum.photos/seed/paigutang/300/300', '猪排骨炖莲藕，汤清味鲜，藕粉肉烂', '[{"name":"葱花","type":"single","required":false,"options":[{"label":"加葱花","value":"加葱花"},{"label":"不要葱花","value":"不要葱花"}]}]', 999, 1),
    (4, '鲫鱼豆腐汤', 36.00, 'https://picsum.photos/seed/jiyutang/300/300', '鲜鲫鱼炖嫩豆腐，汤色奶白，鲜美无比', '[{"name":"葱花","type":"single","required":false,"options":[{"label":"加葱花","value":"加葱花"},{"label":"不要葱花","value":"不要葱花"}]}]', 999, 1),
    (4, '玉米排骨汤', 28.00, 'https://picsum.photos/seed/yumitang/300/300', '甜玉米配排骨，清甜不腻，营养丰富', '[{"name":"葱花","type":"single","required":false,"options":[{"label":"加葱花","value":"加葱花"},{"label":"不要葱花","value":"不要葱花"}]}]', 999, 1);

-- 饮品 (category_id=5) — 冰度(必选)+糖度(可选)
INSERT INTO `dish` (`category_id`, `name`, `price`, `image`, `description`, `taste_config`, `stock`, `status`) VALUES
    (5, '冰镇可乐', 6.00, 'https://picsum.photos/seed/kele/300/300', '冰镇可口可乐，气泡十足，解腻神器', '[{"name":"冰度","type":"single","required":true,"options":[{"label":"正常冰","value":"正常冰"},{"label":"少冰","value":"少冰"},{"label":"去冰","value":"去冰"},{"label":"常温","value":"常温"}]},{"name":"糖度","type":"single","required":false,"options":[{"label":"正常糖","value":"正常糖"},{"label":"半糖","value":"半糖"},{"label":"无糖","value":"无糖"}]}]', 999, 1),
    (5, '柠檬茶', 10.00, 'https://picsum.photos/seed/ningmengcha/300/300', '新鲜柠檬片泡红茶，酸甜清爽，冰镇更佳', '[{"name":"冰度","type":"single","required":true,"options":[{"label":"正常冰","value":"正常冰"},{"label":"少冰","value":"少冰"},{"label":"去冰","value":"去冰"},{"label":"常温","value":"常温"}]},{"name":"糖度","type":"single","required":false,"options":[{"label":"正常糖","value":"正常糖"},{"label":"半糖","value":"半糖"},{"label":"无糖","value":"无糖"}]}]', 999, 1),
    (5, '酸梅汤', 8.00, 'https://picsum.photos/seed/suanmeitang/300/300', '古法熬制酸梅汤，乌梅山楂桂花，酸甜开胃', '[{"name":"冰度","type":"single","required":true,"options":[{"label":"正常冰","value":"正常冰"},{"label":"少冰","value":"少冰"},{"label":"去冰","value":"去冰"},{"label":"常温","value":"常温"}]},{"name":"糖度","type":"single","required":false,"options":[{"label":"正常糖","value":"正常糖"},{"label":"半糖","value":"半糖"},{"label":"无糖","value":"无糖"}]}]', 999, 1),
    (5, '鲜榨西瓜汁', 16.00, 'https://picsum.photos/seed/xiguazhi/300/300', '新鲜西瓜现榨，不加一滴水，清甜解暑', '[{"name":"冰度","type":"single","required":true,"options":[{"label":"正常冰","value":"正常冰"},{"label":"少冰","value":"少冰"},{"label":"去冰","value":"去冰"},{"label":"常温","value":"常温"}]},{"name":"糖度","type":"single","required":false,"options":[{"label":"正常糖","value":"正常糖"},{"label":"半糖","value":"半糖"},{"label":"无糖","value":"无糖"}]}]', 999, 1),
    (5, '杨枝甘露', 22.00, 'https://picsum.photos/seed/yangzhi/300/300', '芒果西米露配西柚粒，丝滑浓郁，港式经典', '[{"name":"冰度","type":"single","required":true,"options":[{"label":"正常冰","value":"正常冰"},{"label":"少冰","value":"少冰"},{"label":"去冰","value":"去冰"},{"label":"常温","value":"常温"}]},{"name":"糖度","type":"single","required":false,"options":[{"label":"正常糖","value":"正常糖"},{"label":"半糖","value":"半糖"},{"label":"无糖","value":"无糖"}]}]', 999, 1),
    (5, '热豆浆', 6.00, 'https://picsum.photos/seed/doujiang/300/300', '现磨热豆浆，浓郁醇香，搭配主食绝佳', '[{"name":"冰度","type":"single","required":true,"options":[{"label":"正常冰","value":"正常冰"},{"label":"少冰","value":"少冰"},{"label":"去冰","value":"去冰"},{"label":"常温","value":"常温"}]},{"name":"糖度","type":"single","required":false,"options":[{"label":"正常糖","value":"正常糖"},{"label":"半糖","value":"半糖"},{"label":"无糖","value":"无糖"}]}]', 999, 1),
    (5, '王老吉', 8.00, 'https://picsum.photos/seed/wanglaoji/300/300', '凉茶降火，怕上火喝王老吉', '[{"name":"冰度","type":"single","required":true,"options":[{"label":"正常冰","value":"正常冰"},{"label":"少冰","value":"少冰"},{"label":"去冰","value":"去冰"},{"label":"常温","value":"常温"}]},{"name":"糖度","type":"single","required":false,"options":[{"label":"正常糖","value":"正常糖"},{"label":"半糖","value":"半糖"},{"label":"无糖","value":"无糖"}]}]', 999, 1);
