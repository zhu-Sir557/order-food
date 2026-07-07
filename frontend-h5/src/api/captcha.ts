import request from './request'

/** 滑块验证码响应 */
export interface SliderCaptchaVO {
  captchaId: string
  backgroundImage: string
  puzzleImage: string
  y: number
}

/** 滑块校验结果 */
export interface CaptchaCheckVO {
  success: boolean
  captchaToken: string | null
}

/**
 * 获取滑块验证码
 * @returns 滑块验证码（含背景图、拼图块、captchaId）
 */
export function getSliderCaptcha(): Promise<SliderCaptchaVO> {
  return request.get('/api/h5/captcha/slider')
}

/**
 * 校验滑块
 * @param captchaId 验证码ID
 * @param xOffset 滑块X轴偏移量
 * @returns 校验结果
 */
export function checkSlider(captchaId: string, xOffset: number): Promise<CaptchaCheckVO> {
  return request.post('/api/h5/captcha/slider/check', { captchaId, xOffset })
}
