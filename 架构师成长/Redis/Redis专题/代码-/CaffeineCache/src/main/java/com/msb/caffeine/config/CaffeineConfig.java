package com.msb.caffeine.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineConfig {
    @Bean
    public Cache<String,Object> caffeineCache(){
        return Caffeine.newBuilder()
                .initialCapacity(128)//初始大小
                .maximumSize(1024)//最大数量
                .expireAfterAccess(15, TimeUnit.SECONDS) //如果超过15s没有访问，该缓存就过期
                //.expireAfterWrite(15, TimeUnit.SECONDS)//过期时间 15S
                .build();
    }






    @Bean
    public CacheManager cacheManager(){
        CaffeineCacheManager cacheManager=new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(128)
                .maximumSize(1024)
                .expireAfterWrite(15, TimeUnit.SECONDS));
        return cacheManager;
    }
}
