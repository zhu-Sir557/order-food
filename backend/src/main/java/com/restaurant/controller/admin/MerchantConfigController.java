package com.restaurant.controller.admin;

import com.restaurant.common.Result;
import com.restaurant.dto.MerchantConfigSaveDTO;
import com.restaurant.service.MerchantConfigService;
import com.restaurant.vo.MerchantConfigVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家配置后台管理接口（单行配置，鉴权复用既有 AdminAuthInterceptor）。
 */
@RestController
@RequestMapping("/api/admin/merchant-config")
@RequiredArgsConstructor
public class MerchantConfigController {

    private final MerchantConfigService merchantConfigService;

    /**
     * 获取商家配置。
     */
    @GetMapping
    public Result<MerchantConfigVO> getConfig() {
        return Result.success(merchantConfigService.getConfig());
    }

    /**
     * 新增商家配置。
     */
    @PostMapping
    public Result<Void> saveConfig(@Valid @RequestBody MerchantConfigSaveDTO saveDTO) {
        merchantConfigService.upsert(saveDTO);
        return Result.success();
    }

    /**
     * 更新商家配置。
     */
    @PutMapping
    public Result<Void> updateConfig(@Valid @RequestBody MerchantConfigSaveDTO saveDTO) {
        merchantConfigService.upsert(saveDTO);
        return Result.success();
    }
}
