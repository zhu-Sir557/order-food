package com.restaurant.service;

import com.restaurant.dto.MerchantConfigSaveDTO;
import com.restaurant.vo.MerchantConfigPublicVO;
import com.restaurant.vo.MerchantConfigVO;

/**
 * 商家配置服务接口。
 */
public interface MerchantConfigService {

    /**
     * 获取商家配置（单行，id 固定为 1）。
     *
     * @return 配置 VO；未配置时返回字段全空的默认 VO（不抛异常）
     */
    MerchantConfigVO getConfig();

    /**
     * 新增或更新商家配置（save-or-update 语义）。
     *
     * @param saveDTO 保存请求体（已通过 @Valid 校验）
     */
    void upsert(MerchantConfigSaveDTO saveDTO);

    /**
     * 获取对外公开的商家配置（仅 aboutUsContent / contactPhone）。
     *
     * @return 公开 VO；未配置时返回字段全空的对象
     */
    MerchantConfigPublicVO getPublic();
}
