package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderMapper orderMapper;
    /**
     * 统计指定时间内的营业额数据
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        // 计算日期，并加入到集合中
        while (!begin.equals(end)){
            // 日期计算到end
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        // 计算每天都的营业额，并加入到集合中
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            // 计算每天都的00：00 到 23：59：599999
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // select sum(amount) from order where status = 5 and order_time > beginTime and order_time < endTime;
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.getByMap(map);
            // 如果那天没有营业额，查询为null，赋值为0.0
            turnover = turnover != null ? turnover : 0.0;
            turnoverList.add(turnover);
        }

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 统计指定时间内的用户数据
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        // 计算日期，并加入到集合中
        while (!begin.equals(end)){
            // 日期计算到end
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        // 每天新增的用户数量
        List<Integer> newUserList = new ArrayList<>();
        // 每天的总用户数量
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            // 计算每天都的00：00 到 23：59：599999
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("end", endTime);
            Integer total = userMapper.getByMap(map);
            totalUserList.add(total);

            map.put("begin", beginTime);
            Integer newNum = userMapper.getByMap(map);
            newUserList.add(newNum);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }

    /**
     * 统计指定时间内的订单数据
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        // 计算日期，并加入到集合中
        while (!begin.equals(end)){
            // 日期计算到end
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        // 每天的订单总数
        List<Integer> total = new ArrayList<>();
        // 每天的有效订单总数
        List<Integer> validTotal = new ArrayList<>();
        // 订单总数
        Integer totalOrderCount = 0;
        // 有效订单总数
        Integer validOrderCount = 0;
        for (LocalDate date : dateList) {
            // 每天的订单总数
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Integer orderCount = getOrderCount(beginTime, endTime, null);

            // 每天有效的订单总数
            Integer validCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);
            total.add(orderCount);
            validTotal.add(validCount);

            // 计算订单总数和有效订单总数
            totalOrderCount += orderCount;
            validOrderCount += validCount;
        }
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0){
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(total, ","))
                .validOrderCountList(StringUtils.join(validTotal, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status){
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", status);
        return orderMapper.countByMap(map);
    }
}
