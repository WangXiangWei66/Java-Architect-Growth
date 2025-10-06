package com.msb.caffeine.lock.redistypes;
import redis.clients.jedis.Jedis;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
//hyperloglog的原理的大致解释
public class HyperLogLogSimulation {

    private static final int BUCKET_COUNT = 16384; // 默认的桶数

    // 模拟 pfadd 操作
    public static void pfadd(Jedis jedis, String key, String... elements) {
        for (String element : elements) {
            // 使用哈希函数获取哈希值
            long hash = hash(element);

            // 计算桶索引（使用哈希值的低14位）
            int bucketIndex = (int) (hash & (BUCKET_COUNT - 1));

            // 获取最大连续出现0的个数 （使用哈希值的高32位）
            int maxBitPosition = getMaxBitPosition(hash);
            System.out.println("bucketIndex: " + bucketIndex + ", maxBitPosition: " + maxBitPosition);
            // 更新桶中的最大位位置（hash的处理）
            jedis.hset("hllbucket"+key, String.valueOf(bucketIndex), String.valueOf(Math.max(
                    Long.parseLong(jedis.hget(key, String.valueOf(bucketIndex)) != null ?
                            jedis.hget(key, String.valueOf(bucketIndex)) : "0"),
                    maxBitPosition
            )));
        }
    }

    // 模拟 pfcount 操作
    public static long pfcount(Jedis jedis, String key) {
        double sum = 0.0;
        for (int i = 0; i < BUCKET_COUNT; i++) {
            String value = jedis.hget("hllbucket"+key, String.valueOf(i));
            int m = value != null ? Integer.parseInt(value) : 0;
            if(m!=0){
                sum += m;
            }
        }
        double estimate = sum/2;
        return (long) estimate;
    }

    // 哈希函数，使用 SHA-256 哈希的简化版本(这里最难的就是这个hash函数，我们案例这里的hash函数是简化，会有误差)
    private static long hash(String element) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(element.getBytes(StandardCharsets.UTF_8));
            return bytesToLong(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    // 将字节数组转换为长整型
    private static long bytesToLong(byte[] bytes) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value = (value << 8) | (bytes[i] & 0xFF);
        }
        return value;
    }

    // 获取哈希值中的最大位位置（使用哈希值的低32位）
    private static int getMaxBitPosition(long hash) {
        int position = 0;
        // 只使用哈希值的低32位
        int low32Bits = (int) (hash & 0xFFFFFFFFL);
        while ((low32Bits & 1) == 0) {
            position++;
            low32Bits >>= 1;
        }
        return position;
    }

    public static void main(String[] args) {
        Jedis jedis = new Jedis("127.0.0.1", 6379);

        String key1 = "hll1";
        String key2 = "hll2";

        // 初始化
        jedis.del(key1, key2,"hllbucket"+key1,"hllbucket"+key2);

        // 模拟 pfadd，添加更多数据
        pfadd(jedis, key1, "element1", "element2", "element3", "element4", "element5", "element6");
        //pfadd(jedis, key2, "element2", "element3", "element4", "element5", "element6", "element7");

        // 模拟 pfcount
        System.out.println("Count for key1: " + pfcount(jedis, key1));
        //System.out.println("Count for key2: " + pfcount(jedis, key2));

        jedis.close();
    }
}