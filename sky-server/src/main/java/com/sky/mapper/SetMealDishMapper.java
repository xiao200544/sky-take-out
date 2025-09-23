package com.sky.mapper;


import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetMealDishMapper {

    /**
     * 根据菜品id查询关联的n个套餐id
     * @param ids
     * @return
     */
    List<Long> getMealDishByDishId(List<Long> ids);
}
