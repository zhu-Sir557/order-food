package com.restaurant.controller.admin;

import com.restaurant.common.PageResult;
import com.restaurant.common.Result;
import com.restaurant.dto.BannerSaveDTO;
import com.restaurant.dto.OrderStatusDTO;
import com.restaurant.service.BannerService;
import com.restaurant.vo.BannerVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @GetMapping("/page")
    public Result<PageResult<BannerVO>> getBannerPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(bannerService.getBannerPage(page, size));
    }

    @PostMapping
    public Result<Void> addBanner(@Valid @RequestBody BannerSaveDTO saveDTO) {
        bannerService.addBanner(saveDTO);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateBanner(@PathVariable Long id, @Valid @RequestBody BannerSaveDTO saveDTO) {
        bannerService.updateBanner(id, saveDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return Result.success();
    }

    @PatchMapping("/{id}/status")
    public Result<Void> updateBannerStatus(@PathVariable Long id, @RequestBody OrderStatusDTO statusDTO) {
        bannerService.updateBannerStatus(id, statusDTO.getStatus());
        return Result.success();
    }
}
