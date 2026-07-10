package com.restaurant.service;

/**
 * 短信验证码存储接口（自校验通道）
 *
 * <p>验证码明文仅落 Redis，不落 MySQL、不落日志；校验成功即删（一次性）。</p>
 */
public interface SmsCodeStore {

    /**
     * 保存验证码
     *
     * @param phone     手机号
     * @param code      验证码明文
     * @param ttlSeconds 过期时间（秒）
     */
    void save(String phone, String code, int ttlSeconds);

    /**
     * 获取验证码明文
     *
     * @param phone 手机号
     * @return 验证码明文，不存在或已过期返回 {@code null}
     */
    String get(String phone);

    /**
     * 删除验证码（校验成功后一次性消费）
     *
     * @param phone 手机号
     */
    void delete(String phone);

    /**
     * 判断验证码是否存在且未过期
     *
     * @param phone 手机号
     * @return 存在且未过期返回 {@code true}
     */
    boolean exists(String phone);

    /**
     * 手机号脱敏工具（日志专用，绝不打印明文验证码）
     *
     * <p>示例：{@code 13800001111 -> 138****1111}</p>
     *
     * @param phone 原始手机号
     * @return 脱敏后的手机号
     */
    static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "****";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
