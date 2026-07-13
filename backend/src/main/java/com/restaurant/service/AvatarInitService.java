package com.restaurant.service;

/**
 * 头像库初始化服务接口（应用启动时幂等 seed）
 */
public interface AvatarInitService {

    /**
     * 若需要则初始化头像库（受 {@code avatar.init.enabled} 开关控制，幂等）
     */
    void initIfNeeded();
}
