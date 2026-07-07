package com.restaurant.controller.h5;

import com.restaurant.common.Result;
import com.restaurant.dto.SliderCaptchaCheckDTO;
import com.restaurant.service.CaptchaService;
import com.restaurant.vo.CaptchaCheckVO;
import com.restaurant.vo.SliderCaptchaVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * H5 验证码接口
 */
@RestController
@RequestMapping("/api/h5/captcha")
@RequiredArgsConstructor
public class H5CaptchaController {

    private final CaptchaService captchaService;

    /**
     * 获取滑块验证码
     *
     * @return 滑块验证码（含背景图、拼图块、captchaId）
     */
    @GetMapping("/slider")
    public Result<SliderCaptchaVO> getSliderCaptcha() {
        return Result.success(captchaService.generateSliderCaptcha());
    }

    /**
     * 校验滑块
     *
     * @param dto 校验请求（captchaId + xOffset）
     * @return 校验结果（成功时返回 captchaToken）
     */
    @PostMapping("/slider/check")
    public Result<CaptchaCheckVO> checkSlider(@Valid @RequestBody SliderCaptchaCheckDTO dto) {
        return Result.success(captchaService.checkSlider(dto.getCaptchaId(), dto.getXOffset()));
    }
}
