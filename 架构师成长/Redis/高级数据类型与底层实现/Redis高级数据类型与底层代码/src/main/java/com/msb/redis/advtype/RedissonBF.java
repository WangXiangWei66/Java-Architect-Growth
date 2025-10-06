package com.msb.redis.advtype;

import org.redisson.Redisson;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

/*Redisson底层基于位图实现了一个布隆过滤器，使用非常方便*/
public class RedissonBF {
    public static void main(String[] args) {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");

        //构造Redisson
        RedissonClient redisson = Redisson.create(config);

        RBloomFilter<String> bloomFilter = redisson.getBloomFilter("phoneList");
        //初始化布隆过滤器：预计元素为20000L,误差率为3%
        bloomFilter.tryInit(50000L,0.01);
        //将号码1~10086插入到布隆过滤器中
        for(int i =1;i<=10086 ;i++){
            bloomFilter.add(String.valueOf(i));
        }

        //判断下面号码是否在布隆过滤器中
        System.out.println("996:BF--"+bloomFilter.contains("996"));//true
        System.out.println("10086:BF--"+bloomFilter.contains("10086"));//true
        System.out.println("10088:BF--"+bloomFilter.contains("10088"));//false
        System.out.println("10096:BF--"+bloomFilter.contains("10096"));//false
        System.out.println("10340:BF--"+bloomFilter.contains("10340"));//?
        for(int i =10087;i<=18000 ;i++){
            if(bloomFilter.contains(String.valueOf(i))){
                System.out.println("布隆过滤器会误判的值："+i);
            }
        }


        //布隆过滤器（送入布隆过滤器的元素，判断是一定在的）
        for(int i =1;i<=10086 ;i++){
            if(!bloomFilter.contains(String.valueOf(i))){
                System.out.println("送入BF的不一定在："+i);
            }
        }


    }
}
