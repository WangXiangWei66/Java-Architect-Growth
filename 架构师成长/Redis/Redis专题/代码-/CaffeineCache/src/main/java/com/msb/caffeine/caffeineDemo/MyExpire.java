package com.msb.caffeine.caffeineDemo;

import com.github.benmanes.caffeine.cache.Expiry;

import java.util.concurrent.TimeUnit;

class MyExpire implements Expiry<String,String> {
    //创建后(多久失效)
    @Override
    public long expireAfterCreate( String key,  String value, long currentTime) {
        //创建后
        System.out.println("创建后,失效计算 -- "+key+": "+value);
        //将两秒转换为纳秒，并返回；代表创建后2000秒失效
        return TimeUnit.NANOSECONDS.convert(2000,TimeUnit.SECONDS);
    }
    //更行后(多久失效)
    @Override
    public long expireAfterUpdate( String key,  String value, long currentTime,  long currentDuration) {
        //更新后
        System.out.println("更新后,失效计算 -- "+key+": "+value);
        return TimeUnit.NANOSECONDS.convert(500,TimeUnit.SECONDS);
    }
    //读取后(多久失效)
    @Override
    public long expireAfterRead( String key,  String value, long currentTime,  long currentDuration) {
        //读取后
        System.out.println("读取后,失效计算 -- "+key+": "+value);
        return TimeUnit.NANOSECONDS.convert(100,TimeUnit.SECONDS);
    }
}

