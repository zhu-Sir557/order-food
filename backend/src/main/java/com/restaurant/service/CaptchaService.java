package com.restaurant.service;

import com.restaurant.vo.CaptchaCheckVO;
import com.restaurant.vo.SliderCaptchaVO;

/**
 * 验证码服务接口
 */
public interface CaptchaService {

    /**
     * 生成滑块验证码
     *
     * @return 滑块验证码 VO（含背景图、拼图块、captchaId）
     */
    SliderCaptchaVO generateSliderCaptcha();

    /**
     * 校验滑块偏移量
     *
     * @param captchaId 验证码ID
     * @param xOffset   滑块 X 轴偏移量
     * @return 校验结果（成功时返回 captchaToken）
     */
    CaptchaCheckVO checkSlider(String captchaId, Integer xOffset);

    /**
     * 验证并消费 captchaToken（登录时调用，一次性）
     *
     * @param captchaToken 验证 token
     * @return true 验证通过，false 验证失败
     */
    boolean verifyAndConsumeCaptcha(String captchaToken);
}
