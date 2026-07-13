package com.restaurant.vo;

import lombok.Data;

/**
 * 修改次数回执 VO（昵称/头像修改后剩余次数）
 */
@Data
public class ChangeLimitVO {

    /** 今日剩余可修改次数 */
    private Integer remaining;
}
