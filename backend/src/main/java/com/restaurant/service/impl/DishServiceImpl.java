package com.restaurant.service.impl;

import com.restaurant.common.BizException;
import com.restaurant.common.PageResult;
import com.restaurant.dto.DishQueryDTO;
import com.restaurant.dto.DishSaveDTO;
import com.restaurant.entity.Category;
import com.restaurant.entity.Dish;
import com.restaurant.mapper.CategoryMapper;
import com.restaurant.mapper.DishMapper;
import com.restaurant.service.DishService;
import com.restaurant.vo.DishVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {

    private final DishMapper dishMapper;
    private final CategoryMapper categoryMapper;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResult<DishVO> getDishPage(DishQueryDTO queryDTO) {
        Page<Map<String, Object>> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        IPage<Map<String, Object>> result = dishMapper.selectDishPage(
                page, queryDTO.getCategoryId(), queryDTO.getName(), queryDTO.getStatus());

        List<DishVO> voList = result.getRecords().stream().map(this::mapToDishVO).collect(Collectors.toList());
        return new PageResult<>(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    @Transactional
    public void addDish(DishSaveDTO saveDTO) {
        Dish dish = new Dish();
        dish.setCategoryId(saveDTO.getCategoryId());
        dish.setName(saveDTO.getName());
        dish.setPrice(saveDTO.getPrice());
        dish.setImage(saveDTO.getImage());
        dish.setDescription(saveDTO.getDescription());
        dish.setTasteConfig(saveDTO.getTasteConfig());
        dish.setStock(saveDTO.getStock() != null ? saveDTO.getStock() : 0);
        dish.setStatus(1);
        dishMapper.insert(dish);
    }

    @Override
    @Transactional
    public void updateDish(Long id, DishSaveDTO saveDTO) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new BizException("菜品不存在");
        }
        dish.setCategoryId(saveDTO.getCategoryId());
        dish.setName(saveDTO.getName());
        dish.setPrice(saveDTO.getPrice());
        dish.setImage(saveDTO.getImage());
        dish.setDescription(saveDTO.getDescription());
        dish.setTasteConfig(saveDTO.getTasteConfig());
        if (saveDTO.getStock() != null) {
            dish.setStock(saveDTO.getStock());
        }
        dishMapper.updateById(dish);
    }

    @Override
    @Transactional
    public void deleteDish(Long id) {
        dishMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void updateDishStatus(Long id, Integer status) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new BizException("菜品不存在");
        }
        dish.setStatus(status);
        dishMapper.updateById(dish);
    }

    @Override
    public List<DishVO> getH5DishList(Long categoryId) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getStatus, 1);
        if (categoryId != null) {
            wrapper.eq(Dish::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(Dish::getCreateTime);
        List<Dish> dishes = dishMapper.selectList(wrapper);
        return dishes.stream().map(this::entityToDishVO).collect(Collectors.toList());
    }

    @Override
    public List<DishVO> searchDishes(String keyword) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getStatus, 1);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Dish::getName, keyword);
        }
        List<Dish> dishes = dishMapper.selectList(wrapper);
        return dishes.stream().map(this::entityToDishVO).collect(Collectors.toList());
    }

    private DishVO mapToDishVO(Map<String, Object> map) {
        DishVO vo = new DishVO();
        vo.setId(toLong(map.get("id")));
        vo.setCategoryId(toLong(map.get("categoryId")));
        vo.setCategoryName((String) map.get("categoryName"));
        vo.setName((String) map.get("name"));
        vo.setPrice(toBigDecimal(map.get("price")));
        vo.setImage((String) map.get("image"));
        vo.setDescription((String) map.get("description"));
        vo.setTasteConfig((String) map.get("tasteConfig"));
        vo.setStock(toInt(map.get("stock")));
        vo.setStatus(toInt(map.get("status")));
        Object ct = map.get("createTime");
        if (ct != null) {
            vo.setCreateTime(ct.toString());
        }
        return vo;
    }

    private DishVO entityToDishVO(Dish dish) {
        DishVO vo = new DishVO();
        vo.setId(dish.getId());
        vo.setCategoryId(dish.getCategoryId());
        Category cat = categoryMapper.selectById(dish.getCategoryId());
        if (cat != null) {
            vo.setCategoryName(cat.getName());
        }
        vo.setName(dish.getName());
        vo.setPrice(dish.getPrice());
        vo.setImage(dish.getImage());
        vo.setDescription(dish.getDescription());
        vo.setTasteConfig(dish.getTasteConfig());
        vo.setStock(dish.getStock());
        vo.setStatus(dish.getStatus());
        if (dish.getCreateTime() != null) {
            vo.setCreateTime(dish.getCreateTime().format(FMT));
        }
        return vo;
    }

    private Long toLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Long) return (Long) obj;
        if (obj instanceof Number) return ((Number) obj).longValue();
        return Long.valueOf(obj.toString());
    }

    private Integer toInt(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Integer) return (Integer) obj;
        if (obj instanceof Number) return ((Number) obj).intValue();
        return Integer.valueOf(obj.toString());
    }

    private java.math.BigDecimal toBigDecimal(Object obj) {
        if (obj == null) return null;
        if (obj instanceof java.math.BigDecimal) return (java.math.BigDecimal) obj;
        return new java.math.BigDecimal(obj.toString());
    }
}
