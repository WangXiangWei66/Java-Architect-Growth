package com.msb.caffeine.lock.redistypes;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
//亿级URL黑名单（布隆过滤器）
@SpringBootTest
public class BloomFilterTest {


    @Autowired
    private RedissonClient redisson;

    @Test
    void testUrlBlacklist() {
        String filterName = "url_blacklist";
        RBloomFilter<String> bloomFilter = redisson.getBloomFilter(filterName);

        // 初始化布隆过滤器（1亿数据，误判率1%）
        bloomFilter.tryInit(100_000_000L, 0.01);

        // 添加测试URL
        bloomFilter.add("http://malicious.com/attack");
        bloomFilter.add("http://phishing.com/login");

        // 检查URL是否存在
        System.out.println("Check malicious: " +
                bloomFilter.contains("http://malicious.com/attack")); // true

        System.out.println("Check google: " +
                bloomFilter.contains("http://google.com")); // false (可能误判)

        // 性能测试
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            bloomFilter.contains("http://test" + i + ".com");
        }
        System.out.println("100k checks in: " + (System.currentTimeMillis() - start) + "ms");
    }
}
