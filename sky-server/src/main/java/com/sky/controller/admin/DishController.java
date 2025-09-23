package com.sky.controller.admin;

import com.alibaba.druid.filter.logging.Log4j2Filter;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.mapper.DishMapper;
import com.sky.result.Result;
import com.sky.service.DishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 新增菜品
 */
@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    /**
     * 新增菜品
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result sava(@RequestBody DishDTO dishDTO){
        log.info("新增菜品");
        dishService.saveWithFlavors(dishDTO);
        return Result.success();
    }
}
