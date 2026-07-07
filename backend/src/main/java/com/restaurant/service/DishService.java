package com.restaurant.service;

import com.restaurant.common.PageResult;
import com.restaurant.dto.DishQueryDTO;
import com.restaurant.dto.DishSaveDTO;
import com.restaurant.vo.DishVO;
import java.util.List;

public interface DishService {
    PageResult<DishVO> getDishPage(DishQueryDTO queryDTO);
    void addDish(DishSaveDTO saveDTO);
    void updateDish(Long id, DishSaveDTO saveDTO);
    void deleteDish(Long id);
    void updateDishStatus(Long id, Integer status);
    List<DishVO> getH5DishList(Long categoryId);
    List<DishVO> searchDishes(String keyword);
}
