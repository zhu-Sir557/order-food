package com.restaurant.vo;

import lombok.Data;

/**
 * 商家配置公开视图对象（H5 使用，仅暴露必要字段）。
 */
@Data
public class MerchantConfigPublicVO {

    /** 关于我们富文本 HTML */
    private String aboutUsContent;

    /** 联系电话 */
    private String contactPhone;
}
