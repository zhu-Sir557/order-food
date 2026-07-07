package com.restaurant.controller.h5;

import com.restaurant.common.Result;
import com.restaurant.service.BannerService;
import com.restaurant.service.CategoryService;
import com.restaurant.service.DishService;
import com.restaurant.vo.BannerVO;
import com.restaurant.vo.CategoryVO;
import com.restaurant.vo.DishVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/h5")
@RequiredArgsConstructor
public class H5DishController {

    private final CategoryService categoryService;
    private final DishService dishService;
    private final BannerService bannerService;

    @GetMapping("/categories")
    public Result<List<CategoryVO>> getCategories() {
        return Result.success(categoryService.getH5CategoryList());
    }

    @GetMapping("/dishes")
    public Result<List<DishVO>> getDishes(@RequestParam(required = false) Long categoryId) {
        return Result.success(dishService.getH5DishList(categoryId));
    }

    @GetMapping("/dishes/search")
    public Result<List<DishVO>> searchDishes(@RequestParam String keyword) {
        return Result.success(dishService.searchDishes(keyword));
    }

    @GetMapping("/banners")
    public Result<List<BannerVO>> getBanners() {
        return Result.success(bannerService.getH5BannerList());
    }
}
