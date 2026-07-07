package com.restaurant.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Random;
import javax.imageio.ImageIO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 滑块验证码图片生成工具
 *
 * <p>使用纯 Java AWT/ImageIO 生成滑块验证码图片，不引入额外依赖。
 * 生成 310×160 背景图 + 44×44 拼图块，返回 Base64 编码。</p>
 */
@Slf4j
public class CaptchaUtil {

    /** 背景图宽度 */
    private static final int BG_WIDTH = 310;

    /** 背景图高度 */
    private static final int BG_HEIGHT = 160;

    /** 拼图块尺寸 */
    private static final int PUZZLE_SIZE = 44;

    /** 拼图块 Y 位置（固定） */
    private static final int PUZZLE_Y = 60;

    /** 拼图块 X 位置最小值 */
    private static final int MIN_X = 60;

    /** 拼图块 X 位置最大值 */
    private static final int MAX_X = 260;

    /** 随机数生成器 */
    private static final Random RANDOM = new Random();

    /**
     * 滑块验证码生成结果
     */
    @Getter
    public static class SliderCaptchaResult {
        /** 背景图 Base64（含 data URI 前缀） */
        private final String bgImageBase64;
        /** 拼图块 Base64（含 data URI 前缀） */
        private final String puzzleImageBase64;
        /** 拼图块正确 X 位置 */
        private final int correctX;
        /** 拼图块 Y 位置 */
        private final int y;

        public SliderCaptchaResult(String bgImageBase64, String puzzleImageBase64, int correctX, int y) {
            this.bgImageBase64 = bgImageBase64;
            this.puzzleImageBase64 = puzzleImageBase64;
            this.correctX = correctX;
            this.y = y;
        }
    }

    /**
     * 生成滑块验证码
     *
     * @return 包含背景图、拼图块 Base64 和正确位置的生成结果
     */
    public static SliderCaptchaResult generate() {
        // 随机 X 位置
        int correctX = MIN_X + RANDOM.nextInt(MAX_X - MIN_X);

        // 创建背景图
        BufferedImage bgImage = createBackground(BG_WIDTH, BG_HEIGHT);

        // 从背景图裁剪拼图块并挖空背景
        BufferedImage puzzleImage = cutPuzzle(bgImage, correctX, PUZZLE_Y, PUZZLE_SIZE);

        // 编码为 Base64
        String bgBase64 = toBase64(bgImage);
        String puzzleBase64 = toBase64(puzzleImage);

        return new SliderCaptchaResult(bgBase64, puzzleBase64, correctX, PUZZLE_Y);
    }

    /**
     * 创建背景图（随机渐变色 + 干扰线 + 噪点）
     *
     * @param width  背景图宽度
     * @param height 背景图高度
     * @return 生成的背景图
     */
    private static BufferedImage createBackground(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 随机渐变色填充
        Color color1 = randomColor();
        Color color2 = randomColor();
        LinearGradientPaint paint = new LinearGradientPaint(
                new Point(0, 0),
                new Point(width, height),
                new float[]{0.0f, 1.0f},
                new Color[]{color1, color2}
        );
        g2d.setPaint(paint);
        g2d.fillRect(0, 0, width, height);

        // 添加干扰线
        for (int i = 0; i < 6; i++) {
            g2d.setColor(randomColor());
            g2d.setStroke(new BasicStroke(1 + RANDOM.nextInt(2)));
            int x1 = RANDOM.nextInt(width);
            int y1 = RANDOM.nextInt(height);
            int x2 = RANDOM.nextInt(width);
            int y2 = RANDOM.nextInt(height);
            g2d.drawLine(x1, y1, x2, y2);
        }

        // 添加噪点
        for (int i = 0; i < 100; i++) {
            g2d.setColor(randomColor());
            int x = RANDOM.nextInt(width);
            int y = RANDOM.nextInt(height);
            g2d.fillRect(x, y, 1, 1);
        }

        g2d.dispose();
        return image;
    }

    /**
     * 从背景图裁剪拼图块，并在背景图上挖空
     *
     * @param bgImage 背景图
     * @param x       拼图块 X 位置
     * @param y       拼图块 Y 位置
     * @param size    拼图块尺寸
     * @return 拼图块图片（带 alpha 通道）
     */
    private static BufferedImage cutPuzzle(BufferedImage bgImage, int x, int y, int size) {
        // 创建拼图块图片（带 alpha 通道）
        BufferedImage puzzleImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D puzzleG2d = puzzleImage.createGraphics();
        puzzleG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 从背景图复制拼图块区域
        puzzleG2d.drawImage(bgImage, 0, 0, size, size, x, y, x + size, y + size, null);

        // 添加拼图块边框
        puzzleG2d.setColor(Color.WHITE);
        puzzleG2d.setStroke(new BasicStroke(2));
        puzzleG2d.drawRect(0, 0, size - 1, size - 1);
        puzzleG2d.dispose();

        // 在背景图上挖空（填充半透明灰色）
        Graphics2D bgG2d = bgImage.createGraphics();
        bgG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        bgG2d.setColor(new Color(0, 0, 0, 100));
        bgG2d.fillRect(x, y, size, size);
        // 挖空边框
        bgG2d.setColor(new Color(255, 255, 255, 200));
        bgG2d.setStroke(new BasicStroke(2));
        bgG2d.drawRect(x, y, size - 1, size - 1);
        bgG2d.dispose();

        return puzzleImage;
    }

    /**
     * 将 BufferedImage 编码为 Base64 data URI
     *
     * @param image 图片
     * @return Base64 编码的 data URI 字符串
     */
    private static String toBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            byte[] bytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            log.error("图片转Base64失败", e);
            throw new RuntimeException("图片编码失败", e);
        }
    }

    /**
     * 生成随机颜色
     *
     * @return 随机颜色
     */
    private static Color randomColor() {
        return new Color(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256));
    }

    /**
     * 私有构造方法，防止实例化
     */
    private CaptchaUtil() {
    }
}
