package com.msb.caffeine.lock.redistypes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//10亿自然数排序（Bitmap实现）
@SpringBootTest
public class BitmapSortingTest {
    Jedis jedis;
    public BitmapSortingTest() {
        jedis =  new Jedis("127.0.0.1",6379);
    }

    @Test
    void testLargeNumberSorting() {
        String key = "sorted_numbers";
        jedis.del(key);  // 清除历史数据

        key.hashCode();


        // 模拟添加10万条测试数据（这里肯定是乱序）
        for (int i = 0; i < 100000; i++) {
            int num = (int)(Math.random() * 1000000);
            jedis.setbit(key, num, true);
        }

        // 查找最小值和最大值
        long min = findMin(key);
        long max = findMax(key);
        System.out.println("Min: " + min + ", Max: " + max);

        // 分页获取排序结果(顺序（升序）)
        int page = 0;
        int pageSize = 100;
        while (true) {
            long[] result = getSortedPage(key, min, max, page, pageSize);
            if (result.length == 0) break;

            System.out.println("Page " + page + ": " + Arrays.toString(result));
            page++;
        }
    }
    //从位置 0 开始，找到第一个设置为 true 的位，即最小值。
    private long findMin(String key) {

        long pos = 0;
        while (!jedis.getbit(key, pos)) {
            pos++;
        }
        return pos;
    }
    //从位置 1,000,000 开始，找到最后一个设置为 true 的位，即最大值。
    private long findMax(String key) {
        long pos = 1000000; // 已知范围上限
        while (!jedis.getbit(key, pos)) {
            pos--;
        }
        return pos;
    }
    //从 min 开始，按顺序遍历到 max，并通过 jedis.getbit 检查每个位置是否为 true
    private long[] getSortedPage(String key, long min, long max, int page, int pageSize) {
        List<Long> result = new ArrayList<>();
        long start = min + page * pageSize;
        long end = Math.min(start + pageSize, max);

        for (long i = start; i <= end; i++) {
            if (jedis.getbit(key, i)) {
                result.add(i);
            }
        }
        return result.stream().mapToLong(Long::longValue).toArray();
    }
}
