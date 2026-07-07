package com.restaurant.service;

import com.restaurant.dto.CategorySaveDTO;
import com.restaurant.vo.CategoryVO;
import java.util.List;

public interface CategoryService {
    List<CategoryVO> getCategoryList();
    List<CategoryVO> getH5CategoryList();
    void addCategory(CategorySaveDTO saveDTO);
    void updateCategory(Long id, CategorySaveDTO saveDTO);
    void deleteCategory(Long id);
    void updateSort(List<Long> ids);
}
