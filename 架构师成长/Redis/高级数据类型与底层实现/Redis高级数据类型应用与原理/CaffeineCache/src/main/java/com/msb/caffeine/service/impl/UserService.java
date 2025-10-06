package com.msb.caffeine.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.msb.caffeine.bean.User;
import com.msb.caffeine.cache.CacheType;
import com.msb.caffeine.cache.DoubleCache;
import com.msb.caffeine.mapper.UserMapper;
import com.msb.caffeine.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Service的实现类
 * 必须继承ServiceImpl 并且在泛型中指定 对应的Mapper和实体对象
 */
@Service
@Slf4j
public class UserService extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private Cache cache;


    //Caffeine+Redis两级缓存查询-- 使用注解
    //@Cacheable:
    // 根据键从缓存中取值，如果缓存存在，那么获取缓存成功之后，直接返回这个缓存的结果。如果缓存不存在，那么执行方法，并将结果放入缓存中。
    @Cacheable(value = "user", key = "#userId")
    public User query2_2(long userId){
        String key = "user-"+userId;
        //先查询 Redis  （2级缓存）
        Object obj = redisTemplate.opsForValue().get(key);
        if (Objects.nonNull(obj)) {
            log.info("get data from redis:"+key);
            return (User)obj;
        }
        // Redis没有则查询 DB（MySQL）
        User user = userMapper.selectById(userId);
        log.info("get data from database:"+userId);
        redisTemplate.opsForValue().set(key, user, 30, TimeUnit.SECONDS);

        return user;
    }

    //只有当userId为偶数时才会进行缓存
    @Cacheable(value = "user", key = "#userId", condition="#userId%2==0")
    public User query2_3(long userId){
        String key = "user-"+userId;
        //先查询 Redis  （2级缓存）
        Object obj = redisTemplate.opsForValue().get(key);
        if (Objects.nonNull(obj)) {
            log.info("get data from redis:"+key);
            return (User)obj;
        }
        // Redis没有则查询 DB（MySQL）
        User user = userMapper.selectById(userId);
        log.info("get data from database:"+userId);
        redisTemplate.opsForValue().set(key, user, 30, TimeUnit.SECONDS);

        return user;
    }
    public void deleteCache(String cacheType) {
        cache.invalidate(cacheType);
    }

    //清除缓存(所有的元素)
    @CacheEvict(value="user", key = "#userId",allEntries=true)
    public void deleteAll(long userId) {
        System.out.println(userId);
    }
    //beforeInvocation=true：在调用该方法之前清除缓存中的指定元素
    @CacheEvict(value="user", key = "#userId",beforeInvocation=true)
    public void delete(long userId) {
        System.out.println(userId);
    }

    //不管之前的键对应的缓存是否存在，都执行方法，并将结果强制放入缓存。
    @CachePut(value="user", key = "#userId")
    public void CachePut(long userId) {
        System.out.println(userId);
    }













    @DoubleCache(cacheName = "user", key = "#userId",
            type = CacheType.PUT)
    public User query3(Long userId) {
        User user = userMapper.selectById(userId);
        return user;
    }

    @DoubleCache(cacheName = "user",key = "#user.userId",
            type = CacheType.PUT)
    public int update3(User user) {
        return userMapper.updateById(user);
    }

    @DoubleCache(cacheName = "user",key = "#user.userId",
            type = CacheType.DELETE)
    public void deleteOrder(User user) {
        userMapper.deleteById(user);
    }
}
