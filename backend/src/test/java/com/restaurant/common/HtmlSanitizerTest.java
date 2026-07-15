package com.restaurant.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * HtmlSanitizer 纯逻辑单元测试（不加载 Spring / MySQL 上下文）。
 *
 * <p>覆盖「关于我们」富文本存储前的 XSS 防护关键用例：黑名单整块移除、
 * 事件属性清洗、危险协议清洗、非 https 图片丢弃、白名单标签保留。</p>
 */
class HtmlSanitizerTest {

    @Test
    @DisplayName("1. script 标签及其内容被整块移除")
    void removesScriptTag() {
        String input = "<div>正常内容<script>alert(1)</script></div>";
        String output = HtmlSanitizer.sanitize(input);
        assertFalse(output.contains("script"), "输出不应包含 script 标签");
        assertFalse(output.contains("alert"), "输出不应包含 alert 关键字");
        assertTrue(output.contains("正常内容"), "合法文本应保留");
    }

    @Test
    @DisplayName("2. on* 事件属性被移除")
    void removesOnclickHandler() {
        String input = "<div onclick=\"evil()\">hi</div>";
        String output = HtmlSanitizer.sanitize(input);
        assertFalse(output.contains("onclick"), "输出不应包含 onclick 属性");
        assertTrue(output.contains("hi"), "合法文本应保留");
    }

    @Test
    @DisplayName("3. javascript: 危险协议属性被移除")
    void removesJavascriptProtocol() {
        String input = "<a href=\"javascript:alert(1)\">link</a>";
        String output = HtmlSanitizer.sanitize(input);
        assertFalse(output.contains("javascript:"), "输出不应包含 javascript: 协议");
    }

    @Test
    @DisplayName("4. iframe 标签被移除")
    void removesIframe() {
        String input = "<p>前</p><iframe src=\"https://evil.com/x\"></iframe><p>后</p>";
        String output = HtmlSanitizer.sanitize(input);
        assertFalse(output.contains("iframe"), "输出不应包含 iframe 标签");
    }

    @Test
    @DisplayName("5. https 图片被保留且 src 不变")
    void keepsHttpsImage() {
        String input = "<p>图：</p><img src=\"https://x.com/a.png\">";
        String output = HtmlSanitizer.sanitize(input);
        assertTrue(output.contains("<img"), "https 图片应保留");
        assertTrue(output.contains("https://x.com/a.png"), "src 应为 https 且不变");
    }

    @Test
    @DisplayName("6. http 图片被整张丢弃")
    void dropsHttpImage() {
        String input = "<p>图：</p><img src=\"http://x.com/a.png\">";
        String output = HtmlSanitizer.sanitize(input);
        assertEquals("<p>图：</p>", output, "http 图片应被整张丢弃，且合法 <p> 应保留");
        assertFalse(output.contains("http://x.com/a.png"), "http 图片地址不应出现");
    }

    @Test
    @DisplayName("7. 白名单标签原样保留")
    void keepsWhitelistedTags() {
        String input = "<h2>标题</h2><p>段落</p><strong>粗</strong><ul><li>项</li></ul>";
        String output = HtmlSanitizer.sanitize(input);
        assertEquals(input, output, "白名单标签应原样保留");
    }

    @Test
    @DisplayName("8. 普通文本原样保留")
    void keepsPlainText() {
        String input = "欢迎光临";
        String output = HtmlSanitizer.sanitize(input);
        assertEquals("欢迎光临", output, "普通文本应原样保留");
    }

    @Test
    @DisplayName("9. 非白名单标签被剥离但文本保留")
    void stripsUnknownTagKeepText() {
        String input = "<marquee>滚动文本</marquee>";
        String output = HtmlSanitizer.sanitize(input);
        assertEquals("滚动文本", output, "非白名单标签应被剥离，仅保留内部文本");
        assertTrue(output.contains("滚动文本"), "标签内部文本应保留");
    }

    @Test
    @DisplayName("10. null / 空输入返回空串")
    void handlesNullOrEmpty() {
        assertEquals("", HtmlSanitizer.sanitize(null));
        assertEquals("", HtmlSanitizer.sanitize(""));
    }
}
