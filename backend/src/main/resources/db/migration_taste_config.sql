-- =============================================
-- 口味配置增量迁移脚本
-- =============================================

-- 1. dish 表添加 taste_config 字段
ALTER TABLE dish ADD COLUMN taste_config TEXT DEFAULT NULL COMMENT '口味配置JSON' AFTER description;

-- 2. order_item 表添加 taste_selection 字段
ALTER TABLE order_item ADD COLUMN taste_selection VARCHAR(500) DEFAULT NULL COMMENT '口味选择' AFTER subtotal;

-- 3. 热菜（category_id=1）：辣度(必选) + 香菜(可选) + 葱花(可选)
UPDATE dish SET taste_config = '[{"name":"辣度","type":"single","required":true,"options":[{"label":"不辣","value":"不辣"},{"label":"微辣","value":"微辣"},{"label":"中辣","value":"中辣"},{"label":"特辣","value":"特辣"}]},{"name":"香菜","type":"single","required":false,"options":[{"label":"加香菜","value":"加香菜"},{"label":"不要香菜","value":"不要香菜"}]},{"name":"葱花","type":"single","required":false,"options":[{"label":"加葱花","value":"加葱花"},{"label":"不要葱花","value":"不要葱花"}]}]' WHERE category_id = 1 AND deleted = 0;

-- 4. 凉菜（category_id=2）：辣度(必选) + 香菜(可选)
UPDATE dish SET taste_config = '[{"name":"辣度","type":"single","required":true,"options":[{"label":"不辣","value":"不辣"},{"label":"微辣","value":"微辣"},{"label":"中辣","value":"中辣"}]},{"name":"香菜","type":"single","required":false,"options":[{"label":"加香菜","value":"加香菜"},{"label":"不要香菜","value":"不要香菜"}]}]' WHERE category_id = 2 AND deleted = 0;

-- 5. 主食（category_id=3）：葱花(可选)
UPDATE dish SET taste_config = '[{"name":"葱花","type":"single","required":false,"options":[{"label":"加葱花","value":"加葱花"},{"label":"不要葱花","value":"不要葱花"}]}]' WHERE category_id = 3 AND deleted = 0;

-- 6. 汤/羹（category_id=4）：葱花(可选)
UPDATE dish SET taste_config = '[{"name":"葱花","type":"single","required":false,"options":[{"label":"加葱花","value":"加葱花"},{"label":"不要葱花","value":"不要葱花"}]}]' WHERE category_id = 4 AND deleted = 0;

-- 7. 饮品（category_id=5）：冰度(必选) + 糖度(可选)
UPDATE dish SET taste_config = '[{"name":"冰度","type":"single","required":true,"options":[{"label":"正常冰","value":"正常冰"},{"label":"少冰","value":"少冰"},{"label":"去冰","value":"去冰"},{"label":"常温","value":"常温"}]},{"name":"糖度","type":"single","required":false,"options":[{"label":"正常糖","value":"正常糖"},{"label":"半糖","value":"半糖"},{"label":"无糖","value":"无糖"}]}]' WHERE category_id = 5 AND deleted = 0;
