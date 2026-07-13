package com.restaurant.vo;

import lombok.Data;

/**
 * 头像选项 VO
 */
@Data
public class AvatarVO {

    /** 头像ID */
    private Long id;

    /** 头像OSS地址 */
    private String ossUrl;
}
