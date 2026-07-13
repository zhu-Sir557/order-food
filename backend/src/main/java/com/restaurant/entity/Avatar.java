package com.restaurant.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 卡通头像库实体
 */
@Data
@TableName("avatar")
public class Avatar {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 头像OSS地址 */
    private String ossUrl;

    /** 展示排序 */
    private Integer sort;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除：0未删除，1已删除 */
    @TableLogic
    private Integer deleted;
}
