package com.restaurant.vo;

import lombok.Data;

/**
 * 商家配置视图对象（后台使用，含全部字段及格式化时间）。
 */
@Data
public class MerchantConfigVO {

    /** 主键 */
    private Long id;

    /** 关于我们富文本 HTML */
    private String aboutUsContent;

    /** 联系电话 */
    private String contactPhone;

    /** 创建时间（yyyy-MM-dd HH:mm:ss） */
    private String createTime;

    /** 更新时间（yyyy-MM-dd HH:mm:ss） */
    private String updateTime;
}
