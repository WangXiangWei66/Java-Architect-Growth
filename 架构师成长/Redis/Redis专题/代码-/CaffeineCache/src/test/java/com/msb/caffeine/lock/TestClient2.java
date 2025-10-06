package com.msb.caffeine.lock;

import org.junit.jupiter.api.Test;
import org.redisson.api.*;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class TestClient2 {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redisson;

    @Test
    public void JedisDemo() {

        Jedis jedis =  new Jedis("127.0.0.1",6379);

        // 设置字符串值
        jedis.del("key1");
        jedis.set("key1", "value1");
        // 获取字符串值
        String value = jedis.get("key1");
        System.out.println(value);

        // 向列表中插入元素
        jedis.del("list1");
        jedis.lpush("list1", "element1", "element2", "element3");
        // 获取列表中的所有元素
        List elements = jedis.lrange("list1", 0, -1);
        System.out.println(elements);

        // 向集合中添加元素
        jedis.del("set1");
        jedis.sadd("set1", "member1", "member2", "member3");
        // 获取集合中的所有元素
        Set members = jedis.smembers("set1");
        System.out.println(members);


        // 设置哈希字段值
        jedis.del("hash1");
        jedis.hset("hash1", "field1", "value1");
        // 获取哈希字段值
        String hashValue = jedis.hget("hash1", "field1");
        System.out.println(hashValue);


        // 向有序集合中添加元素
        jedis.del("zset1");
        jedis.zadd("zset1", 100, "player1");
        jedis.zadd("zset1", 200, "player2");
        jedis.zadd("zset1", 150, "player3");

        // 获取有序集合中的排名榜
        Set<Tuple> ranking = jedis.zrevrangeWithScores("zset1", 0, -1);

        // 展示排名榜
        int rank = 1;
        for (Tuple tuple : ranking) {
            String player = tuple.getElement();
            double score = tuple.getScore();
            System.out.println("排名：" + rank + "，玩家：" + player + "，得分：" + score);
            rank++;
        }
    }
    @Test
    public void RedisTemplateDemo() {


        // 设置字符串值
        redisTemplate.delete("key1");
        redisTemplate.opsForValue().set("key1", "value1");
        // 获取字符串值
        String value = redisTemplate.opsForValue().get("key1").toString();
        System.out.println(value);

        // 向列表中插入元素
        redisTemplate.delete("list1");
        redisTemplate.opsForList().leftPushAll("list1", "element1", "element2", "element3");
        // 获取列表中的所有元素
        List<String> elements = redisTemplate.opsForList().range("list1", 0, -1);
        System.out.println(elements);

        // 向集合中添加元素
        redisTemplate.delete("set1");
        redisTemplate.opsForSet().add("set1", "member1", "member2", "member3");
        // 获取集合中的所有元素
        Set<String> members = redisTemplate.opsForSet().members("set1");
        System.out.println(members);

        // 设置哈希字段值
        redisTemplate.delete("hash1");
        redisTemplate.opsForHash().put("hash1", "field1", "value1");
        // 获取哈希字段值
        String hashValue = (String) redisTemplate.opsForHash().get("hash1", "field1");
        System.out.println(hashValue);

        // 向有序集合中添加元素
        ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();


        redisTemplate.delete("zset1");
        zSetOperations.add("zset1", "player1", 100);
        zSetOperations.add("zset1", "player2", 200);
        zSetOperations.add("zset1", "player3", 150);
        // 获取有序集合中的排名榜
        Set<ZSetOperations.TypedTuple<Object>> ranking = zSetOperations.reverseRangeWithScores("zset1", 0, -1);
        // 展示排名榜
        int rank = 1;
        for (ZSetOperations.TypedTuple<Object> tuple : ranking) {
            String player = tuple.getValue().toString();
            double score = tuple.getScore();
            System.out.println("排名：" + rank + "，玩家：" + player + "，得分：" + score);
            rank++;
        }
    }

    @Test
    public void RedissonDemo() {
        // 设置字符串值
        redisson.getBucket("key1").delete();
        redisson.getBucket("key1").set("value1");
        // 获取字符串值
        String value = redisson.getBucket("key1").toString();
        System.out.println(value);

        // 向列表中插入元素
        RList<String> list = redisson.getList("list1");
        list.delete();
        list.add("element1");
        list.add("element2");
        list.add("element3");
        // 获取列表中的所有元素
        List<String> elements = list.readAll();
        System.out.println(elements);

        // 向集合中添加元素
        RSet<String> set = redisson.getSet("set1");
        set.delete();
        set.add("member1");
        set.add("member2");
        set.add("member3");
        // 获取集合中的所有元素
        Set<String> members = set.readAll();
        System.out.println(members);

        // 设置哈希字段值
        RMap<String, String> map = redisson.getMap("hash1");
        map.delete();
        map.put("field1", "value1");
        // 获取哈希字段值
        String hashValue = map.get("field1");
        System.out.println(hashValue);

        // 向有序集合中添加元素
        RScoredSortedSet<String> zset = redisson.getScoredSortedSet("zset1");
        zset.delete();
        zset.add(100, "player1");
        zset.add(200, "player2");
        zset.add(150, "player3");
        // 获取有序集合中的排名榜
        Collection<ScoredEntry<String>> ranking = zset.entryRangeReversed(0, -1);
        // 展示排名榜
        int rank = 1;
        for (ScoredEntry<String> entry : ranking) {
            String player = entry.getValue();
            double score = entry.getScore();
            System.out.println("排名：" + rank + "，玩家：" + player + "，得分：" + score);
            rank++;
        }
    }


    @Test
    public void LockExample() {
        long goods_id =13;
        String Lockkey ="goods-number"+goods_id;
        RLock lock = redisson.getLock(Lockkey);

        //lock.lock();//这种代码下 是会开启看门狗的（默认的10秒运行一次， 这种Redis的TTL30秒）
        lock.lock(30, TimeUnit.SECONDS); //这种就不会启动看门狗了

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
       }

        lock.unlock();


    }

    @Test
    public void CountDownLatchExample() throws Exception{
        // 在其他线程或其他JVM里
        RCountDownLatch latch = redisson.getCountDownLatch("anyCountDownLatch");
        latch.countDown();
    }

}
