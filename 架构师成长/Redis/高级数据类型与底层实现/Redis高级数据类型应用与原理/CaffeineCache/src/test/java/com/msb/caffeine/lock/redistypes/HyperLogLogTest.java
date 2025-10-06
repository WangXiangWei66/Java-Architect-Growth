package com.msb.caffeine.lock.redistypes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.Jedis;
///每日UV统计（HyperLogLog）
@SpringBootTest
public class HyperLogLogTest {

    Jedis jedis;
    public HyperLogLogTest() {
        jedis =  new Jedis("127.0.0.1",6379);
    }

    @Test
    void testDailyUV() {
        String todayKey = "uv:20250606";
        String yesterdayKey = "uv:20250605";
        jedis.del(todayKey);
        jedis.del(yesterdayKey);
        jedis.del("uv:twodays");

        // 模拟添加用户访问 10万的访问 循环
        for (int i = 0; i < 100000; i++) {
            String user = "user" + (i % 50000); // 50%重复用户
            jedis.pfadd(todayKey, user);  //这里UV应该是5万
        }

        // 模拟添加用户访问
        for (int i = 0; i < 100000; i++) {
            String user = "user-2-" + (i % 10000); // 重复率10倍
            jedis.pfadd(yesterdayKey, user);//这里UV应该是1万
        }

        // 获取UV统计
        long uv = jedis.pfcount(todayKey);
        System.out.println("todayKey UV: " + uv); // ≈50000

        // 获取UV统计
        long uv2 = jedis.pfcount(yesterdayKey);
        System.out.println("yesterdayKey UV: " + uv2); // ≈10000

        // 合并2日数据
        jedis.pfmerge("uv:twodays", todayKey, yesterdayKey);
        System.out.println("twodays UV: " + jedis.pfcount("uv:twodays"));
    }
}
