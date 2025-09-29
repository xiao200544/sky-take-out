package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        // 判断当前购物车中是否已存在
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list != null && !list.isEmpty()){
            // 如果存在添加一个数量
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            /*
             还要去更新数据库
             update shopping_cart set number =? where id =?
            */
            shoppingCartMapper.updateNumberById(cart);
        }else {
            // 如果不存在，插入一条数据
            // 判断本次添加的是菜品还是套餐
            Long dishId = shoppingCartDTO.getDishId();
            if (dishId != null){
                // 本次添加的是菜品
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            }else {
                // 本次添加的是套餐
                Long setmealId = shoppingCartDTO.getSetmealId();
                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }

    }

    /**
     * 查看购物车
     * @return
     */
    public List<ShoppingCart> showShoppingCart() {
        // 获取当前微信用户的id
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(userId).build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list;
    }

    /**
     * 清空购物车
     */
    public void cleanShoppingCart() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
    }

    /**
     * 删除购物车中的一个商品
     * @param shoppingCartDTO
     */
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        // 到购物车中查询当前用户的数据
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(BaseContext.getCurrentId()).build();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list != null && !list.isEmpty()){
            shoppingCart = list.get(0);
            Integer number = shoppingCart.getNumber();
            if (number == 1){
                // 如果个数为1,删除记录
                shoppingCartMapper.deleteById(shoppingCart.getId());
            }else {
                // 如果数量不为1，number - 1
                shoppingCart.setNumber(shoppingCart.getNumber() - 1);
                shoppingCartMapper.updateNumberById(shoppingCart);
            }
        }
    }
}
