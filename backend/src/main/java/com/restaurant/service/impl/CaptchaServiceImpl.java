package com.restaurant.service.impl;

import com.restaurant.service.CaptchaService;
import com.restaurant.util.CaptchaUtil;
import com.restaurant.vo.CaptchaCheckVO;
import com.restaurant.vo.SliderCaptchaVO;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 验证码服务实现
 *
 * <p>使用 ConcurrentHashMap 存储验证码状态，5分钟过期，定时清理。
 * 滑块校验容差 ±10px。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    /** 容差范围（±10px） */
    private static final int TOLERANCE = 10;

    /** 验证码有效期（分钟） */
    private static final int EXPIRE_MINUTES = 5;

    /** 验证码状态存储：captchaId → CaptchaInfo */
    private final ConcurrentHashMap<String, CaptchaInfo> captchaStore = new ConcurrentHashMap<>();

    /**
     * 验证码状态信息
     */
    private static class CaptchaInfo {
        /** 正确的 X 位置 */
        final int correctX;
        /** 是否已通过滑块校验 */
        volatile boolean verified;
        /** 创建时间 */
        final LocalDateTime createTime;

        CaptchaInfo(int correctX) {
            this.correctX = correctX;
            this.verified = false;
            this.createTime = LocalDateTime.now();
        }

        /**
         * 是否过期
         *
         * @return true 已过期
         */
        boolean isExpired() {
            return createTime.plusMinutes(EXPIRE_MINUTES).isBefore(LocalDateTime.now());
        }
    }

    @Override
    public SliderCaptchaVO generateSliderCaptcha() {
        // 调用工具类生成验证码图片
        CaptchaUtil.SliderCaptchaResult result = CaptchaUtil.generate();

        // 生成 captchaId
        String captchaId = UUID.randomUUID().toString().replace("-", "");

        // 存储验证状态
        captchaStore.put(captchaId, new CaptchaInfo(result.getCorrectX()));

        // 构建 VO
        SliderCaptchaVO vo = new SliderCaptchaVO();
        vo.setCaptchaId(captchaId);
        vo.setBackgroundImage(result.getBgImageBase64());
        vo.setPuzzleImage(result.getPuzzleImageBase64());
        vo.setY(result.getY());

        log.debug("生成滑块验证码: captchaId={}, correctX={}", captchaId, result.getCorrectX());
        return vo;
    }

    @Override
    public CaptchaCheckVO checkSlider(String captchaId, Integer xOffset) {
        CaptchaInfo info = captchaStore.get(captchaId);

        // 验证码不存在或已过期
        if (info == null || info.isExpired()) {
            if (info != null) {
                captchaStore.remove(captchaId);
            }
            return CaptchaCheckVO.fail();
        }

        // 校验偏移量（容差 ±10px）
        if (Math.abs(xOffset - info.correctX) <= TOLERANCE) {
            info.verified = true;
            // captchaToken 即为 captchaId，登录时用于验证
            return CaptchaCheckVO.success(captchaId);
        }

        return CaptchaCheckVO.fail();
    }

    @Override
    public boolean verifyAndConsumeCaptcha(String captchaToken) {
        if (captchaToken == null || captchaToken.isBlank()) {
            return false;
        }

        CaptchaInfo info = captchaStore.get(captchaToken);
        if (info == null) {
            return false;
        }

        // 检查是否已验证且未过期
        if (!info.verified || info.isExpired()) {
            captchaStore.remove(captchaToken);
            return false;
        }

        // 一次性消费：删除验证码
        captchaStore.remove(captchaToken);
        return true;
    }

    /**
     * 定时清理过期的验证码（每5分钟执行一次）
     */
    @Scheduled(fixedRate = 300000)
    public void cleanupExpiredCaptchas() {
        int count = 0;
        Iterator<Entry<String, CaptchaInfo>> it = captchaStore.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, CaptchaInfo> entry = it.next();
            if (entry.getValue().isExpired()) {
                it.remove();
                count++;
            }
        }
        if (count > 0) {
            log.debug("清理过期验证码: {} 条", count);
        }
    }
}
