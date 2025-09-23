package com.sky.service;

import com.sky.dto.DishDTO;

public interface DishService {

    /**
     * 新增菜品及对应的口味
     */
    public void saveWithFlavors(DishDTO dishDTO);
}
