package com.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.restaurant.config.AvatarProperties;
import com.restaurant.entity.Avatar;
import com.restaurant.mapper.AvatarMapper;
import com.restaurant.service.AvatarInitService;
import com.restaurant.service.FileService;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

/**
 * 头像库初始化服务实现（CommandLineRunner，幂等，启动不阻塞）
 *
 * <p>应用启动时若 {@code avatar.init.enabled=true} 且头像库不足目标数量，
 * 程序生成卡通 SVG → 经 {@link FileService#upload(byte[], String)} 上传 OSS → seed 头像表。
 * 任何异常均被捕获并 log.warn 后跳过，绝不阻塞应用启动。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AvatarInitServiceImpl implements AvatarInitService, CommandLineRunner {

    private final FileService fileService;
    private final AvatarMapper avatarMapper;
    private final AvatarProperties avatarProperties;

    @Override
    public void run(String... args) {
        try {
            initIfNeeded();
        } catch (Exception e) {
            // 任何异常都必须 catch 并跳过，绝不阻塞应用启动
            log.warn("头像库初始化异常，已跳过: {}", e.getMessage());
        }
    }

    @Override
    public void initIfNeeded() {
        if (!avatarProperties.getInit().isEnabled()) {
            log.info("头像库初始化已关闭(avatar.init.enabled=false)，跳过");
            return;
        }

        Long count = avatarMapper.selectCount(new LambdaQueryWrapper<>());
        int target = avatarProperties.getCount();
        if (count != null && count >= target) {
            log.info("头像库已存在 {} 条（>=目标 {}），跳过初始化（幂等）", count, target);
            return;
        }

        int existing = count == null ? 0 : count.intValue();
        for (int i = existing + 1; i <= target; i++) {
            String svg = buildCartoonSvg(i);
            byte[] bytes = svg.getBytes(StandardCharsets.UTF_8);
            String ossUrl = fileService.upload(bytes, "svg");
            Avatar avatar = new Avatar();
            avatar.setOssUrl(ossUrl);
            avatar.setSort(i);
            avatarMapper.insert(avatar);
            log.info("头像库初始化: 已生成第 {}/{} 张", i, target);
        }
        log.info("头像库初始化完成，共 {} 张", target);
    }

    /**
     * 生成卡通头像 SVG（纯字符串，无外部图片依赖）
     *
     * @param index 序号（决定色相，保证各头像视觉区分）
     * @return SVG 字符串
     */
    private String buildCartoonSvg(int index) {
        int hue = (index * 30) % 360;
        int hue2 = (hue + 30) % 360;
        String bg = String.format("hsl(%d, 70%%, 78%%)", hue);
        String bg2 = String.format("hsl(%d, 70%%, 58%%)", hue2);
        String gradId = "g" + index;
        return "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"120\" height=\"120\" viewBox=\"0 0 120 120\">"
                + "<defs><radialGradient id=\"" + gradId + "\" cx=\"50%\" cy=\"35%\" r=\"75%\">"
                + "<stop offset=\"0%\" stop-color=\"" + bg + "\"/>"
                + "<stop offset=\"100%\" stop-color=\"" + bg2 + "\"/></radialGradient></defs>"
                + "<circle cx=\"60\" cy=\"60\" r=\"56\" fill=\"url(#" + gradId + "\"/>"
                + "<circle cx=\"44\" cy=\"52\" r=\"7\" fill=\"#3a3a3a\"/>"
                + "<circle cx=\"76\" cy=\"52\" r=\"7\" fill=\"#3a3a3a\"/>"
                + "<path d=\"M40 78 Q60 96 80 78\" stroke=\"#3a3a3a\" stroke-width=\"5\" "
                + "fill=\"none\" stroke-linecap=\"round\"/>"
                + "</svg>";
    }
}
