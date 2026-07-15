package com.restaurant.controller.h5;

import com.restaurant.common.Result;
import com.restaurant.service.MerchantConfigService;
import com.restaurant.vo.MerchantConfigPublicVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家配置 H5 公开接口（无需登录、无需 token）。
 */
@RestController
@RequestMapping("/api/h5/merchant-config")
@RequiredArgsConstructor
public class H5MerchantConfigController {

    private final MerchantConfigService merchantConfigService;

    /**
     * 获取对外公开的商家配置（关于我们 + 联系电话）。
     */
    @GetMapping
    public Result<MerchantConfigPublicVO> getPublic() {
        return Result.success(merchantConfigService.getPublic());
    }
}
