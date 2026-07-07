package com.restaurant.vo;

import lombok.Data;

/**
 * 滑块验证码响应 VO
 */
@Data
public class SliderCaptchaVO {

    /** 验证码ID */
    private String captchaId;

    /** 背景图 Base64 */
    private String backgroundImage;

    /** 拼图块 Base64 */
    private String puzzleImage;

    /** 拼图块 Y 位置 */
    private Integer y;
}
