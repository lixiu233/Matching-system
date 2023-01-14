package com.ldy.usercenter.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ldy.usercenter.model.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedissonTest {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;


    // 重点用户
    private List<Long> mainUserList = Arrays.asList(1L);

    @Test
    void text(){
        //List
        RList<Object> list = redissonClient.getList("test-list");
        list.add("ldy");
        list.get(0);
    }

    @Test
    void text2() {
        RLock lock = redissonClient.getLock("ldy:precacgejod:docache:lock");
        try {
            // 只有一个程序获取到锁
            if (lock.tryLock(0,-1, TimeUnit.MILLISECONDS)){
                Thread.sleep(50000);
                System.out.println("gitLock:" + Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
