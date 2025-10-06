#### [Redis]()中的布隆过滤器

##### Redisson

Maven引入Redisson

```
   <dependency>
            <groupId>org.redisson</groupId>
            <artifactId>redisson</artifactId>
            <version>3.12.3</version>
        </dependency>
```

```
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
        bloomFilter.tryInit(20000L,0.03);
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
        //布隆过滤器（送入布隆过滤器的元素，判断是一定在的）
//        for(int i =1;i<=10086 ;i++){
//            if(!bloomFilter.contains(String.valueOf(i))){
//                System.out.println("送入BF的不一定在："+i);
//            }
//        }


    }
}

```

**实验结果**

![# ](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1699944921017/b5e16faed6e140898d5bf85bc930f639.png)

# Hyperloglog

#### 代码演示

```
package com.msb.redis.advtype;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
/*HyperLogLog测试UV与set的对比*/
public class HyperLogLogTest {
    public static void main(String[] args) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, "127.0.0.1", 6379, 30000);
        Jedis jedis1 = null;
        try {
            jedis1 = jedisPool.getResource();
            for(int i=0;i<10000;i++){ //1万个元素
                jedis1.pfadd("hyper-count","user"+i);
            }
            long total = jedis1.pfcount("hyper-count");
            System.out.println("实际次数:" + 10000 + "，HyperLogLog统计次数:"+total);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis1.close();
        }


        Jedis jedis2 = null;
        try {
            jedis2 = jedisPool.getResource();
            for(int i=0;i<10000;i++){ //1万个元素
                jedis2.sadd("set-count","user"+i);
            }
            long total = jedis2.scard("set-count");
            System.out.println("实际次数:" + 10000 + "，set统计次数:"+total);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis2.close();
        }

    }
}

```

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1699944921017/a3e368762e234d0daab65b557709713c.png)
