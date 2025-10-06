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
        //这里使用HyperLogLog 放入10万个元素，然后进行UV统计
        try {
            jedis1 = jedisPool.getResource();
            for(int i=0;i<100000;i++){ //1万个元素
                jedis1.pfadd("hyper-count","user"+i);
            }
            long total = jedis1.pfcount("hyper-count");
            System.out.println("实际次数:" + 10000 + "，HyperLogLog统计次数:"+total);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis1.close();
        }

        //这里使用set 放入100万个元素，然后进行UV统计
        Jedis jedis2 = null;
        try {
            jedis2 = jedisPool.getResource();
            for(int i=0;i<100000;i++){ //1万个元素
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
