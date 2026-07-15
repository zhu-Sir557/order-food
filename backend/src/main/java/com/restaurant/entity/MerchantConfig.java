package com.restaurant.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 商家配置实体（单行表，id 固定为 1）。
 *
 * <p>与 banner 等表不同，本表为单行永久配置：不设置逻辑删除字段（{@code @TableLogic}），
 * 且主键使用 {@link IdType#INPUT} 由业务显式写入 1。</p>
 */
@Data
@TableName("merchant_config")
public class MerchantConfig {

    /** 主键，固定为 1（单行配置） */
    @TableId(type = IdType.INPUT)
    private Long id;

    /** 关于我们富文本 HTML */
    @TableField("about_us_content")
    private String aboutUsContent;

    /** 联系电话（中国大陆手机 11 位或固话） */
    @TableField("contact_phone")
    private String contactPhone;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
