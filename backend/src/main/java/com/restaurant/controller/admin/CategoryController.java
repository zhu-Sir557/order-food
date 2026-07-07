package com.restaurant.controller.admin;

import com.restaurant.common.Result;
import com.restaurant.dto.CategorySaveDTO;
import com.restaurant.service.CategoryService;
import com.restaurant.vo.CategoryVO;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public Result<List<CategoryVO>> getCategoryList() {
        return Result.success(categoryService.getCategoryList());
    }

    @PostMapping
    public Result<Void> addCategory(@Valid @RequestBody CategorySaveDTO saveDTO) {
        categoryService.addCategory(saveDTO);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @Valid @RequestBody CategorySaveDTO saveDTO) {
        categoryService.updateCategory(id, saveDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }

    @PutMapping("/sort")
    public Result<Void> updateSort(@RequestBody List<Long> ids) {
        categoryService.updateSort(ids);
        return Result.success();
    }
}
