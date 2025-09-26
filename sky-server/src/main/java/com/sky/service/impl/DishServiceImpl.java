package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setMealDishMapper;
    /**
     * 新增菜品及对应的口味
     */
    @Transactional
    public void saveWithFlavors(DishDTO dishDTO) {
        Dish dish = new Dish();

        BeanUtils.copyProperties(dishDTO, dish);
        // 向菜品表中插入1条数据
        dishMapper.insert(dish);
        // 获取insert语句生成的主键值
        Long id = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();

        if (flavors != null && flavors.size() > 0){
            flavors.forEach(dishflavor ->{
                dishflavor.setDishId(id);
            });
            // 向口味表中插入n条数据
            dishFlavorMapper.bentch(flavors);
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> dishVOPage = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(dishVOPage.getTotal(), dishVOPage.getResult());
    }

    /**
     * 菜品的批量删除
     * @param ids
     */
    @Transactional
    public void delete(List<Long> ids) {

        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE){
                // 判断是否在起售中
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        // 判断是否有关联的套餐
        List<Long> mealDishByDishId = setMealDishMapper.getMealDishByDishId(ids);
        if (mealDishByDishId != null && mealDishByDishId.size() > 0){
            // 被关联了不可以被删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        // 可以删除了
        /*for (Long id : ids) {
            dishMapper.deleteBy(id);
            // 删除关联的口味
            dishFlavorMapper.deleteBydishId(id);
        }*/
        // 批量删除
        // delete from dish where id in (...)
        dishMapper.deleteByIds(ids);
        // 批量删除关联的口味
        // delete from dish_flavor where dish_id in (...)
        dishFlavorMapper.deleteBydishIds(ids);
    }
    /**
     * 根据id查询菜品和对应的口味
     * @param id
     * @return dishVO
     */
    public DishVO getByIdWithFlavor(Long id) {
        // 根据id查询菜品数据
        Dish dish = dishMapper.getById(id);
        // 根据id到dish_flavor中查询
        List<DishFlavor> dishFlavors = dishMapper.getByDishId(id);
        DishVO dishVO = new DishVO();
        // 拷贝属性
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    /**
     * 修改菜品及对应的口味
     * @param dishDTO
     */
    public void update(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        // 修改dish表的基本字段
        dishMapper.update(dish);
        // 修改口味，先删除，再添加
        dishFlavorMapper.deleteBydishId(dishDTO.getId());
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0){
            flavors.forEach(dishflavor ->{
                dishflavor.setDishId(dishDTO.getId());
            });
            // 向口味表中插入n条数据
            dishFlavorMapper.bentch(flavors);
        }
    }

    /**
     * 菜品的起售停售
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder().status(status).id(id).build();
        dishMapper.update(dish);
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }



    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

}
