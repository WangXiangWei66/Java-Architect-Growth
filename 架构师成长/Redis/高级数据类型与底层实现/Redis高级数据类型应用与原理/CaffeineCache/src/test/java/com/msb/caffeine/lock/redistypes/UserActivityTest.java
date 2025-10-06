package com.msb.caffeine.lock.redistypes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.Jedis;
//用户活跃分析（Bitmap）
@SpringBootTest
public class UserActivityTest {

    Jedis jedis;
    public UserActivityTest() {
        jedis =  new Jedis("127.0.0.1",6379);
    }
    //如果要记录用户的登录情况，想到的使用set来做。 100用户， set集合  很大 ，每天都要记录  100M *365  *10
    // key:activity:202506    set  [123456,222,1111,44,55,66,778,99,88,00]
    //使用bitmap   做365个key ,每个 key中的offset -> userId
    //  100K *365  *10
    @Test
    void testUserActivity() {
        long userId = 123456;
        String keyPrefix = "activity:202506";
        jedis.del(keyPrefix);

        // 记录6月份登录情况
        recordLogin(keyPrefix, userId, 5);  // 6月5日登录
        recordLogin(keyPrefix, userId, 7);  // 6月7日登录
        recordLogin(keyPrefix, userId, 8);  // 6月8日登录

        recordLogin(keyPrefix, userId, 12);  // 6月12日登录
        recordLogin(keyPrefix, userId, 13);  // 6月13日登录
        recordLogin(keyPrefix, userId, 14);  // 6月14日登录
        recordLogin(keyPrefix, userId, 15);  // 6月15日登录
        recordLogin(keyPrefix, userId, 16);  // 6月16日登录
        recordLogin(keyPrefix, userId, 22);  // 6月22日登录
        recordLogin(keyPrefix, userId, 28);  // 6月28日登录

        // 检查活跃情况
        System.out.println("Total active days: " +
                getActiveDays(keyPrefix, userId));
        //检查用户在指定月份的某一天是否登录。
        System.out.println("Active on 7: " +
                isActiveOnDay(keyPrefix, userId, 7));
        //计算用户在指定时间段内的最大连续活跃天数
        System.out.println("Max continuous: " +
                getMaxContinuous(keyPrefix, userId, 1, 30));
    }

    private void recordLogin(String keyPrefix, long userId, int day) {
        jedis.setbit(keyPrefix + day, userId, true);
    }

    private int getActiveDays(String keyPrefix, long userId) {
        int count = 0;
        for (int day = 1; day <= 30; day++) {
            if (jedis.getbit(keyPrefix + day, userId)) count++;
        }
        return count;
    }

    private boolean isActiveOnDay(String keyPrefix, long userId, int day) {
        return jedis.getbit(keyPrefix + day, userId);
    }

    private int getMaxContinuous(String keyPrefix, long userId, int start, int end) {
        int max = 0, current = 0;
        for (int day = start; day <= end; day++) {
            if (jedis.getbit(keyPrefix + day, userId)) {
                current++;
                max = Math.max(max, current);
            } else {
                current = 0;
            }
        }
        return max;
    }
}
