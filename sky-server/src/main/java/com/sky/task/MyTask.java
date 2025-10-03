package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 自定义定时任务类
 */
@Component
@Slf4j
public class MyTask {
    /**
     * 每5秒触发一次
     */
//    @Scheduled(cron = "0/5 * * * * ?")
    public void task(){
        log.info("自定义定时任务类:{}", new Date());
    }

}
