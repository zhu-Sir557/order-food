package com.restaurant.controller.admin;

import com.restaurant.common.PageResult;
import com.restaurant.common.Result;
import com.restaurant.dto.DishQueryDTO;
import com.restaurant.dto.DishSaveDTO;
import com.restaurant.dto.OrderStatusDTO;
import com.restaurant.service.DishService;
import com.restaurant.vo.DishVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dishes")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    @GetMapping("/page")
    public Result<PageResult<DishVO>> getDishPage(DishQueryDTO queryDTO) {
        return Result.success(dishService.getDishPage(queryDTO));
    }

    @PostMapping
    public Result<Void> addDish(@Valid @RequestBody DishSaveDTO saveDTO) {
        dishService.addDish(saveDTO);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateDish(@PathVariable Long id, @Valid @RequestBody DishSaveDTO saveDTO) {
        dishService.updateDish(id, saveDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteDish(@PathVariable Long id) {
        dishService.deleteDish(id);
        return Result.success();
    }

    @PatchMapping("/{id}/status")
    public Result<Void> updateDishStatus(@PathVariable Long id, @RequestBody OrderStatusDTO statusDTO) {
        dishService.updateDishStatus(id, statusDTO.getStatus());
        return Result.success();
    }
}
