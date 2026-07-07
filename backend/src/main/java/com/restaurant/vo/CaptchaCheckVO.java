package com.restaurant.vo;

import lombok.Data;

/**
 * 滑块验证码校验结果 VO
 */
@Data
public class CaptchaCheckVO {

    /** 是否校验通过 */
    private Boolean success;

    /** 验证通过后返回的 token（用于登录时校验） */
    private String captchaToken;

    /**
     * 构造校验失败结果
     *
     * @return 失败结果
     */
    public static CaptchaCheckVO fail() {
        CaptchaCheckVO vo = new CaptchaCheckVO();
        vo.setSuccess(false);
        vo.setCaptchaToken(null);
        return vo;
    }

    /**
     * 构造校验成功结果
     *
     * @param captchaToken 验证 token
     * @return 成功结果
     */
    public static CaptchaCheckVO success(String captchaToken) {
        CaptchaCheckVO vo = new CaptchaCheckVO();
        vo.setSuccess(true);
        vo.setCaptchaToken(captchaToken);
        return vo;
    }
}
