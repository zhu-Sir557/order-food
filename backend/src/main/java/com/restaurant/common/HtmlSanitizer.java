package com.restaurant.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 轻量级 HTML 富文本净化工具，用于「关于我们」富文本存储前的 XSS 防护。
 *
 * <p>设计原则：不依赖任何第三方库，仅使用 JDK 自带的正则表达式与字符串处理。
 * 净化策略为「黑名单整块移除 + 属性清洗 + 白名单标签过滤」的组合，兼顾安全性与富文本可用性。</p>
 *
 * <p>处理顺序：
 * <ol>
 *   <li>整块移除危险标签（script / iframe / object / embed 等）及其内容；</li>
 *   <li>丢弃 {@code src} 非 {@code https://} 开头的 {@code <img>} 整张图片；</li>
 *   <li>清除所有 {@code on*} 事件属性；</li>
 *   <li>清除携带 {@code javascript:} / {@code data:} 等危险协议的属性；</li>
 *   <li>白名单标签过滤：非白名单标签整体剥离（保留其文本内容）。</li>
 * </ol>
 * </p>
 */
public final class HtmlSanitizer {

    /** 允许的标签白名单 */
    private static final Set<String> ALLOWED_TAGS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "h1", "h2", "h3", "p", "br", "div", "span", "ul", "ol", "li",
            "strong", "b", "em", "i", "u", "img", "a")));

    /** 需要整块移除（含内部内容）的容器型危险标签 */
    private static final Pattern BLOCK_TAG_PATTERN = Pattern.compile(
            "<(script|iframe|object|style|svg|math|form|select|textarea|button|applet|noscript|frameset|details)\\b[^>]*>.*?</\\1>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /** 需要整块移除的空元素型危险标签（无闭合内容） */
    private static final Pattern VOID_TAG_PATTERN = Pattern.compile(
            "<(embed|input|link|meta|base|frame|source|area|col|wbr|param)\\b[^>]*/?>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /** 所有 on* 事件属性（如 onclick、onerror、onload 等） */
    private static final Pattern EVENT_HANDLER_PATTERN = Pattern.compile(
            "\\s+on[a-z]+\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\\s>]+)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /** 携带 javascript: / data: 等危险协议的属性（href / src / action 等） */
    private static final Pattern DANGEROUS_PROTOCOL_PATTERN = Pattern.compile(
            "\\s+(?:href|src|action|xlink:href)\\s*=\\s*(\"javascript:[^\"]*\"|'javascript:[^']*'|\"data:[^\"]*\"|'data:[^']*'|javascript:[^\\s>]+|data:[^\\s>]+)",
            Pattern.CASE_INSENSITIVE);

    /** 任意标签匹配（用于白名单过滤） */
    private static final Pattern TAG_PATTERN = Pattern.compile(
            "<(/?)([a-zA-Z][a-zA-Z0-9]*)\\b([^>]*)>",
            Pattern.CASE_INSENSITIVE);

    private HtmlSanitizer() {
    }

    /**
     * 净化富文本 HTML。
     *
     * @param rawHtml 原始 HTML（可能为 {@code null}）
     * @return 净化后的 HTML；输入为空时返回空字符串
     */
    public static String sanitize(String rawHtml) {
        if (rawHtml == null || rawHtml.isEmpty()) {
            return "";
        }
        String html = rawHtml;

        // 1. 整块移除危险标签（含内容）
        html = BLOCK_TAG_PATTERN.matcher(html).replaceAll("");
        html = VOID_TAG_PATTERN.matcher(html).replaceAll("");

        // 2. 处理 img：src 非 https:// 开头的整张图片丢弃
        html = stripInsecureImages(html);

        // 3. 移除所有 on* 事件属性
        html = EVENT_HANDLER_PATTERN.matcher(html).replaceAll("");

        // 4. 移除携带危险协议的属性
        html = DANGEROUS_PROTOCOL_PATTERN.matcher(html).replaceAll("");

        // 5. 白名单标签过滤（非白名单标签整体移除，保留内部文本）
        html = filterByWhitelist(html);

        return html;
    }

    /**
     * 丢弃 {@code src} 非 {@code https://} 开头的 {@code <img>} 标签。
     */
    private static String stripInsecureImages(String html) {
        Matcher matcher = Pattern.compile("<img\\b([^>]*)>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)
                .matcher(html);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String attrs = matcher.group(1) == null ? "" : matcher.group(1);
            String src = extractAttribute(attrs, "src");
            // 无 src 或 src 以 https:// 开头的图片保留，其余（http / 相对路径 / 其它协议）整张丢弃
            if (src == null || src.startsWith("https://")) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group()));
            } else {
                // 丢弃非 https 图片（http / 相对路径 / 其它协议）
                matcher.appendReplacement(sb, "");
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 从属性串中提取指定属性的值（不含引号）。
     *
     * @param attrs 标签内的属性字符串
     * @param name  属性名
     * @return 属性值；不存在时返回 {@code null}
     */
    private static String extractAttribute(String attrs, String name) {
        Pattern pattern = Pattern.compile(
                name + "\\s*=\\s*(\"([^\"]*)\"|'([^']*)'|([^\\s>]+))",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(attrs);
        if (matcher.find()) {
            if (matcher.group(2) != null) {
                return matcher.group(2);
            }
            if (matcher.group(3) != null) {
                return matcher.group(3);
            }
            return matcher.group(4);
        }
        return null;
    }

    /**
     * 白名单过滤：移除不在白名单中的标签（仅去标签，保留内部文本）。
     */
    private static String filterByWhitelist(String html) {
        Matcher matcher = TAG_PATTERN.matcher(html);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String tagName = matcher.group(2).toLowerCase();
            if (ALLOWED_TAGS.contains(tagName)) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group()));
            } else {
                // 非白名单标签：丢弃标签本身，保留其内部文本
                matcher.appendReplacement(sb, "");
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
