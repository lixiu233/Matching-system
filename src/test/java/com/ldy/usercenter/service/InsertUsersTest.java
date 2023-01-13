package com.ldy.usercenter.service;

import com.ldy.usercenter.model.domain.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InsertUsersTest {

    @Resource
    private UserService userService;

    // 定义日志记录器对象
    public static final Logger LOG = LogManager.getLogger(InsertUsersTest.class);

    /**
     * 批量插入用户
     */
    @Test
    public void doInsertUsers(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_MUN = 10000;
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_MUN; i++) {
            User user = new User();
            user.setUsername("假账号");
            user.setUserAccount("");
            user.setUserPassword("123456789");
            user.setGender(0);
            user.setAvatarUrl("https://gitee.com/li-xiuer/img/raw/master/INimg/img.jpeg");
            user.setPhone("12345678910");
            user.setEmail("3778@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setTags("[]");
            userList.add(user);
        }
        userService.saveBatch(userList, 1000);
        stopWatch.stop();
        LOG.info(stopWatch.getTotalTimeMillis());
    }

    /**
     * 并发批量插入用户
     */
    @Test
    public void doConcurrencyInsertUsers(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_MUN = 10000;
        //分10组
        int j = 0;
        List<CompletableFuture> futureList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            List<User> userList = new ArrayList<>();
            while (true){
                j++;
                User user = new User();
                user.setUsername("假账号");
                user.setUserAccount("WWW"+j);
                user.setUserPassword("123456789");
                user.setGender(0);
                user.setProfile("");
                user.setAvatarUrl("https://gitee.com/li-xiuer/img/raw/master/INimg/img.jpeg");
                user.setPhone("");
                user.setEmail("");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setTags("[]");
                userList.add(user);
                if (j % 1000 == 0){
                    break;
                }
            }
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                LOG.info("threadName:"+Thread.currentThread().getName());
                userService.saveBatch(userList, 1000);
            });
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        //20秒 1万条
        stopWatch.stop();
        LOG.info(stopWatch.getTotalTimeMillis());
    }

}
