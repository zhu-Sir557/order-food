package com.restaurant.service.impl;

import com.restaurant.common.BizException;
import com.restaurant.dto.CategorySaveDTO;
import com.restaurant.entity.Category;
import com.restaurant.entity.Dish;
import com.restaurant.mapper.CategoryMapper;
import com.restaurant.mapper.DishMapper;
import com.restaurant.service.CategoryService;
import com.restaurant.vo.CategoryVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final DishMapper dishMapper;

    @Override
    public List<CategoryVO> getCategoryList() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSort);
        List<Category> categories = categoryMapper.selectList(wrapper);
        return categories.stream().map(this::toCategoryVO).collect(Collectors.toList());
    }

    @Override
    public List<CategoryVO> getH5CategoryList() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, 1);
        wrapper.orderByAsc(Category::getSort);
        List<Category> categories = categoryMapper.selectList(wrapper);
        return categories.stream().map(this::toCategoryVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addCategory(CategorySaveDTO saveDTO) {
        Category category = new Category();
        category.setName(saveDTO.getName());
        category.setSort(saveDTO.getSort());
        category.setStatus(saveDTO.getStatus());
        categoryMapper.insert(category);
    }

    @Override
    @Transactional
    public void updateCategory(Long id, CategorySaveDTO saveDTO) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BizException("分类不存在");
        }
        category.setName(saveDTO.getName());
        category.setSort(saveDTO.getSort());
        category.setStatus(saveDTO.getStatus());
        categoryMapper.updateById(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getCategoryId, id);
        Long count = dishMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BizException("该分类下还有菜品，无法删除");
        }
        categoryMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void updateSort(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        int sort = 1;
        for (Long id : ids) {
            Category category = new Category();
            category.setId(id);
            category.setSort(sort++);
            categoryMapper.updateById(category);
        }
    }

    private CategoryVO toCategoryVO(Category category) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setSort(category.getSort());
        vo.setStatus(category.getStatus());

        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getCategoryId, category.getId());
        wrapper.eq(Dish::getStatus, 1);
        vo.setDishCount(Math.toIntExact(dishMapper.selectCount(wrapper)));

        return vo;
    }
}
