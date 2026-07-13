package com.restaurant.vo;

import lombok.Data;

/**
 * 上传头像响应 VO（返回新头像地址与当日剩余可修改次数）
 */
@Data
public class AvatarUpdateVO {

    /** 新头像 OSS 地址 */
    private String avatar;

    /** 当日剩余可修改次数 */
    private int remaining;
}
