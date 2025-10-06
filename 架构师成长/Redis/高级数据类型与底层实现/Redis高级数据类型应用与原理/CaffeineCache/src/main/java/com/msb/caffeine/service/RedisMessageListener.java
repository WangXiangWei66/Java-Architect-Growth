package com.msb.caffeine.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.msb.caffeine.service.impl.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisMessageListener  implements MessageListener {

    @Autowired
    private Cache cache;

    //这里就是应用接收到了（要删除缓存的策略）： 这里就强制删除Caffeine中的缓存数据
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String cacheKey = new String(message.getBody());
        if(!cacheKey.equals("")){
            log.info("invalidate:"+cacheKey);
            cache.invalidate(cacheKey);
        }
    }
}
