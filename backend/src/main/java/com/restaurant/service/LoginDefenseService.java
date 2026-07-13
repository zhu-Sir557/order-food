package com.restaurant.service;

/**
 * 登录防暴破服务接口
 *
 * <p>按「账号 + IP」双维度记录登录失败次数，达到上限后短时锁定，
 * 与短信验证码错误锁定（{@code sms:lock:phone}）语义相互独立。</p>
 */
public interface LoginDefenseService {

    /**
     * 判断账号是否处于登录锁定状态
     *
     * @param account 账号（手机号或账户名）
     * @return 锁定中返回 {@code true}
     */
    boolean isAccountLocked(String account);

    /**
     * 判断 IP 是否处于登录锁定状态
     *
     * @param ip 客户端 IP
     * @return 锁定中返回 {@code true}
     */
    boolean isIpLocked(String ip);

    /**
     * 记录一次登录失败，达上限则锁定账号与 IP
     *
     * @param account 账号
     * @param ip      客户端 IP
     * @return 失败结果（含是否锁定与当前次数）
     */
    LoginFailResult onLoginFail(String account, String ip);

    /**
     * 登录成功后重置失败计数（锁定态随 TTL 自然过期）
     *
     * @param account 账号
     * @param ip      客户端 IP
     */
    void resetOnSuccess(String account, String ip);
}
